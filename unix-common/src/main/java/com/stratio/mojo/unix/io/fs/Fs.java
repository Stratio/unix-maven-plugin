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

/**
 * Move the write methods into WrFs&lt;F extends Fs> extends Fs&lt;F>
 */
public interface Fs<F extends Fs>
    extends Closeable
{
    boolean exists();

    boolean isFile();

    boolean isDirectory();

    LocalDateTime lastModified();

    long size();

    F resolve( RelativePath relativePath );

    /**
     * The logical root of this file system. For local file system, it is the File it was created from, for archive
     * file system types it's the archive's File path.
     */
    File basedir();

    /**
     * The path inside the file system.
     */
    RelativePath relativePath();

    /**
     * For local files, File.getAbsolutePath. For archive files it's the archives path + "!" + relativePath
     */
    String absolutePath();

    InputStream inputStream()
        throws IOException;

    Iterable<F> find( IncludeExcludeFilter filter )
        throws IOException;

    void mkdir()
        throws IOException;

    void copyFrom( Fs<?> from )
        throws IOException;

    void copyFrom( Fs from, InputStream is )
        throws IOException;
}
