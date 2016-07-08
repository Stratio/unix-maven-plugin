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
 package com.stratio.mojo.unix.sysvpkg;

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
import static fj.data.List.*;
import static fj.data.Option.*;
import junit.framework.*;
import static com.stratio.mojo.unix.sysvpkg.Pkginfo.*;
import com.stratio.mojo.unix.util.line.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkginfoTest
    extends TestCase
{
    public void testParsing()
    {
        Iterable<String> pkginfoStrings = (LineFile) (new LineFile().
            add( "        PKGINST:  project-pkg-1" ).
            add( "           NAME:  Hudson" ).
            add( "       CATEGORY:  application" ).
            add( "           ARCH:  all" ).
            add( "           SIZE:  1024" ).
            add( "        VERSION:  1.1-2" ).
            add( "         PSTAMP:  20090129.134909" ).
            add( "          EMAIL:  trygvis@inamo.no" ).
            add( "         STATUS:  spooled" ).
            add( "          FILES:        3 spooled pathnames" ).
            add( "                        2 package information files" ).
            add( "                    40281 blocks used (approx)" ));

        Pkginfo expected = new Pkginfo( "all", "application", "Hudson", "project-pkg-1", "1.1-2",
                                                some( "20090129.134909" ), Option.<String>none(),
                                                some( "trygvis@inamo.no" ),some( "1024" ),
                                                List.<String>nil(),Option.<String>none());

        assertEquals( expected.toString(), fromStream( pkginfoStrings ).some().toString() );
    }

    public void testClasses()
    {
        Pkginfo pkginfo = new Pkginfo( "all", "application", "name", "mypackage", "1.0" );

        assertEquals( new LineFile().
            add( "ARCH=all" ).
            add( "CATEGORY=application" ).
            add( "NAME=name" ).
            add( "PKG=mypackage" ).
            add( "VERSION=1.0" ).
//            add( "CLASSES=none"). I think this is the right behaviou.
//  If pkgmk always insert *and* warn about the class it should be commented back in
            toString(), pkginfo.toString() );

        pkginfo = pkginfo.
            classes( list( "smf" ) );

        assertEquals( new LineFile().
            add( "ARCH=all" ).
            add( "CATEGORY=application" ).
            add( "NAME=name" ).
            add( "PKG=mypackage" ).
            add( "VERSION=1.0" ).
            add( "CLASSES=smf" ).
            toString(), pkginfo.toString() );

        pkginfo = pkginfo.
            classes( list( "none", "smf" ) );

        assertEquals( new LineFile().
            add( "ARCH=all" ).
            add( "CATEGORY=application" ).
            add( "NAME=name" ).
            add( "PKG=mypackage" ).
            add( "VERSION=1.0" ).
            add( "CLASSES=none smf" ).
            toString(), pkginfo.toString() );
    }
}
