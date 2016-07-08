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
 package com.stratio.mojo.unix.maven.zip;

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
import static fj.Function.*;
import fj.data.*;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.maven.plugin.logging.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.BasicPackageFileSystemObject.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.PackageFileSystem.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.core.FsFileCollector.*;
import com.stratio.mojo.unix.io.*;
import com.stratio.mojo.unix.io.fs.Fs;
import com.stratio.mojo.unix.java.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import org.codehaus.plexus.util.*;
import static org.codehaus.plexus.util.IOUtil.*;
import org.joda.time.*;

import java.io.*;
import java.util.zip.*;

public class ZipUnixPackage
    extends UnixPackage<ZipUnixPackage, ZipUnixPackage.ZipPreparedPackage>
{
    private PackageFileSystem<F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>> fileSystem;

    private final Log log;

    public ZipUnixPackage(Log log)
    {
        super( "zip" );
        this.log = log;
    }

    public ZipUnixPackage parameters( PackageParameters parameters )
    {
        return this;
    }

    // -----------------------------------------------------------------------
    // FileCollector Implementation
    // -----------------------------------------------------------------------

    public void addDirectory( final Directory directory )
        throws IOException
    {
        fileSystem = fileSystem.addDirectory( directory( directory ) );
    }

    public void addFile( Fs<?> fromFile, RegularFile file )
        throws IOException
    {
        fileSystem = fileSystem.addFile( file( fromFile, file ) );
    }

    public void addSymlink( Symlink symlink )
        throws IOException
    {
        log.warn( "Symlinks are not supported in ZIP files." );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        fileSystem = fileSystem.apply( f );
    }

    // -----------------------------------------------------------------------
    // UnixPackage Implementation
    // -----------------------------------------------------------------------

    public void beforeAssembly( FileAttributes defaultDirectoryAttributes, LocalDateTime timestamp )
        throws IOException
    {
        Directory rootDirectory = Directory.directory( BASE, timestamp, EMPTY );

        fileSystem = create( directory( rootDirectory ), directory( rootDirectory ) );
    }

    public ZipPreparedPackage prepare( ScriptUtil.Strategy strategy )
        throws Exception
    {
        return new ZipPreparedPackage();
    }

    public class ZipPreparedPackage
        extends UnixPackage.PreparedPackage
    {
        public void packageToFile( File packageFile )
            throws Exception
        {
            F2<RelativePath, PackageFileSystemObject<F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>>, Boolean>
                pathFilter = pathFilter();

            fileSystem = fileSystem.prettify();

            Stream<PackageFileSystemObject<F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>>> items = fileSystem.
                toList().
                filter( compose( BooleanF.invert, curry( pathFilter, BASE ) ) );

            ZipArchiveOutputStream zos = null;
            try
            {
                zos = new ZipArchiveOutputStream( packageFile );
                zos.setLevel( Deflater.BEST_COMPRESSION );

                for ( PackageFileSystemObject<F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>> fileSystemObject : items )
                {
                    fileSystemObject.getExtension().f( fileSystemObject.getUnixFsObject(), zos ).run();
                }
            }
            finally
            {
                IOUtil.close( zos );
            }
        }
    }

    public static <A> F2<RelativePath, PackageFileSystemObject<A>, Boolean> pathFilter()
    {
        return new F2<RelativePath, PackageFileSystemObject<A>, Boolean>()
        {
            public Boolean f( RelativePath path, PackageFileSystemObject object )
            {
                return object.getUnixFsObject().path.equals( path );
            }
        };
    }

    private BasicPackageFileSystemObject<F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>> directory( Directory directory )
    {
        F2<UnixFsObject, ZipArchiveOutputStream, IoEffect> f = new F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>()
        {
            public IoEffect f( final UnixFsObject file, final ZipArchiveOutputStream zos )
            {
                return new IoEffect()
                {
                    public void run()
                        throws IOException
                    {
                        String path = file.path.isBase() ? "." : file.path.asAbsolutePath( "./" ) + "/";

                        ZipArchiveEntry entry = new ZipArchiveEntry( path );
                        entry.setSize( file.size );
                        entry.setTime( file.lastModified.toDateTime().getMillis() );
                        if ( file.attributes.mode.isSome() )
                        {
                            entry.setUnixMode( file.attributes.mode.some().toInt() );
                        }
                        zos.putArchiveEntry( entry );
                        zos.closeArchiveEntry();
                    }
                };
            }
        };

        return basicPackageFSO( directory, f );
    }

    private BasicPackageFileSystemObject<F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>> file( final Fs<?> fromFile,
                                                                                            UnixFsObject file )
    {
        F2<UnixFsObject, ZipArchiveOutputStream, IoEffect> f = new F2<UnixFsObject, ZipArchiveOutputStream, IoEffect>()
        {
            public IoEffect f( final UnixFsObject file, final ZipArchiveOutputStream zos )
            {
                return new IoEffect()
                {
                    public void run()
                        throws IOException
                    {
                        InputStream inputStream = null;
                        BufferedReader reader = null;
                        try
                        {
                            P2<InputStream, Option<Long>> p =
                                filtersAndLineEndingHandingInputStream( file, fromFile.inputStream() );

                            inputStream = p._1();

                            long size = p._2().orSome( file.size );

                            ZipArchiveEntry entry = new ZipArchiveEntry( file.path.asAbsolutePath( "./" ) );
                            entry.setSize( size );
                            entry.setTime( file.lastModified.toDateTime().getMillis() );
                            if ( file.attributes.mode.isSome() )
                            {
                                entry.setUnixMode( file.attributes.mode.some().toInt() );
                            }

                            zos.putArchiveEntry( entry );
                            copy( inputStream, zos, 1024 * 128 );
                            zos.closeArchiveEntry();
                        }
                        finally
                        {
                            IOUtil.close( inputStream );
                            IOUtil.close( reader );
                        }
                    }
                };
            }
        };

        return basicPackageFSO( file, f );
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
