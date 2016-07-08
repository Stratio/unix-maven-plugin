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
 package com.stratio.mojo.unix.maven.sysvpkg;

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
import fj.data.List;
import static fj.data.List.*;
import fj.data.*;
import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.core.FsFileCollector.*;
import com.stratio.mojo.unix.io.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.sysvpkg.*;
import com.stratio.mojo.unix.sysvpkg.prototype.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;
import static org.joda.time.LocalDateTime.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkgUnixPackage
    extends UnixPackage<PkgUnixPackage, PkgUnixPackage.PkgPreparedPackage>
{
    private LocalFs prototype;
    private LocalFs pkginfoFile;
    private boolean debug;

    private static final ScriptUtil scriptUtil = new ScriptUtil( "preinstall", "postinstall", "preremove", "postremove" ).
        customScript( "depend" ).
        customScript( "checkinstall" ).
        customScript( "compver" ).
        customScript( "copyright" ).
        customScript( "request" ).
        customScript( "space" );

    private PrototypeFile prototypeFile;

    private Pkginfo pkginfo;

    private List<IoEffect> operations = nil();

    private Option<String> classifier = none();

    public PkgUnixPackage()
    {
        super( "pkg" );
    }

    public PkgUnixPackage parameters( PackageParameters parameters )
    {
        this.classifier = parameters.classifier;
        pkginfo = new Pkginfo( parameters.architecture.orSome( "all" ),
                               "application",
                               parameters.name,
                               parameters.id,
                               getPkgVersion( parameters.version ) ).
            desc( parameters.description ).
            email( parameters.contactEmail ).
            size(parameters.size).
            pstamp( some( parameters.version.timestamp ) ).
            outputFileName( parameters.outputFileName);

        return this;
    }

    // -----------------------------------------------------------------------
    // Common Settings
    // -----------------------------------------------------------------------

    public PkgUnixPackage debug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp )
        throws IOException
    {
        prototype = workingDirectory.resolve( relativePath( "prototype" ) );
        pkginfoFile = workingDirectory.resolve( relativePath( "pkginfo" ) );

        Directory defaultDirectory = directory( BASE, fromDateFields( new Date( 0 ) ), defaultDirectoryAttributes );
        DirectoryEntry directoryEntry = new DirectoryEntry( Option.<String>none(), defaultDirectory );

        prototypeFile = new PrototypeFile( directoryEntry );
    }

    // -----------------------------------------------------------------------
    // Pkg Specific Settings
    // -----------------------------------------------------------------------

    public PkgUnixPackage pkgParameters( List<String> classes, Option<String> category )
    {
        pkginfo = pkginfo.
            category( category.orSome( pkginfo.category ) ).
            classes( classes );

        return this;
    }

    public PkgPreparedPackage prepare( ScriptUtil.Strategy strategy )
        throws Exception
    {
        workingDirectory.mkdir();

        // -----------------------------------------------------------------------
        // Validate that the prototype looks sane
        // -----------------------------------------------------------------------

        // TODO: This should be more configurable
        RelativePath[] specialPaths =
            new RelativePath[]{ BASE, relativePath( "/etc" ), relativePath( "/etc/opt" ), relativePath( "/opt" ),
                relativePath( "/usr" ), relativePath( "/var" ), relativePath( "/var/opt" ), };

        // TODO: This should use setDirectoryAttributes
        for ( RelativePath specialPath : specialPaths )
        {
            if ( prototypeFile.hasPath( specialPath ) )
            {
                // TODO: this should come from a common time object so that all "now" timestamps are the same
                prototypeFile.addDirectory( directory( specialPath, new LocalDateTime(), EMPTY ) );
            }
        }

        // -----------------------------------------------------------------------
        // The shit
        // -----------------------------------------------------------------------

        ScriptUtil.Result result = scriptUtil.
            createExecution( classifier.orSome( "default" ), "pkg", getScripts(), workingDirectory.file, strategy ).
            execute();

        LineStreamUtil.toFile( pkginfo.toList(), pkginfoFile.file );

        prototypeFile.addIFileIf( pkginfoFile.file, "pkginfo" );
        prototypeFile.addIFileIf( result.preInstall, "preinstall" );
        prototypeFile.addIFileIf( result.postInstall, "postinstall" );
        prototypeFile.addIFileIf( result.preRemove, "preremove" );
        prototypeFile.addIFileIf( result.postRemove, "postremove" );
        for ( File file : result.customScripts )
        {
            prototypeFile.addIFileIf( file );
        }

        for ( IoEffect operation : operations )
        {
            operation.run();
        }

        LineStreamUtil.toFile( prototypeFile, prototype.file );

        return new PkgPreparedPackage();
    }

    public class PkgPreparedPackage
        extends UnixPackage.PreparedPackage
    {
        public void packageToFile( File packageFile )
            throws Exception
        {
            String pkg = pkginfo.getPkgName( pkginfoFile.file );

            new PkgmkCommand().
                setDebug( debug ).
                setOverwrite( true ).
                setDevice( workingDirectory.file ).
                setPrototype( prototype.file ).
                execute();

            new PkgtransCommand().
                setDebug( debug ).
                setAsDatastream( true ).
                setOverwrite( true ).
                execute( workingDirectory.file, packageFile, pkg );
        }
    }

    public static String getPkgVersion( PackageVersion v )
    {
        String version = v.version;

        if ( v.snapshot )
        {
            version += "-" + v.timestamp;
        }

        if ( v.revision.isSome() )
        {
            version += "-" + v.revision.some();
        }

        return version;
    }

    public void addDirectory( UnixFsObject.Directory directory )
        throws IOException
    {
        prototypeFile.addDirectory( directory );
    }

    public void addFile( Fs<?> fromFile, RegularFile file )
        throws IOException
    {
        prototypeFile.addFile( fromFile( fromFile, file ), file );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        prototypeFile.addSymlink( symlink );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        prototypeFile.apply( f );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public LocalFs fromFile( final Fs<?> fromFile, final UnixFsObject.RegularFile file )
    {
        // If it is a file on the local file system, just point the entry in the prototype file to it
        if ( fromFile instanceof LocalFs && !needsFiltering( file ) )
        {
            return (LocalFs) fromFile;
        }

        // Creates a file under the working directory that should match the destination path
        final LocalFs tmpFile = workingDirectory.resolve( relativePath( "assembly" ).add( file.path ) );

        operations = operations.cons( new IoEffect()
        {
            public void run()
                throws IOException
            {
                OutputStream outputStream = null;
                try
                {
                    P2<InputStream, Option<Long>> p2 =
                        filtersAndLineEndingHandingInputStream( file, fromFile.inputStream() );

                    tmpFile.parent().mkdir();
                    tmpFile.copyFrom( fromFile, p2._1() );
                }
                finally
                {
                    IOUtil.close( outputStream );
                }
            }
        } );

        return tmpFile;
    }

    @Override
    public String getArchitecture() {
        return "";
    }

    @Override
    public String getOutputFileName() {
        return ""; //TODO Add name
    }
}
