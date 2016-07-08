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
 package com.stratio.mojo.unix.deb;

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

import fj.data.*;
import static fj.data.Option.*;
import org.apache.commons.compress.archivers.tar.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import com.stratio.mojo.unix.ar.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import org.joda.time.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DpkgDebTool
{
    public static List<UnixFsObject> contents( File file )
        throws IOException
    {
        ArReader archive = null;
        try
        {
            archive = Ar.read( file );

            for ( ReadableArFile arFile : archive )
            {
                if ( arFile.getName().startsWith( "data." ) )
                {
                    return processCompressedTar( arFile );
                }
            }

            throw new IOException( "Could not find data file in: " + file.getAbsolutePath() );
        }
        finally
        {
            ArUtil.close( archive );
        }
    }

    private static List<UnixFsObject> processCompressedTar( ReadableArFile arFile )
        throws IOException
    {
        if ( arFile.getName().endsWith( ".tar.gz" ) )
        {
            // Don't worry about closing the stream, that will be taken care of by the iterator.

            return process( new GZIPInputStream( arFile.open(), 1024 * 128 ) );
        }
        else
        {
            throw new IOException( "Unsupported compression format of data tar file: " + arFile.getName() );
        }
    }

    private static List<UnixFsObject> process( InputStream is )
        throws IOException
    {
        TarArchiveInputStream tarInputStream = new TarArchiveInputStream( is );

        List<UnixFsObject> objects = new ArrayList<UnixFsObject>();

        TarArchiveEntry entry = (TarArchiveEntry) tarInputStream.getNextEntry();

        while ( entry != null )
        {
            Option<UnixFileMode> mode = some( UnixFileMode.fromInt( entry.getMode() ) );
            FileAttributes attributes =
                new FileAttributes( some( entry.getUserName() ), some( entry.getGroupName() ), mode );
            RelativePath path = relativePath( entry.getName() );
            LocalDateTime lastModified = LocalDateTime.fromDateFields( entry.getModTime() );

            UnixFsObject object;

            if ( entry.isDirectory() )
            {
                object = directory( path, lastModified, attributes );
            }
            else if ( entry.isSymbolicLink() )
            {
                object = symlink( path, lastModified, some( entry.getUserName() ), some( entry.getGroupName() ),
                                  entry.getLinkName() );
            }
            else if ( entry.isFile() )
            {
                object = regularFile( path, lastModified, entry.getSize(), attributes );
            }
            else
            {
                throw new IOException( "Unsupported link type: name=" + entry.getName() );
            }

            objects.add( object );

            entry = (TarArchiveEntry) tarInputStream.getNextEntry();
        }

        return objects;
    }
}
