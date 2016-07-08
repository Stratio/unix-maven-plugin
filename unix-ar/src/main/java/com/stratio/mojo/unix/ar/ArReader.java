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
 package com.stratio.mojo.unix.ar;

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

import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ArReader
    implements Closeable, Iterable<ReadableArFile>
{
    private InputStream is;

    public ArReader( File file )
        throws IOException
    {
        is = new FileInputStream( file );

        String magic = new String( ArUtil.readBytes( is, 8 ), ArUtil.US_ASCII );

        if ( !magic.equals( ArUtil.AR_ARCHIVE_MAGIC ) )
        {
            throw new InvalidArchiveMagicException();
        }
    }

    public Iterator<ReadableArFile> iterator()
    {
        return new ArFileIterator();
    }

    public void close()
    {
        IOUtil.close( is );
    }

    public ReadableArFile readFile()
        throws IOException
    {
        byte[] bytes = ArUtil.readBytes( is, 60 );

        if ( bytes == null )
        {
            return null;
        }

        ReadableArFile arFile = new ReadableArFile( is );
        arFile.name = ArUtil.convertString( bytes, 0, 16 );
        arFile.lastModified = Long.parseLong( ArUtil.convertString( bytes, 16, 12 ) );
        arFile.ownerId = Integer.parseInt( ArUtil.convertString( bytes, 28, 6 ) );
        arFile.groupId = Integer.parseInt( ArUtil.convertString( bytes, 34, 6 ) );
        arFile.mode = Integer.parseInt( ArUtil.convertString( bytes, 40, 8 ), 8 );
        arFile.size = Long.parseLong( ArUtil.convertString( bytes, 48, 10 ) );

        if ( arFile.name.endsWith( "/" ) )
        {
            arFile.name = arFile.name.substring( 0, arFile.name.length() - 1 );
        }

        String fileMagic = ArUtil.convertString( bytes, 58, 2 );

        if ( !fileMagic.equals( ArUtil.AR_FILE_MAGIC ) )
        {
            throw new InvalidFileMagicException();
        }

        return arFile;
    }

    public class ArFileIterator
        implements Iterator<ReadableArFile>
    {

        private boolean used;
        private boolean atEnd;
        private ReadableArFile file;

        public boolean hasNext()
        {
            updateNext();
            return file != null;
        }

        public ReadableArFile next()
        {
            updateNext();

            if ( file == null )
            {
                throw new NoSuchElementException();
            }

            used = true;

            return file;
        }

        private void updateNext()
        {
            try
            {
                if ( used )
                {
                    file.close();
                    file = null;
                    used = false;
                }

                // There already is an element ready
                if ( file != null )
                {
                    return;
                }

                // If we're at the end, don't call readFile() anymore as that will throw an IOException
                if ( atEnd )
                {
                    return;
                }

                file = readFile();
                atEnd = file == null;

                if ( atEnd )
                {
                    close();
                }
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException( "Not implemented." );
        }
    }
}
