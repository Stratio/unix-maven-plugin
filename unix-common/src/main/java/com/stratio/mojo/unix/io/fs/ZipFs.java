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
import java.util.zip.*;

public class ZipFs
    implements Fs<ZipFs>
{
    public final ZipFsRoot root;

    public final ZipEntry entry;

    public final RelativePath relativePath;

    public ZipFs( ZipFsRoot root, ZipEntry entry, RelativePath relativePath )
    {
        this.root = root;
        this.entry = entry;
        this.relativePath = relativePath;
    }

    public void close()
        throws IOException
    {
        root.close();
    }

    public boolean exists()
    {
        return entry != null;
    }

    public boolean isFile()
    {
        return exists() && !entry.isDirectory();
    }

    public boolean isDirectory()
    {
        return exists() && entry.isDirectory();
    }

    public LocalDateTime lastModified()
    {
        return new LocalDateTime( entry.getTime() );
    }

    public long size()
    {
        return entry.getSize();
    }

    public ZipFs resolve( RelativePath relativePath )
    {
        return root.resolve( this.relativePath.add( relativePath ) );
    }

    public File basedir()
    {
        return root.file;
    }

    public RelativePath relativePath()
    {
        return relativePath;
    }

    public String absolutePath()
    {
        return root.absolutePath( relativePath );
    }

    public InputStream inputStream()
        throws IOException
    {
        return root.zipFile.getInputStream( entry );
    }

    public Iterable<ZipFs> find( IncludeExcludeFilter filter )
        throws IOException
    {
        throw new IOException( "Not supported" );
    }

    public void mkdir()
        throws IOException
    {
        throw new IOException( "Not supported" );
    }

    public void copyFrom( Fs<?> from )
        throws IOException
    {
        throw new IOException( "Not supported" );
    }

    public void copyFrom( Fs from, InputStream is )
        throws IOException
    {
        throw new RuntimeException( "Not supported" );
    }
}
