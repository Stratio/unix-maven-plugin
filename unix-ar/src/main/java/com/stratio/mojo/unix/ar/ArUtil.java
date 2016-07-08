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

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ArUtil
{
    public static final String LF = "\n";
    public static final String AR_ARCHIVE_MAGIC = "!<arch>" + LF;
    public static final String AR_FILE_MAGIC = "`\n";
    public static final String US_ASCII = "US-ASCII";

    public static String convertString( byte[] bytes, int start, int count )
        throws UnsupportedEncodingException
    {
        String s = new String( bytes, start, count, ArUtil.US_ASCII );

        int index = s.indexOf( ' ' );

        if ( index == -1 )
        {
            return s;
        }

        s = s.substring( 0, index );

        return s;
    }

    public static byte[] readBytes( InputStream is, long count )
        throws IOException
    {
        byte[] bytes = new byte[(int) count];

        int start = 0;

        do
        {
            int read = is.read( bytes, start, (int) count );

            // If we're at EOF, but trying to read the first set of bytes, return null
            if ( read == -1 )
            {
                if ( start > 0 )
                {
                    throw new EOFException();
                }

                return null;
            }

            start += read;
            count -= read;
        }
        while ( count > 0 );

        return bytes;
    }

    public static void skipBytes( InputStream is, long count )
        throws IOException
    {
        long left = count;

        do
        {
            long read = is.skip( left );

            left -= read;
        }
        while ( left > 0 );
    }

    public static void copy( InputStream input, OutputStream output, int bufferSize )
        throws IOException
    {
        final byte[] buffer = new byte[bufferSize];
        int n;
        while ( -1 != (n = input.read( buffer )) )
        {
            output.write( buffer, 0, n );
        }
    }

    public static void close( ArWriter writer )
    {
        try
        {
            writer.close();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    public static void close( Closeable reader )
    {
        if ( reader != null )
        {
            try
            {
                reader.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
    }
}
