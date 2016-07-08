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

import fj.*;
import fj.Function;
import fj.data.*;
import junit.framework.*;
import static com.stratio.mojo.unix.sysvpkg.PkgchkUtil.*;
import com.stratio.mojo.unix.util.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;

public class PkgchkUtilTest
    extends TestCase
{
    public void testBasic()
    {
        LineFile file = new LineFile();
        file.
            add( "## Checking control scripts." ).
            add( "## Checking package objects." ).
            add( "Pathname: /usr/share/hudson/lib/hudson.war" ).
            add( "Type: regular file" ).
            add( "Expected mode: 0644" ).
            add( "Expected owner: hudson" ).
            add( "Expected group: hudson" ).
            add( "Expected file size (bytes): 20623413" ).
            add( "Expected sum(1) of contents: 3301" ).
            add( "Expected last modification: Jan 30 15:11:40 2009" ).
            add( "Current status: installed" ).
            add().
            add( "Pathname: /var/log/hudson" ).
            add( "Type: symbolic link" ).
            add( "Source of link: /var/opt/hudson/log" ).
            add( "Current status: installed" ).
            add().
            add( "Pathname: /usr/lib" ).
            add( "Type: directory" ).
            add( "Expected mode: 0755" ).
            add( "Expected owner: root" ).
            add( "Expected group: bin" ).
            add().
            add( "Pathname: pkginfo" ).
            add( "Type: installation file" ).
            add( "Expected file size (bytes): 143" ).
            add( "Expected sum(1) of contents: 11066" ).
            add( "Expected last modification: Feb 25 15:13:52 2009" ).
            add().
            add( "## Checking is complete." );

        PkgchkUtil.PkgchkParser parser = new PkgchkUtil.PkgchkParser();

        for ( String line : file )
        {
            parser.onLine( line );
        }

        Option<LocalDateTime> date = PkgchkUtil.parser.f( "Jan 30 15:11:40 2009" ).map( UnixUtil.toLocalDateTime );
        Option<LocalDateTime> date2 = PkgchkUtil.parser.f( "Feb 25 15:13:52 2009" ).map( UnixUtil.toLocalDateTime );
        assertTrue( date.isSome() );
        assertTrue( date2.isSome() );

        List<PkgchkUtil.FileInfo> expected = List.<PkgchkUtil.FileInfo>nil().
            cons( regularFile( "/usr/share/hudson/lib/hudson.war", "0644", "hudson", "hudson", 20623413, 3301, date ) ).
            cons( symlink( "/var/log/hudson", "/var/opt/hudson/log" ) ).
            cons( directory( "/usr/lib", "0755", "root", "bin", Option.<LocalDateTime>none() ) ).
            cons( installationFile( "pkginfo", 143, 11066, date2 ) ).
            reverse();

        List<FileInfo> actual = parser.getList();

        assertEquals( expected.length(), actual.length() );

        expected.zipWith( actual, Function.curry( new F2<FileInfo, FileInfo, Object>()
        {
            public Object f( FileInfo expected, FileInfo actual )
            {
                if ( !expected.equalsIgnoreNull( actual ) )
                {
                    LineFile lines = new LineFile();

                    lines.add( "expected:" );
                    expected.streamTo( lines );

                    lines.add();
                    lines.add( "actual:" );
                    actual.streamTo( lines );

                    fail( lines.toString() );
                }
                return Unit.unit();
            }
        } ) );
    }
}
