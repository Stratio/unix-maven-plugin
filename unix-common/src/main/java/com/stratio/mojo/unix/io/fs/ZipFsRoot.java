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
 package com.stratio.mojo.unix.io.fs;

import com.stratio.mojo.unix.io.*;
import com.stratio.mojo.unix.util.*;
import org.joda.time.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipFsRoot
    implements Fs<ZipFs>
{
    final File file;

    final ZipFile zipFile;

    private final SortedMap<RelativePath, ZipFs> entries = new TreeMap<RelativePath, ZipFs>( RelativePath.comparator );

    /**
     * This scans the entire ZIP file which may or may not be an issue. Most likely the file will be accessed so parts
     * of it will have to be read, but it might be possible to skip parts of the file.
     */
    public ZipFsRoot( File file )
        throws IOException
    {
        this.file = file;
        zipFile = new ZipFile( file );

        Enumeration<? extends ZipEntry> en = zipFile.entries();

        while ( en.hasMoreElements() )
        {
            ZipEntry entry = en.nextElement();
            RelativePath path = RelativePath.relativePath( entry.getName() );
            entries.put( path, new ZipFs( this, entry, path ) );
        }
    }

    public void close()
        throws IOException
    {
        zipFile.close();
    }

    public boolean exists()
    {
        return true;
    }

    public boolean isFile()
    {
        return false;
    }

    public boolean isDirectory()
    {
        return true;
    }

    public LocalDateTime lastModified()
    {
        return new LocalDateTime( file.lastModified() );
    }

    public long size()
    {
        return 0;
    }

    public synchronized ZipFs resolve( RelativePath relativePath )
    {
        ZipFs zipFs = entries.get( relativePath );

        if ( zipFs == null )
        {
            zipFs = new ZipFs( this, null, relativePath );
            entries.put( relativePath, zipFs );
        }

        return zipFs;
    }

    public File basedir()
    {
        return file;
    }

    public RelativePath relativePath()
    {
        return RelativePath.BASE;
    }

    public String absolutePath()
    {
        return absolutePath( RelativePath.BASE );
    }

    public InputStream inputStream()
        throws IOException
    {
        throw new RuntimeException( "Not supported" );
    }

    public Iterable<ZipFs> find( IncludeExcludeFilter filter )
        throws IOException
    {
        List<ZipFs> list = new ArrayList<ZipFs>(  );

        for ( Map.Entry<RelativePath, ZipFs> entry : entries.entrySet() )
        {
            ZipFs value = entry.getValue();

            if ( !value.exists() )
            {
                continue;
            }

            if ( !filter.matches( entry.getKey() ) )
            {
                continue;
            }

            list.add( value );
        }

        return list;
    }

    public void mkdir()
        throws IOException
    {
        throw new RuntimeException( "Not supported" );
    }

    public void copyFrom( Fs<?> from )
        throws IOException
    {
        throw new RuntimeException( "Not supported" );
    }

    public void copyFrom( Fs from, InputStream is )
        throws IOException
    {
        throw new RuntimeException( "Not supported" );
    }

    public String absolutePath( RelativePath relativePath )
    {
        return file + "!/" + relativePath.string;
    }
}
