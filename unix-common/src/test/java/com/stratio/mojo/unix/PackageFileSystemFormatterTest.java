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
 package com.stratio.mojo.unix;

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
import static com.stratio.mojo.unix.PackageFileSystem.*;
import static com.stratio.mojo.unix.PackageFileSystemTest.*;
import com.stratio.mojo.unix.util.line.*;

public class PackageFileSystemFormatterTest
    extends TestCase
{
    public void testBasic()
    {
        PackageFileSystemFormatter<Object> formatter = PackageFileSystemFormatter.flatFormatter();

        LineFile fs1 = formatter.print( create( root, root ).
            addDirectory( a ).
            addDirectory( b ) );

        assertEquals( new LineFile().add( "." ).add( "    a" ).add( "    b" ), fs1 );

        LineFile fs2 = formatter.print( create( root, root ).
            addDirectory( a ).
            addDirectory( b ).
            addFile( a_x ) );

        assertEquals( new LineFile().
            add( "." ).
            add( "    a" ).
            add( "        a-x" ).
            add( "    b" ), fs2 );

        LineFile fs3 = formatter.print( create( root, root ).
            addDirectory( b ).
            addDirectory( a ).
            addFile( a_x ).
            addFile( b_x ).
            addFile( a_y ).
            addFile( c_x_u ) );

//        System.out.print( formatter.print( fs3 ) );

        assertEquals( new LineFile().
            add(".").
            add("    a").
            add("        a-x").
            add("        a-y").
            add("    b").
            add("        b-x").
            add("    c").
            add("        c-x").
            add("            c-x-u"), fs3 );
    }
}
