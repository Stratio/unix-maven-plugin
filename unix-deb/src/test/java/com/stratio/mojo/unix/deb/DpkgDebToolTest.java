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
 package com.stratio.mojo.unix.deb;

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

import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import org.codehaus.plexus.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DpkgDebToolTest
    extends PlexusTestCase
{
    public void testBasic()
        throws Exception
    {
        List<UnixFsObject> contents = DpkgDebTool.contents( getTestFile( "src/test/resources/base-files_4_i386.deb" ) );

        assertEquals( paths.length, contents.size() );
        assertEquals( UnixFsObject.Directory.class, contents.get( 0 ).getClass() );
        int i = 0;

        for ( UnixFsObject unixFsObject : contents )
        {
            assertEquals( paths[i++], unixFsObject.path );
        }
    }

    private static final RelativePath[] paths = new RelativePath[]
        {
            RelativePath.BASE,
            relativePath( "usr/" ),
            relativePath( "usr/share/" ),
            relativePath( "usr/share/doc/" ),
            relativePath( "usr/share/doc/base-files/" ),
            relativePath( "usr/share/doc/base-files/FAQ" ),
            relativePath( "usr/share/doc/base-files/README.FHS" ),
            relativePath( "usr/share/doc/base-files/README.base" ),
            relativePath( "usr/share/doc/base-files/copyright" ),
            relativePath( "usr/share/doc/base-files/remove-base" ),
            relativePath( "usr/share/doc/base-files/changelog.gz" ),
            relativePath( "usr/share/base-files/" ),
            relativePath( "usr/share/base-files/dot.bashrc" ),
            relativePath( "usr/share/base-files/dot.profile" ),
            relativePath( "usr/share/base-files/info.dir" ),
            relativePath( "usr/share/base-files/motd.md5sums" ),
            relativePath( "usr/share/base-files/nsswitch.conf" ),
            relativePath( "usr/share/base-files/profile" ),
            relativePath( "usr/share/base-files/motd" ),
            relativePath( "usr/share/common-licenses/" ),
            relativePath( "usr/share/common-licenses/Artistic" ),
            relativePath( "usr/share/common-licenses/BSD" ),
            relativePath( "usr/share/common-licenses/GPL-2" ),
            relativePath( "usr/share/common-licenses/LGPL-2" ),
            relativePath( "usr/share/common-licenses/LGPL-2.1" ),
            relativePath( "usr/share/dict/" ),
            relativePath( "usr/share/info/" ),
            relativePath( "usr/share/man/" ),
            relativePath( "usr/share/misc/" ),
            relativePath( "usr/bin/" ),
            relativePath( "usr/games/" ),
            relativePath( "usr/include/" ),
            relativePath( "usr/lib/" ),
            relativePath( "usr/sbin/" ),
            relativePath( "usr/src/" ),
            relativePath( "bin/" ),
            relativePath( "boot/" ),
            relativePath( "dev/" ),
            relativePath( "etc/" ),
            relativePath( "etc/default/" ),
            relativePath( "etc/skel/" ),
            relativePath( "etc/debian_version" ),
            relativePath( "etc/host.conf" ),
            relativePath( "etc/issue" ),
            relativePath( "etc/issue.net" ),
            relativePath( "home/" ),
            relativePath( "lib/" ),
            relativePath( "mnt/" ),
            relativePath( "proc/" ),
            relativePath( "root/" ),
            relativePath( "sbin/" ),
            relativePath( "tmp/" ),
            relativePath( "var/" ),
            relativePath( "var/backups/" ),
            relativePath( "var/cache/" ),
            relativePath( "var/lib/" ),
            relativePath( "var/lib/dpkg/" ),
            relativePath( "var/lib/misc/" ),
            relativePath( "var/local/" ),
            relativePath( "var/lock/" ),
            relativePath( "var/log/" ),
            relativePath( "var/run/" ),
            relativePath( "var/spool/" ),
            relativePath( "var/tmp/" ),
            relativePath( "usr/share/common-licenses/LGPL" ),
            relativePath( "usr/share/common-licenses/GPL" ),
        };
}
