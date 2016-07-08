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

import com.stratio.mojo.unix.io.IncludeExcludeFilter;
import com.stratio.mojo.unix.util.RelativePath;
import org.codehaus.plexus.util.*;
import org.joda.time.LocalDateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.stratio.mojo.unix.util.RelativePath.relativePathFromFiles;

/**
 * These can in theory be cached in something with very fast lookup to conserve objects.
 */
public class LocalFs
    implements Fs<LocalFs>
{
    public final File basedir;

    public final RelativePath relativePath;

    public final File file;

    public LocalFs( File basedir )
    {
        this.basedir = basedir.getAbsoluteFile();
        this.relativePath = RelativePath.BASE;
        this.file = this.basedir;
    }

    private LocalFs( File basedir, RelativePath relativePath, File file )
    {
        this.basedir = basedir.getAbsoluteFile();
        this.relativePath = relativePath;
        this.file = file.getAbsoluteFile();
    }

    public void close()
        throws IOException
    {
    }

    public boolean exists()
    {
        return file.exists();
    }

    public boolean isFile()
    {
        return file.isFile();
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    public LocalDateTime lastModified()
    {
        return new LocalDateTime( file.lastModified() );
    }

    public long size()
    {
        return file.length();
    }

    public LocalFs resolve( String relativePath )
    {
        return new LocalFs( basedir, this.relativePath.add( relativePath ), new File( file, relativePath ) );
    }

    public LocalFs resolve( RelativePath relativePath )
    {
        return resolve( relativePath.string );
    }

    public File basedir()
    {
        return basedir;
    }

    public RelativePath relativePath()
    {
        return relativePath;
    }

    public String absolutePath()
    {
        return file.getAbsolutePath();
    }

    public Iterable<LocalFs> find( IncludeExcludeFilter filter )
        throws IOException
    {
        return find( filter, false );
    }

    public Iterable<LocalFs> find( IncludeExcludeFilter filter, boolean filesOnly )
        throws IOException
    {
        ArrayList<LocalFs> list = new ArrayList<LocalFs>();

        if ( !filesOnly )
        {
            list.add( this );
        }

        find( file, list, filter, filesOnly );

        return list;
    }

    public void mkdir()
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return;
        }

        if ( !file.mkdirs() )
        {
            throw new IOException( "Unable to create directory: " + file );
        }
    }

    public void copyFrom( Fs from )
        throws IOException
    {
        copyFrom( from, from.inputStream() );
    }

    public void copyFrom( Fs from, InputStream is )
        throws IOException
    {
        parent().mkdir();

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream( file );
            IOUtil.copy( is, os );
        }
        finally
        {
            IOUtil.close( os );
            IOUtil.close( is );
        }

        if ( !file.setLastModified( from.lastModified().toDateTime().toDate().getTime() ) )
        {
            throw new IOException( "Unable to set last modified on " + file.getAbsolutePath() );
        }
    }

    private void find( File directory, List<LocalFs> list, IncludeExcludeFilter filter, boolean filesOnly )
        throws IOException
    {
        if ( !directory.isDirectory() )
        {
            return;
        }

        File[] files = directory.listFiles();

        if ( files == null )
        {
            throw new IOException( "Unable to list contents: " + directory.getAbsolutePath() );
        }

        for ( File file : files )
        {
            RelativePath relativePath = relativePathFromFiles( this.file, file );

            if ( file.isFile() || file.isDirectory() && !filesOnly )
            {
                if ( filter.matches( relativePath ) )
                {
                    list.add( new LocalFs( basedir, relativePath, file ) );
                }
            }

            if ( file.isDirectory() )
            {
                find( file, list, filter, filesOnly );
            }
        }
    }

    public InputStream inputStream()
        throws FileNotFoundException
    {
        return new FileInputStream( file );
    }

    public LocalFs parent()
    {
        return new LocalFs( basedir, relativePath.parent(), file.getParentFile() );
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        LocalFs localFs = (LocalFs) o;

        return file.equals( localFs.file );
    }

    public int hashCode()
    {
        return file.hashCode();
    }

    public String toString()
    {
        return "LocalFs{baserdir=" + basedir + ", relativePath=" + relativePath + '}';
    }
}
