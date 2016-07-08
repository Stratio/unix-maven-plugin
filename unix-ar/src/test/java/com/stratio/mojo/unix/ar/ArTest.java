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

import junit.framework.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ArTest
    extends TestCase
{
    private File file1 = new File( System.getProperty( "basedir" ), "target/file1" );
    private String file1Contents = "Hello World";
    private File file2 = new File( System.getProperty( "basedir" ), "target/file2" );
    private File file3 = new File( System.getProperty( "basedir" ), "target/file3" );
    private String file3Contents = "Hello World, weee!"; // odd number of characters
    private File myAr = new File( System.getProperty( "basedir" ), "target/my.ar" );

    public ArTest()
        throws Exception
    {
        Writer writer = new OutputStreamWriter( new FileOutputStream( file1 ), "UTF-16" );
        writer.write( file1Contents );
        writer.close();

        writer = new OutputStreamWriter( new FileOutputStream( file2 ), "UTF-16" );
        writer.write( "I'm going to be skipped!" );
        writer.close();

        writer = new OutputStreamWriter( new FileOutputStream( file3 ), "UTF-16" );
        writer.write( file3Contents );
        writer.close();
    }

    public void testAr()
        throws Exception
    {
        ArWriterTest.writeFiles();

        Ar.create().
            addFile( file1 ).withUid( 10 ).withGid( 100 ).done().
            addFileDone( file2 ).
            addFileDone( file3 ).
            storeToFile( myAr );

        assertTrue( myAr.canRead() );
        assertEquals( 8 +
            60 + file1.length() +
            60 + file2.length(),
            60 + file3.length(),
            myAr.length() );

        ArReader reader = Ar.read( myAr );
        Iterator it = reader.iterator();

        ReadableArFile arFile;

        assertTrue( it.hasNext() );
        assertTrue( it.hasNext() );
        assertTrue( it.hasNext() );

        arFile = (ReadableArFile) it.next();
        assertNotNull( arFile );
        assertEquals( file1.getName(), arFile.name );
        assertEquals( file1.lastModified() / 1000, arFile.lastModified );
        assertEquals( 10, arFile.ownerId );
        assertEquals( 100, arFile.groupId );
        assertEquals( 420, arFile.mode );
        assertEquals( file1.length(), arFile.size );
        assertOpenable( file1, arFile, file1Contents );

        assertTrue( it.hasNext() );
        assertTrue( it.hasNext() );

        it.next();

        assertTrue( it.hasNext() );
        assertTrue( it.hasNext() );

        arFile = (ReadableArFile) it.next();
        assertNotNull( arFile );
        assertEquals( file3.getName(), arFile.name );
        assertEquals( file3.lastModified() / 1000, arFile.lastModified );
        assertEquals( 0, arFile.ownerId );
        assertEquals( 0, arFile.groupId );
        assertEquals( 420, arFile.mode );
        assertEquals( file3.length(), arFile.size );
        assertOpenable( file3, arFile, file3Contents );

        assertFalse( it.hasNext() );
        assertFalse( it.hasNext() );
        assertFalse( it.hasNext() );
    }

    private void assertOpenable( File file, ReadableArFile arFile, String contents )
        throws IOException
    {
        InputStream is = arFile.open();
        byte[] bytes = IOUtil.toByteArray( is );

        assertEquals( "File length vs bytes read.", file.length(), bytes.length );
        assertEquals( contents, new String( bytes, "UTF-16" ) );
        is.close();
        is.close();

        try
        {
            arFile.open();
            fail( "Expected IOException when opening a ReadableArFile twice." );
        }
        catch ( RuntimeException ex )
        {
            assertTrue( ex.getMessage().indexOf( "already been read" ) > 0 );
            // expected
        }
    }
}
