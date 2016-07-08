/**
 * Copyright (C) 2008 Stratio (http://stratio.com)
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
 package com.stratio.mojo.unix.sysvpkg.prototype;

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
import static fj.data.List.*;
import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.PackageFileSystem.*;

import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PrototypeFile
    implements LineProducer
{
    public static final LocalDateTime START_OF_TIME = new LocalDateTime( 0, DateTimeZone.UTC );

    private List<String> iFiles = nil();

    private PackageFileSystem<PrototypeEntry> fileSystem;

    public PrototypeFile( DirectoryEntry defaultDirectory )
    {
        Validate.validateNotNull( defaultDirectory );

        UnixFsObject.Directory root = UnixFsObject.directory( BASE, START_OF_TIME, EMPTY );
        DirectoryEntry rootEntry = new DirectoryEntry( Option.<String>none(), root );

        fileSystem = create( rootEntry, defaultDirectory );
    }

    public void addIFileIf( File file, String name )
    {
        if ( file == null || !file.canRead() )
        {
            return;
        }

        iFiles = iFiles.cons( "i " + name + "=" + file.getAbsolutePath() );
    }

    public void addIFileIf( Option<File> file, String name )
    {
        if ( file.isNone() )
        {
            return;
        }

        iFiles = iFiles.cons( "i " + name + "=" + file.some().getAbsolutePath() );
    }

    public void addIFileIf( File file )
    {
        if ( file == null || !file.canRead() )
        {
            return;
        }

        addIFileIf( file, file.getName() );
    }

    public boolean hasPath( RelativePath path )
    {
        return fileSystem.hasPath( path );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        fileSystem = fileSystem.addDirectory( new DirectoryEntry( findClassTag.f( directory.attributes ), directory ) );
    }

    public void addFile( LocalFs fromFile, UnixFsObject.RegularFile file )
    {
        fileSystem = fileSystem.addFile( new FileEntry( findClassTag.f( file.attributes ), some( false ), file, some( fromFile.file ) ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
    {
        fileSystem = fileSystem.addSymlink( new SymlinkEntry( findClassTag.f( symlink.attributes ), symlink ) );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        fileSystem = fileSystem.apply( f );
    }

    public void streamTo( final LineStreamWriter stream )
    {
        stream.
            addAllLines( iFiles.reverse() );

        for ( PackageFileSystemObject<PrototypeEntry> object : fileSystem.prettify().toList().filter( filterRoot ) )
        {
            object.getExtension().streamTo( stream );
        }
    }

    public static Option<String> findClassTag( FileAttributes fileAttributes )
    {
        String prefix = "class:";

        for ( String tag : fileAttributes.tags )
        {
            if ( tag.startsWith( prefix ) )
            {
                return some( tag.substring( prefix.length() ) );
            }
        }

        return none();
    }

    public static F<FileAttributes, Option<String>> findClassTag = new F<FileAttributes, Option<String>>()
    {
        public Option<String> f( FileAttributes fileAttributes )
        {
            return findClassTag( fileAttributes );
        }
    };

    private F<PackageFileSystemObject<PrototypeEntry>, Boolean> filterRoot = new F<PackageFileSystemObject<PrototypeEntry>, Boolean>()
    {
        public Boolean f( PackageFileSystemObject<PrototypeEntry> fileSystemObject )
        {
            return !fileSystemObject.getUnixFsObject().path.isBase();
        }
    };
}
