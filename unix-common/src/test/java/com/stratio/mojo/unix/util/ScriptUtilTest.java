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
 package com.stratio.mojo.unix.util;

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
import static com.stratio.mojo.unix.util.ScriptUtil.Strategy.*;
import static com.stratio.mojo.unix.util.UnixUtil.*;
import static org.codehaus.plexus.util.FileUtils.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ScriptUtilTest
    extends TestCase
{
    private final TestUtil testUtil = new TestUtil( this );

    private final ScriptUtil scriptUtil = new ScriptUtil( "preinstall", "postinstall", "preremove", "postremove" ).
        customScript( "i.daemon" ).
        customScript( "r.daemon" );

    public void testMultipleFormatsMultiplePackages()
        throws Exception
    {
        File basedir = testUtil.getTestFile( "src/test/resources/script-util/multiple-formats_multiple-packages" );

        File toDir = testUtil.getTestFile( "target/script-util/multiple-formats_multiple-packages" );

        if ( toDir.exists() )
        {
            deleteDirectory( toDir );
        }

        File toDirAPkg = new File( toDir, "a/pkg" );
        scriptUtil.createExecution( "a", "pkg", basedir, toDirAPkg, MULTIPLE ).execute();
        assertEquals( 3, toDirAPkg.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDirAPkg, "i.daemon" ) ) );
        assertEquals( "fee1c93e2d7cae61f2cc3d76fce920b6", md5String( new File( toDirAPkg, "postinstall" ) ) );
        assertEquals( "59c348f93f45929ded194ea42eefa965", md5String( new File( toDirAPkg, "preremove" ) ) );

        File toDirARpm = new File( toDir, "a/rpm" );
        scriptUtil.createExecution( "a", "rpm", basedir, toDirARpm, MULTIPLE ).execute();
        assertEquals( 3, toDirARpm.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDirARpm, "i.daemon" ) ) );
        assertEquals( "cb0385adef4b6e57ed311a21664cad8c", md5String( new File( toDirARpm, "postinstall" ) ) );
        assertEquals( "59c348f93f45929ded194ea42eefa965", md5String( new File( toDirARpm, "preremove" ) ) );

        File toDirBPkg = new File( toDir, "b/pkg" );
        scriptUtil.createExecution( "b", "pkg", basedir, toDirBPkg, MULTIPLE ).execute();
        assertEquals( 4, toDirBPkg.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDirBPkg, "i.daemon" ) ) );
        assertEquals( "da47470c91c9725960298b280ecf3747", md5String( new File( toDirBPkg, "postinstall" ) ) );
        assertEquals( "59c348f93f45929ded194ea42eefa965", md5String( new File( toDirBPkg, "preremove" ) ) );
        assertEquals( "747a5f6b0f8a23403f6e4530fe626afd", md5String( new File( toDirBPkg, "r.daemon" ) ) );

        File toDirBRpm = new File( toDir, "b/rpm" );
        scriptUtil.createExecution( "b", "rpm", basedir, toDirBRpm, MULTIPLE ).execute();
        assertEquals( 3, toDirBRpm.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDirBRpm, "i.daemon" ) ) );
        assertEquals( "389da7874f36033d95fe5a42fe84af60", md5String( new File( toDirBRpm, "postinstall" ) ) );
        assertEquals( "59c348f93f45929ded194ea42eefa965", md5String( new File( toDirBRpm, "preremove" ) ) );
    }

    public void testSingleFormatMultiplePackages()
        throws Exception
    {
        File basedir = testUtil.getTestFile( "src/test/resources/script-util/single-format_multiple-packages" );

        File toDir = testUtil.getTestFile( "target/script-util/single-format_multiple-packages" );

        if ( toDir.exists() )
        {
            deleteDirectory( toDir );
        }

        File toDirA = new File( toDir, "a" );
        scriptUtil.createExecution( "a", "foo", basedir, toDirA, SINGLE ).execute();
        assertEquals( 3, toDirA.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDirA, "i.daemon" ) ) );
        assertEquals( "d5d6723dd39bad4e6b5655b75e0807c6", md5String( new File( toDirA, "postinstall" ) ) );
        assertEquals( "59c348f93f45929ded194ea42eefa965", md5String( new File( toDirA, "preremove" ) ) );

        File toDirB = new File( toDir, "b" );
        scriptUtil.createExecution( "b", "foo", basedir, toDirB, SINGLE ).execute();
        assertEquals( 4, toDirB.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDirB, "i.daemon" ) ) );
        assertEquals( "579fb1243b7f290401e24a629e972cf6", md5String( new File( toDirB, "r.daemon" ) ) );
        assertEquals( "7ba283306d3f1fe058daeb328224bc50", md5String( new File( toDirB, "postinstall" ) ) );
        assertEquals( "59c348f93f45929ded194ea42eefa965", md5String( new File( toDirB, "preremove" ) ) );
    }

    public void testSingleFormatSinglePackage()
        throws Exception
    {
        File basedir = testUtil.getTestFile( "src/test/resources/script-util/single-format_single-package" );

        File toDir = testUtil.getTestFile( "target/script-util/single-format_single-package" );

        if ( toDir.exists() )
        {
            deleteDirectory( toDir );
        }

        scriptUtil.createExecution( "test", "foo", basedir, toDir, SINGLE ).execute();

        assertTrue( toDir.isDirectory() );

        assertEquals( 4, toDir.listFiles().length );
        assertEquals( "5e1202090e1b6c33e1caa956dcef2cc5", md5String( new File( toDir, "i.daemon" ) ) );
        assertEquals( "6fa58bf46fa65ee9270ed454063ddab4", md5String( new File( toDir, "r.daemon" ) ) );
        assertEquals( "389da7874f36033d95fe5a42fe84af60", md5String( new File( toDir, "postinstall" ) ) );
        assertEquals( "92e09c0ad1e8d5967c72e9372db3eb13", md5String( new File( toDir, "preinstall" ) ) );
    }
}
