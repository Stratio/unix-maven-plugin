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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ReadableArFile
    extends ArFile
{
    private InputStream inputStream;
    private ArFileInputStream fileInputStream;
    private long left;

    public ReadableArFile( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public InputStream open()
    {
        if ( inputStream == null )
        {
            throw new RuntimeException( "This file has already been read" );
        }

        return fileInputStream = new ArFileInputStream();
    }

    void close()
    {
        // If the file havent been opened, skip the bytes
        if ( fileInputStream == null )
        {
            fileInputStream = new ArFileInputStream();
        }

        IOUtil.close( fileInputStream );
    }

    private class ArFileInputStream
        extends InputStream
    {
        private InputStream inputStream;

        public ArFileInputStream()
        {
            this.inputStream = ReadableArFile.this.inputStream;
            ReadableArFile.this.inputStream = null;
            left = size;
        }

        public int read()
            throws IOException
        {
            if ( left <= 0 )
            {
                return -1;
            }

            int i = inputStream.read();

            if ( i == -1 )
            {
                return -1;
            }

            left--;
            return i;
        }

        public int read( byte b[], int off, int len )
            throws IOException
        {
            if ( left <= 0 )
            {
                return -1;
            }

            if ( len > left )
            {
                len = (int) left;
            }

            int i = inputStream.read( b, off, len );

            left -= i;

            return i;
        }

        public long skip( long n )
            throws IOException
        {
            throw new IOException( "Not supported" );
        }

        public int available()
            throws IOException
        {
            return (int) left;
        }

        public void close()
            throws IOException
        {
            // TODO: Make sure that we read out all the bytes from the underlying input stream
            if ( left != 0 )
            {
                ArUtil.skipBytes( inputStream, left );
            }

            // Read the extra pad byte if size is odd
            if ( size % 2 == 1 )
            {
                ArUtil.skipBytes( inputStream, 1 );
            }
        }

        public synchronized void mark( int readlimit )
        {
            throw new RuntimeException( "Not supported" );
        }

        public synchronized void reset()
            throws IOException
        {
            throw new RuntimeException( "Not supported" );
        }

        public boolean markSupported()
        {
            return false;
        }
    }
}
