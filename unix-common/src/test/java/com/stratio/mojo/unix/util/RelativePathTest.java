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

import fj.data.*;
import junit.framework.*;
import static com.stratio.mojo.unix.util.RelativePath.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class RelativePathTest
    extends TestCase
{
    public void testFromString()
    {
        assertEquals( ".", relativePath( "" ).string );
        assertEquals( ".", relativePath( null ).string );
        assertEquals( ".", relativePath( "/" ).string );
        assertEquals( ".", relativePath( "/." ).string );
        assertEquals( ".", relativePath( "//" ).string );
        assertEquals( "a", relativePath( "a/" ).string );
        assertEquals( "a", relativePath( "/a" ).string );
        assertEquals( "a", relativePath( "/a/" ).string );

        assertEquals( "a", relativePath( "/a//" ).string );

        assertEquals( "a/b", relativePath( "/a/b/" ).string );

        assertEquals( "a/b/c", relativePath( "/a////b//c////" ).string );

        assertEquals( ".a", relativePath( ".a" ).string );
        assertEquals( ".", relativePath( "/." ).string );
        assertEquals( "a", relativePath( "a/." ).string );
        assertEquals( "a", relativePath( "/a/." ).string );

        assertEquals( "opt/jb/.bash_profile", relativePath( "/opt/jb/.bash_profile" ).string );
        assertEquals( ".bash_profile", relativePath( ".bash_profile" ).string );

        assertEquals( "opt/jetty/bin", relativePath( "/opt/jetty/bin" ).string );
        assertEquals( "opt/jetty", relativePath( "/opt/jetty/" ).string );
        assertEquals( "opt", relativePath( "/opt" ).string );
    }

    public void testAdd()
    {
        assertEquals( ".", relativePath( "" ).add( "" ).string );
        assertEquals( ".", relativePath( "" ).add( "/" ).string );
        assertEquals( ".", relativePath( "/" ).add( "" ).string );
        assertEquals( ".", relativePath( "/" ).add( "/" ).string );

        assertEquals( "a", relativePath( "./" ).add( "/a/" ).string );
        assertEquals( "a", relativePath( "/a/" ).add( "/." ).string );

        assertEquals( "opt/foo/bin/yo", relativePath( "/opt/foo" ).add( "/bin/yo" ).string );
        assertEquals( "opt/jetty/README-unix.txt", relativePath( "/" ).add( "/opt/jetty/README-unix.txt" ).string );
    }

    public void testName()
    {
        assertEquals( "a", relativePath( "b/a" ).name() );
        assertEquals( "a", relativePath( "a" ).name() );
        assertEquals( ".", relativePath( "." ).name() );
    }

    public void testIsBelowOrSame()
    {
        assertTrue( relativePath( "." ).isBelowOrSame( relativePath( "." ) ) );
        assertFalse( relativePath( "." ).isBelowOrSame( relativePath( "a" ) ) );
        assertTrue( relativePath( "a" ).isBelowOrSame( relativePath( "." ) ) );
        assertTrue( relativePath( "a" ).isBelowOrSame( relativePath( "a" ) ) );
        assertTrue( relativePath( "a/b" ).isBelowOrSame( relativePath( "a" ) ) );
        assertFalse( relativePath( "a" ).isBelowOrSame( relativePath( "a/b" ) ) );
    }

    public void testSubtract()
    {
        assertEquals( relativePath( "." ), relativePath( "." ).subtract( relativePath( "." ) ).some() );
        assertTrue( relativePath( "." ).subtract( relativePath( "a" ) ).isNone() );
        assertEquals( relativePath( "a" ), relativePath( "a" ).subtract( relativePath( "." ) ).some() );
        assertEquals( relativePath( "a" ), relativePath( "a" ).subtract( relativePath( "a" ) ).some() );
        assertEquals( relativePath( "b" ), relativePath( "a/b" ).subtract( relativePath( "a" ) ).some() );
        assertTrue( relativePath( "a" ).subtract( relativePath( "a/b" ) ).isNone() );
    }

    public void testToList()
    {
        assertTrue( relativePath( "/" ).toList().isEmpty() );
        assertTrue( relativePath( "." ).toList().isEmpty() );

        assertEquals( 1, relativePath( "a" ).toList().length() );
        assertEquals( "a", relativePath( "a" ).toList().head() );

        List<String> l = relativePath( "/a/b/c" ).toList();
//        Show.<String>listShow( Show.stringShow ).println( l );
        assertEquals( "a", l.index( 0 ) );
        assertEquals( "b", l.index( 1 ) );
        assertEquals( "c", l.index( 2 ) );
    }

    public void testAsAbsolutePath()
    {
        assertEquals( "/", relativePath( "." ).asAbsolutePath( "/" ) );
        assertEquals( "/", relativePath( "/" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", relativePath( "a" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", relativePath( "a/" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", relativePath( "/a" ).asAbsolutePath( "/" ) );
        assertEquals( "/a", relativePath( "/a/" ).asAbsolutePath( "/" ) );
        assertEquals( "/a/b", relativePath( "a/b" ).asAbsolutePath( "/" ) );
        assertEquals( "/opt/jb/.bash_profile", relativePath( "/opt/jb/.bash_profile" ).asAbsolutePath( "/" ) );
    }

    public void testClean()
    {
        assertNull( RelativePath.clean( "" ) );
        assertEquals( "opt/.bash_profile", RelativePath.clean( "opt/.bash_profile" ) );
        assertEquals( "opt/.bash_profile", RelativePath.clean( "opt/.bash_profile" ) );

        assertEquals( "file", RelativePath.clean( "file" ) );
        assertEquals( "file", RelativePath.clean( "/file" ) );
        assertEquals( "file", RelativePath.clean( "//file" ) );
        assertEquals( "file", RelativePath.clean( "./file" ) );

        assertEquals( ".bash_profile", RelativePath.clean( ".bash_profile" ) );
        assertEquals( ".bash_profile", RelativePath.clean( "/.bash_profile" ) );
    }
}
