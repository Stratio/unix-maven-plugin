 package com.stratio.mojo.unix.maven.deb;

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

import fj.*;
import fj.data.*;
import fj.data.List;

import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.core.*;
import com.stratio.mojo.unix.deb.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;
import com.stratio.mojo.unix.util.line.*;

import static java.nio.file.Files.setPosixFilePermissions;
import static com.stratio.mojo.unix.UnixFsObject.RegularFile;
import static com.stratio.mojo.unix.util.RelativePath.relativePath;
import static com.stratio.mojo.unix.util.line.LineStreamWriter.*;
import org.joda.time.*;

import java.io.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DebUnixPackage
    extends UnixPackage<DebUnixPackage, DebUnixPackage.DebPreparedPackage>
{
    private ControlFile controlFile;

    private FsFileCollector fileCollector;

    private boolean useFakeroot;

    private Option<String> dpkgDeb;

    private boolean debug;

    private final static ScriptUtil scriptUtil = new ScriptUtil( "preinst", "postinst", "prerm", "postrm" );

    public DebUnixPackage()
    {
        super( "deb" );
    }

    public DebUnixPackage parameters( PackageParameters parameters )
    {
        controlFile = new ControlFile( parameters.outputFileName.orSome(parameters.id)).
            version( some( getDebianVersion( parameters.version ) ) ).
            description( getDescription( parameters ) ).
            maintainer( parameters.contact ).
            installedSize( parameters.size).
            architecture( parameters.architecture.orElse( some( "all" ) ) );

        return this;
    }

    public DebUnixPackage debParameters( Option<String> priority,
                                         Option<String> section,
                                         boolean useFakeroot,
                                         Option<String> dpkgDeb,
                                         List<String> depends,
                                         List<String> recommends,
                                         List<String> suggests,
                                         List<String> preDepends,
                                         List<String> provides,
                                         List<String> replaces )
    {
        this.useFakeroot = useFakeroot;
        this.dpkgDeb = dpkgDeb;

        controlFile = controlFile.
            priority( priority ).
            section( section ).
            depends( depends ).
            recommends( recommends ).
            suggests( suggests ).
            preDepends( preDepends ).
            provides( provides ).
            replaces( replaces );

        return this;
    }

    public DebUnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp )
        throws IOException
    {
        fileCollector = new FsFileCollector( workingDirectory.resolve( relativePath( "assembly" ) ) );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        fileCollector.addDirectory( directory );
    }

    public void addFile( Fs<?> fromFile, RegularFile file )
        throws IOException
    {
        fileCollector.addFile( fromFile, file );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        fileCollector.addSymlink( symlink );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        fileCollector.apply( f );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public DebPreparedPackage prepare( ScriptUtil.Strategy strategy )
        throws Exception
    {
        LocalFs debian = fileCollector.root.resolve( relativePath( "DEBIAN" ) );
        LocalFs controlFilePath = debian.resolve( relativePath( "control" ) );

        debian.mkdir();
        LineStreamUtil.toFile(controlFile.toList(), controlFilePath.file);

        fileCollector.collect();

        ScriptUtil.Result result = scriptUtil.
            createExecution( controlFile.packageName, "deb", getScripts(), debian.file, strategy ).
            execute();


        try {
            LocalFs conffilesFilePath = debian.resolve(relativePath("conffiles"));
            conffilesFilePath.file.setExecutable(false, false);
            conffilesFilePath.file.setReadable(true, false);
            conffilesFilePath.file.setWritable(false, false);
            conffilesFilePath.file.setWritable(true, true);
        }catch(Exception e) {
            System.out.println("conffiles is not accessible");
        }

        return new DebPreparedPackage( result );
    }

    @Override
    public String getArchitecture() {
        return this.controlFile.architecture.some();
    }

    @Override
    public String getOutputFileName() {
        return this.controlFile.packageName;
    }

    public class DebPreparedPackage
        extends UnixPackage.PreparedPackage
    {
        private final ScriptUtil.Result result;

        DebPreparedPackage( ScriptUtil.Result result )
        {
            this.result = result;
        }

        public void packageToFile( File packageFile )
            throws Exception
        {
            UnixUtil.chmodIf( result.preInstall, "0755" );
            UnixUtil.chmodIf( result.postInstall, "0755" );
            UnixUtil.chmodIf( result.preRemove, "0755" );
            UnixUtil.chmodIf( result.postRemove, "0755" );

            new DpkgDeb().
                setDebug( debug ).
                setPackageRoot( fileCollector.root.file ).
                setDebFile( packageFile ).
                setUseFakeroot( useFakeroot ).
                setDpkgDeb( dpkgDeb ).
                execute();
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static Option<String> getDescription( PackageParameters parameters )
    {
        String description;

        description = parameters.name.trim();

        if ( parameters.description.isSome() )
        {
            description = description + EOL + parameters.description.some().trim();
        }

        // ----------------------------------------------------------------------
        // Trim each line, replace blank lines with " ."
        // ----------------------------------------------------------------------

        String debianDescription;

        try
        {
            BufferedReader reader = new BufferedReader( new StringReader( description.trim() ) );

            String line;

            debianDescription = reader.readLine();

            line = reader.readLine();

            if ( line != null )
            {
                debianDescription += EOL + " " + line.trim();

                line = reader.readLine();
            }

            while ( line != null )
            {
                line = line.trim();

                if ( line.equals( "" ) )
                {
                    debianDescription += EOL + ".";
                }
                else
                {
                    debianDescription += EOL + " " + line;
                }

                line = reader.readLine();
            }
        }
        catch ( IOException e )
        {
            // This won't happen.
            throw new RuntimeException( "Internal error", e );
        }

        return some( debianDescription );
    }

    public static String getDebianVersion( PackageVersion version )
    {
        String v = version.version;
        String r = version.revision.orSome("1");

        // Map Maven qualifiers (e.g. -rc1, -alpha-1) to use tilde (e.g. ~rc1, ~alpha-1).
        // This is required to preserve the expected version ordering.
        int index = v.indexOf('-');
        if (index != -1 && index < v.length() - 1 && Character.isLetter(v.charAt(index + 1))) {
             v = v.replaceFirst("-", "~");
        }

        if ( !version.snapshot )
        {
            return v;
        }

        return v + "~" + version.timestamp;
    }


}
