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
import java.nio.*;
import java.nio.charset.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
class ArWriter
{
    private OutputStream output;

    static final Charset charset;

    public static final String BLANKS = "               ";

    static
    {
        charset = Charset.forName( ArUtil.US_ASCII );
    }

    public ArWriter( File file )
        throws IOException
    {
        output = new FileOutputStream( file );

        output.write( toBytes( ArUtil.AR_ARCHIVE_MAGIC ) );
        output.flush();
    }

    public void add( ArFile arFile )
        throws IOException
    {
        output.write( toBytes( arFile.name, 16 ) );
        output.write( toBytes( Long.toString( arFile.lastModified ), 12 ) );
        output.write( toBytes( Integer.toString( arFile.ownerId ), 6 ) );
        output.write( toBytes( Integer.toString( arFile.groupId ), 6 ) );
        output.write( toBytes( Integer.toOctalString( arFile.mode ), 8 ) );
        output.write( toBytes( Long.toString( arFile.size ), 10 ) );
        output.write( toBytes( ArUtil.AR_FILE_MAGIC ) );
        output.flush();

        InputStream is = null;
        try
        {
            is = new FileInputStream( arFile.file );
            ArUtil.copy( is, output, 8192 );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    private byte[] toBytes( String value )
    {
        return toBytes( value, value.length() );
    }

    private byte[] toBytes( String value, int size )
    {
        String s = value;

        if ( s.length() > size )
        {
            throw new RuntimeException( "Internal error. Field size (" + s.length() + ") > max size (" + size + ")" );
        }

        if ( s.length() < size )
        {
            s += BLANKS.substring( 0, size - s.length() );
        }

        ByteBuffer byteBuffer = charset.encode( s );
        return byteBuffer.array();
    }

    public void close()
        throws IOException
    {
        output.close();
    }
}
