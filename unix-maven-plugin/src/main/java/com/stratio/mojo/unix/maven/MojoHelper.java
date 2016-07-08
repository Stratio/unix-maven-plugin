/**
 * Copyright (C) 2003 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.stratio.mojo.unix.maven;

/*
 * The MIT License
 *
 * Copyright 2009 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import static com.stratio.mojo.unix.PackageParameters.packageParameters;
import static com.stratio.mojo.unix.java.StringF.concat;
import static com.stratio.mojo.unix.util.FileModulator.modulatePath;
import static fj.Function.curry;
import static fj.Ord.stringOrd;
import static fj.P.p;
import static fj.data.List.join;
import static fj.data.List.nil;
import static fj.data.List.single;
import static fj.data.Option.none;
import static fj.data.Option.some;
import static fj.data.Set.empty;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.stratio.mojo.unix.FileAttributes;
import com.stratio.mojo.unix.MissingSettingException;
import com.stratio.mojo.unix.PackageParameters;
import com.stratio.mojo.unix.PackageVersion;
import com.stratio.mojo.unix.UnixPackage;
import com.stratio.mojo.unix.core.AssemblyOperation;
import com.stratio.mojo.unix.core.UnixPlatform;
import com.stratio.mojo.unix.io.fs.LocalFs;
import com.stratio.mojo.unix.java.StringF;
import com.stratio.mojo.unix.maven.plugin.AssemblyOp;
import com.stratio.mojo.unix.maven.plugin.CopyDirectory;
import com.stratio.mojo.unix.maven.plugin.Defaults;
import com.stratio.mojo.unix.maven.plugin.MavenProjectWrapper;
import com.stratio.mojo.unix.maven.plugin.Package;
import com.stratio.mojo.unix.maven.plugin.PackagingFormat;
import com.stratio.mojo.unix.maven.plugin.PackagingMojoParameters;
import com.stratio.mojo.unix.maven.plugin.UnknownArtifactException;
import com.stratio.mojo.unix.util.ScriptUtil;
import com.stratio.mojo.unix.util.line.AbstractLineStreamWriter;

import fj.F;
import fj.P1;
import fj.P2;
import fj.P3;
import fj.data.List;
import fj.data.Option;
import fj.data.Set;

/**
 * Utility class encapsulating how to create a package. Used by all packaging Mojos.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class MojoHelper
{
    public static final String ATTACHED_NO_ARTIFACTS_CONFIGURED = "When running in attached mode at least one package has to be configured.";
    public static final String DUPLICATE_CLASSIFIER = "Duplicate package classifier: '%s'.";
    public static final String DUPLICATE_UNCLASSIFIED = "There can only be one package without an classifier.";

    public static <UP extends UnixPackage<UP, PP>, PP extends UnixPackage.PreparedPackage> Execution create(
        Map platforms,
        String platformType,
        String formatType,
        MavenProjectWrapper project,
        boolean debug,
        boolean attachedMode,
        F<UP, UP> validateMojoSettingsAndApplyFormatSpecificSettingsToPackage,
        PackagingMojoParameters mojoParameters,
        final Log log )
        throws MojoFailureException, MojoExecutionException
    {
        PackagingFormat<UP> format = PackagingFormat.lookup( formatType );

        if ( format == null )
        {
            throw new MojoFailureException( "INTERNAL ERROR: could not find format: '" + formatType + "'." );
        }

        UnixPlatform platform = (UnixPlatform) platforms.get( platformType );

        if ( platform == null )
        {
            throw new MojoFailureException( "INTERNAL ERROR: could not find platform: '" + platformType + "'." );
        }

        /*
        // TODO: This is using a private Maven API that might change. Perhaps use some reflection magic here.
        String timestamp = snapshotTransformation.getDeploymentTimestamp();
        */

        // This chunk replaces the above getDeploymentTimestamp. However, it not ensure that all files get the
        // same timestamp. Need to look into how this is done with Maven 3
        DateFormat utcDateFormatter = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
        utcDateFormatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        String timestamp = utcDateFormatter.format( new Date() );

        LocalFs buildDirectory = new LocalFs( project.buildDirectory );

        PackageVersion version = PackageVersion.packageVersion( project.version, timestamp,
                                                                project.artifact.isSnapshot(), mojoParameters.revision );

        List<P3<UP, Package, List<AssemblyOperation>>> packages = nil();

        for ( Package pakke : validatePackages( mojoParameters.packages, attachedMode ) )
        {
            try
            {
                String name = "unix/root-" + formatType + pakke.classifier.map( dashString ).orSome( "" );

                LocalFs packageRoot = buildDirectory.resolve( name );
                packageRoot.mkdir();

                PackageParameters parameters = calculatePackageParameters( project,
                                                                           version,
                                                                           platform,
                                                                           mojoParameters,
                                                                           pakke );

                UP unixPackage = format.start( log ).
                    parameters( parameters ).
                    setVersion( version ).                      // TODO: This should go away
                    workingDirectory( packageRoot ).
                    debug( debug ).
                    basedir( project.basedir );

                // -----------------------------------------------------------------------
                // Let the implementation add its metadata
                // -----------------------------------------------------------------------

                unixPackage = validateMojoSettingsAndApplyFormatSpecificSettingsToPackage.f( unixPackage );

                // TODO: here the logic should be different if many packages are to be created.
                // Example: name should be taken from mojoParameters if there is only a single package, if not
                //          it should come from the Pakke object. This should also be validated, at least for
                //          name

                List<AssemblyOperation> assemblyOperations = createAssemblyOperations( project,
                                                                                       parameters,
                                                                                       unixPackage,
                                                                                       project.basedir,
                                                                                       buildDirectory,
                                                                                       mojoParameters.assembly,
                                                                                       pakke.assembly );

                // -----------------------------------------------------------------------
                // Dump the execution
                // -----------------------------------------------------------------------

                if ( debug )
                {
                    log.info( "=======================================================================" );
                    log.info( "Package parameters: " + parameters.id );
                    log.info( "Default file attributes: " );
                    log.info( " File      : " + parameters.defaultFileAttributes );
                    log.info( " Directory : " + parameters.defaultDirectoryAttributes );

                    log.info( "Assembly operations: " );
                    for ( AssemblyOperation operation : assemblyOperations )
                    {
                        operation.streamTo( new AbstractLineStreamWriter()
                        {
                            protected void onLine( String line )
                            {
                                log.info( line );
                            }
                        } );
                    }
                }

                packages = packages.cons( p( unixPackage, pakke, assemblyOperations ) );
            }
            catch ( UnknownArtifactException e )
            {
                Map map = new TreeMap<String, Artifact>( e.artifactMap );

                // TODO: Do not log here, throw a CouldNotFindArtifactException with the map as an argument
                log.warn( "Could not find artifact:" + e.artifact );
                log.warn( "Available artifacts:" );
                for ( Object o : map.keySet() )
                {
                    log.warn( o.toString() );
                }

                throw new MojoFailureException( "Unable to find artifact: '" + e.artifact + "'. See log for available artifacts." );
            }
            catch ( MissingSettingException e )
            {
                String msg = "Missing required setting '" + e.getSetting() + "'";
                if ( !pakke.classifier.isNone() )
                {
                    msg += ", for '" + pakke.classifier.some() + "'";
                }
                msg += ", format '" + formatType + "'.";
                throw new MojoFailureException( msg );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error creating package " + (pakke.classifier.isSome() ? "classifier '" + pakke.classifier + "'" : "") + ", format '" + formatType + "'.", e );
            }
        }

        return new Execution<UP>( packages, project, formatType, attachedMode );
    }

    public static class Execution<UP extends UnixPackage>
    {
        private final List<P3<UP, Package, List<AssemblyOperation>>> packages;

        private final MavenProjectWrapper project;

        private final String formatType;

        private final boolean attachedMode;

        public Execution( List<P3<UP, Package, List<AssemblyOperation>>> packages, MavenProjectWrapper project,
                          String formatType, boolean attachedMode )
        {
            this.packages = packages;
            this.project = project;
            this.formatType = formatType;
            this.attachedMode = attachedMode;
        }

        public void execute( String artifactType, MavenProject mavenProject, MavenProjectHelper mavenProjectHelper, ScriptUtil.Strategy strategy )
            throws MojoExecutionException, MojoFailureException
        {
            for ( P3<UP, Package, List<AssemblyOperation>> p : packages )
            {
                UnixPackage unixPackage = p._1();
                Package pakke = p._2();

                try
                {
                    // -----------------------------------------------------------------------
                    // Assemble all the files
                    // -----------------------------------------------------------------------

                    for ( AssemblyOperation assemblyOperation : p._3() )
                    {
                        assemblyOperation.perform( unixPackage );
                    }

                    // -----------------------------------------------------------------------
                    // Package the stuff
                    // -----------------------------------------------------------------------

                    String version = unixPackage.getVersion().getMavenVersion();

                    if(unixPackage.getPackageFileExtension().equals("rpm"))version=version.replace('-','_');

                   //String name = project.artifactId +

                    String architecture ="";
                    if (StringUtils.isNotEmpty(unixPackage.getArchitecture())){
                        architecture = unixPackage.getArchitecture() + ".";
                    }

                    String baseName = null;
                    if (StringUtils.isNotEmpty(unixPackage.getOutputFileName())){
                        baseName=unixPackage.getOutputFileName();
                    }else{
                        baseName=project.outputFileName;
                    }

                    String name = baseName +
                        pakke.classifier.map( dashString ).orSome( "" ) +
                        "-" + version +
                        "." + architecture + unixPackage.getPackageFileExtension();


                    File packageFile = new File( project.buildDirectory, name );

                    unixPackage.prepare( strategy ).
                        packageToFile( packageFile );

                    attach( pakke.classifier, artifactType, packageFile, mavenProject, mavenProjectHelper,
                            attachedMode );
                }
                catch ( MojoExecutionException e )
                {
                    throw e;
                }
                catch ( MojoFailureException e )
                {
                    throw e;
                }
                catch ( Exception e )
                {
                    throw new MojoExecutionException( "Unable to create package.", e );
                }
            }
        }

        private void attach( Option<String> classifier, String artifactType, File packageFile,
                             MavenProject project, MavenProjectHelper mavenProjectHelper, boolean attachedMode )
        {
            if ( attachedMode )
            {
                // In attached mode all the packages are required to have an classifier - this used to be correct - trygve
                // For some reason it is allowed to have attached artifacts without classifier as long as the types differ

                if ( classifier.isSome() )
                {
                    mavenProjectHelper.attachArtifact( project, artifactType, classifier.some(), packageFile );
                }
                else
                {
                    mavenProjectHelper.attachArtifact( project, artifactType, null, packageFile );
                }
            }
            else
            {
                if ( classifier.isNone() )
                {
                    project.getArtifact().setFile( packageFile );
                }
                else
                {
                    mavenProjectHelper.attachArtifact( project, formatType, classifier.some(), packageFile );
                }
            }
        }
    }

    public static PackageParameters calculatePackageParameters( final MavenProjectWrapper project,
                                                                PackageVersion version,
                                                                UnixPlatform platform,
                                                                final PackagingMojoParameters mojoParameters,
                                                                final Package pakke )
    {
        String id = pakke.id.orSome( new P1<String>()
        {
            public String _1()
            {
                // This used to be ${groupId}-${artifactId}, but it was too long for pkg so this is a more sane default
                if(mojoParameters.outputFileName.orSome("").isEmpty())
                    return project.artifactId + pakke.classifier.map( dashString ).orSome( "" );
                else
                    return mojoParameters.outputFileName.some();
            }
        } );

        P2<FileAttributes, FileAttributes> defaultFileAttributes =
            calculateDefaultFileAttributes( platform,
                                            mojoParameters.defaults,
                                            pakke.defaults );

        String name = pakke.name.orElse( mojoParameters.name ).orSome( project.name );
        return packageParameters( project.groupId, project.artifactId, version, id, name, pakke.classifier, defaultFileAttributes._1(), defaultFileAttributes._2() ).
            description(pakke.description.orElse(mojoParameters.description).orElse(project.description)).
            contact(mojoParameters.contact).
            size(mojoParameters.size).
            contactEmail(mojoParameters.contactEmail).
            license(getLicense(project)).
            architecture(pakke.architecture.orElse(mojoParameters.architecture)).excludeDirs(mojoParameters.excludeDirs);
    }

    public static P2<FileAttributes, FileAttributes> calculateDefaultFileAttributes( UnixPlatform platform,
                                                                                     Defaults mojo,
                                                                                     Defaults pakke )
    {
        return p( calculateFileAttributes( platform.getDefaultFileAttributes(),
                                          mojo.fileAttributes.create(),
                                          pakke.fileAttributes.create() ),
                 calculateFileAttributes( platform.getDefaultDirectoryAttributes(),
                                          mojo.directoryAttributes.create(),
                                          pakke.directoryAttributes.create() ) );
    }

    public static FileAttributes calculateFileAttributes( FileAttributes platform,
                                                          FileAttributes mojo,
                                                          FileAttributes pakke )
    {
        // Calculate default file and directory attributes.
        // Priority order (last one wins): platform -> mojo defaults -> package defaults

        return platform.
            useAsDefaultsFor( mojo ).
            useAsDefaultsFor( pakke );
    }

    public static List<AssemblyOperation> createAssemblyOperations(MavenProjectWrapper project,
                                                                   PackageParameters parameters,
                                                                   UnixPackage unixPackage,
                                                                   File basedir,
                                                                   LocalFs buildDirectory,
                                                                   List<AssemblyOp> mojoAssembly,
                                                                   List<AssemblyOp> packageAssembly)
        throws IOException, MojoFailureException, UnknownArtifactException
    {
        unixPackage.beforeAssembly( parameters.defaultDirectoryAttributes, project.timestamp );

        // Create the default set of assembly operations
        String unix = new File( basedir, "src/main/unix/files" ).getAbsolutePath();

        String classifierOrDefault = parameters.classifier.orSome( "default" );

        List<AssemblyOp> defaultAssemblyOp = nil();

        // It would be possible to use the AssemblyOperations here but this just make it easier to document
        // as it has a one-to-one relationship with what the user would configure in a POM
        for ( String s : modulatePath( classifierOrDefault, unixPackage.getPackageFileExtension(), unix ) )
        {
            File file = new File( s );

            if ( !file.isDirectory() )
            {
                continue;
            }

            CopyDirectory op = new CopyDirectory();
            op.setFrom( file );
            defaultAssemblyOp = defaultAssemblyOp.cons( op );
        }

        // Create the complete list of assembly operations.
        // Order: defaults -> mojo -> pakke
        List<List<AssemblyOp>> list = single( defaultAssemblyOp.reverse() ).
            conss( mojoAssembly ).
            conss( packageAssembly );

        List<AssemblyOp> assemblyOps = join( list.reverse() );

        List<AssemblyOperation> operations = nil();

        AssemblyOp.CreateOperationContext context = new AssemblyOp.CreateOperationContext(
            buildDirectory,
            parameters.defaultFileAttributes,
            parameters.defaultDirectoryAttributes,
            project );

        for ( AssemblyOp assemblyOp : assemblyOps )
        {
            operations = operations.cons( assemblyOp.createOperation( context ) );
        }

        return operations.reverse();
    }

    public static List<Package> validatePackages( List<Package> packages, boolean attachedMode )
        throws MojoFailureException
    {
        if ( packages.isEmpty() )
        {
            packages = single( new Package() );
        }

        Set<String> names = empty( stringOrd );
        List<Package> outPackages = nil();

        Option<Package> defaultPackage = none();

        for ( Package pakke : packages )
        {
            if ( pakke.classifier.exists( curry( StringF.equals, "default" ) ) )
            {
                pakke.classifier = none();
            }

            if ( pakke.classifier.isNone() )
            {
                if ( defaultPackage.isSome() )
                {
                    throw new MojoFailureException( DUPLICATE_UNCLASSIFIED );
                }

                defaultPackage = some( pakke );
            }
            else
            {
                if ( names.member( pakke.classifier.some() ) )
                {
                    throw new MojoFailureException( format( DUPLICATE_CLASSIFIER, pakke.classifier ) );
                }

                names = names.insert( pakke.classifier.some() );
                outPackages = outPackages.cons( pakke );
            }
        }

        if ( attachedMode )
        {
            outPackages = defaultPackage.toList().append( outPackages );

            if ( outPackages.isEmpty() )
            {
                throw new MojoFailureException( ATTACHED_NO_ARTIFACTS_CONFIGURED );
            }

            return outPackages;
        }

        if ( defaultPackage.isNone() )
        {
            throw new MojoFailureException( "When running in 'primary artifact mode' either one package has to have 'default' as classifier or there has to be one without any classifier." );
        }

        return defaultPackage.toList().append( outPackages );
    }

    private static Option<String> getLicense( MavenProjectWrapper project )
    {
        if ( project.licenses.size() == 0 )
        {
            return none();
        }

        return some( project.licenses.get( 0 ).getName() );
    }

    static F<String, String> dashString = curry( concat, "-" );
}
