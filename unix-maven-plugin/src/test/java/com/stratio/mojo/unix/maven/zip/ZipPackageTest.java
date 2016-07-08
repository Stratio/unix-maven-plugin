/**
 * Copyright (C) 2003 Stratio (http://stratio.com)
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
 package com.stratio.mojo.unix.maven.zip;

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

import fj.*;
import fj.data.*;
import fj.data.List;
import static fj.data.List.*;
import static fj.data.Option.*;
import static java.util.regex.Pattern.*;
import junit.framework.*;
import org.apache.commons.compress.archivers.zip.*;
import org.apache.maven.plugin.logging.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.UnixFileMode._0777;
import static com.stratio.mojo.unix.UnixFsObject.*;
import com.stratio.mojo.unix.core.*;
import com.stratio.mojo.unix.io.LineEnding;
import com.stratio.mojo.unix.io.fs.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.ScriptUtil.Strategy.SINGLE;
import org.joda.time.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ZipPackageTest
    extends TestCase
{
    private final TestUtil testUtil = new TestUtil( getClass() );

    private final Charset charset = Charset.forName( "utf-8" );

    LocalDateTime dirsTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 10 );

    LocalDateTime dirsBarTxtTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 10 );

    LocalDateTime fileTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 48 );

    LocalDateTime fileFooTxtTimestamp = new LocalDateTime( 2012, 8, 19, 10, 34, 10 );

    // Zip has a resolution of two seconds
    LocalDateTime timestamp = new LocalDateTime( 2012, 1, 2, 3, 4, 6 );

    @SuppressWarnings( "OctalInteger" )
    public void testBasic()
        throws Exception
    {
        File zip1 = testUtil.getTestFile( "src/test/resources/zip/zip-1" );
        File zip = testUtil.getTestFile( "target/zip/zip-1/test.zip" );
        if ( !zip.getParentFile().isDirectory() )
        {
            assertTrue( zip.getParentFile().mkdirs() );
        }

        LocalFs basedir = new LocalFs( zip1 );

        ZipUnixPackage zipPackage = new ZipUnixPackage( new SystemStreamLog() );

        zipPackage.beforeAssembly( EMPTY.mode( UnixFileMode._0755 ), timestamp );

        assertTrue( basedir.isDirectory() );

        // Git set the timestamp of file objects
        assertTrue( new File( zip1, "dirs" ).setLastModified( dirsTimestamp.toDateTime().getMillis() ) );
        assertTrue( new File( zip1, "dirs/bar.txt" ).setLastModified( dirsBarTxtTimestamp.toDateTime().getMillis() ) );
        assertTrue( new File( zip1, "file" ).setLastModified( fileTimestamp.toDateTime().getMillis() ) );
        assertTrue( new File( zip1, "file/foo.txt" ).setLastModified( fileFooTxtTimestamp.toDateTime().getMillis() ) );

        Replacer replacer = new Replacer( "@bar@", "awesome" );

        new CreateDirectoriesOperation( timestamp, new String[]{ "/opt/hudson" }, EMPTY ).
            perform( zipPackage );

        new CopyDirectoryOperation( basedir, relativePath( "" ), List.<String>nil(), List.<String>nil(),
                                    Option.<P2<String, String>>none(), EMPTY, EMPTY ).
            perform( zipPackage );

        new CopyFileOperation( EMPTY, basedir.resolve( "file/foo.txt" ), relativePath( "/file/foo.txt" ) ).
            perform( zipPackage );

        new SymlinkOperation( relativePath( "/var/log/hudson" ), "/var/opt/hudson/log", Option.<String>none(), Option.<String>none() ).
            perform( zipPackage );

        new FilterFilesOperation( single( "dirs/**" ), List.<String>nil(), single( replacer ), LineEnding.unix ).
            perform( zipPackage );

        UnixFileMode fileMode = UnixFileMode.fromInt( 0600 );
        UnixFileMode dirMode = _0777;

        FileAttributes fileAttributes = new FileAttributes( "root", "root", fileMode );
        FileAttributes directoryAttributes = new FileAttributes( "root", "root", dirMode );
        new SetAttributesOperation( BASE, single( "**/*" ), List.<String>nil(), some( fileAttributes ),
                                    some( directoryAttributes ) ).
            perform( zipPackage );

        zipPackage.
            prepare( SINGLE ).
            packageToFile( zip );

        ZipFile file = new ZipFile( zip );
        Enumeration<ZipArchiveEntry> enumeration = file.getEntriesInPhysicalOrder();
        assertDirectory( enumeration.nextElement(), "./dirs/", dirsTimestamp );
        // Is it really correct that filtered files should retain the old timestamp?
        assertFile( file, enumeration.nextElement(), "./dirs/bar.txt", 8, dirsBarTxtTimestamp, "awesome\n", fileMode );
        assertDirectory( enumeration.nextElement(), "./file/", fileTimestamp );
        assertFile( file, enumeration.nextElement(), "./file/foo.txt", 6, fileFooTxtTimestamp, "@foo@\n", fileMode );
        assertDirectory( enumeration.nextElement(), "./opt/", timestamp );
        assertDirectory( enumeration.nextElement(), "./opt/hudson/", timestamp );
        assertFalse( enumeration.hasMoreElements() );
        file.close();
    }

    private void assertDirectory( ZipArchiveEntry entry, String name, LocalDateTime time )
        throws IOException
    {
        assertNotNull( name, entry );
        assertTrue( name + " should be file", entry.isDirectory() );
        assertEquals( name + ", name", name, entry.getName() );
        assertEquals( name + ", timestamp", time, new LocalDateTime( entry.getTime() ) );
    }

    private void assertFile( ZipFile file, ZipArchiveEntry entry, String name, int size, LocalDateTime time,
                             String content, UnixFileMode mode )
        throws IOException
    {
        InputStream in = file.getInputStream( entry );

        assertFalse( name + " should be file", entry.isDirectory() );
        assertEquals( name + ", name", name, entry.getName() );
        assertEquals( name + ", timestamp", time, new LocalDateTime( entry.getTime() ) );
        // wtf: http://vimalathithen.blogspot.no/2006/06/using-zipentrygetsize.html
        // assertEquals( name + ", size", size, entry.getSize() );

        byte[] bytes = new byte[1000];
        assertEquals( size, in.read( bytes, 0, bytes.length ) );
        assertEquals( content, new String( bytes, 0, size, charset ) );

        assertEquals( ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform() );
        assertEquals( name + ", mode", mode.toString(), UnixFileMode.fromInt( entry.getUnixMode() ).toString() );
    }
}
