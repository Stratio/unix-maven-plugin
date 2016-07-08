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

import fj.data.*;
import static fj.data.Option.*;
import junit.framework.*;
import static com.stratio.mojo.unix.PackageVersion.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PackageVersionTest
    extends TestCase
{
    Option<String> noneS = Option.none();

    public void testReleaseVersionWithEmbeddedRevision()
    {
        assertVersion( "1.0-1", "20080703.084400", noneS, "1.0-1",
                       packageVersion( "1.0-1", "20080703.084400", false, noneS ) );
    }

    public void testReleaseVersionWithoutEmbeddedRevison()
    {
        assertVersion( "1.0", "20080703.084400", noneS, "1.0",
                       packageVersion( "1.0", "20080703.084400", false, noneS ) );
    }

    public void testReleaseVersionWithConfiguredRevison()
    {
        assertVersion( "1.0-alpha-2", "20080703.084400", some( "3" ), "1.0-alpha-2-3",
                       packageVersion( "1.0-alpha-2", "20080703.084400", false, some( "3" ) ) );
    }

    public void testSnapshotVersionWithEmbeddedRevision()
    {
        assertVersion( "1.0-1", "20080703.084400", noneS, "1.0-1-SNAPSHOT",
                       packageVersion( "1.0-1-SNAPSHOT", "20080703.084400", true, noneS ) );
    }

    public void testSnapshotVersionWithoutEmbeddedRevison()
    {
        // When your project uses versions on the form "1.0-alpha-2" you have to specify the revision
        assertVersion( "1.0-alpha-2", "20080703.084400", some( "3" ), "1.0-alpha-2-3-SNAPSHOT",
                       packageVersion( "1.0-alpha-2-SNAPSHOT", "20080703.084400", true, some( "3" ) ) );
    }

    public void testSnapshotVersionWithConfiguredRevision()
    {
        assertVersion( "1.0-1", "20080703.084400", some( "3" ), "1.0-1-3-SNAPSHOT",
                       packageVersion( "1.0-1-SNAPSHOT", "20080703.084400", true, some( "3" ) ) );
    }

    public void testRevision()
        throws Exception
    {
        assertVersion( "1.0-2", "20080703.084400", noneS, "1.0-2",
                       packageVersion( "1.0-2", "20080703.084400", false, noneS ) );

        assertVersion( "1.0-alpha-2", "20080703.084400", noneS, "1.0-alpha-2-SNAPSHOT",
                       packageVersion( "1.0-alpha-2-SNAPSHOT", "20080703.084400", true, noneS ) );

        // Hm, should this be allowed? Creating non-snapshot artifacts from snapshot artifacts.
        assertVersion( "1.0-2", "20080703.084400", noneS, "1.0-2-SNAPSHOT",
                       packageVersion( "1.0-2-SNAPSHOT", "20080703.084400", true, noneS ) );
    }

    public void testThatTimestampIsRequired()
    {
        try
        {
            packageVersion( "1.0-SNAPSHOT", null, true, noneS );
            fail( "Expected error" );
        }
        catch ( NullPointerException e )
        {
            assertTrue( e.getMessage().startsWith( "Argument #2 " ) );
        }
    }

    private void assertVersion( String version, String timestamp, Option<String> revision, String mavenVersion,
                                PackageVersion packageVersion )
    {
        assertEquals( "version", version, packageVersion.version );
        assertEquals( "timestamp", timestamp, packageVersion.timestamp );
        if ( revision.isSome() )
        {
            assertTrue( packageVersion.revision.isSome() );
            assertEquals( "revision.isSome()", revision.some(), packageVersion.revision.some() );
        }
        else
        {
            assertTrue( "revision", packageVersion.revision.isNone() );
        }
        assertEquals( "mavenVersion", mavenVersion, packageVersion.getMavenVersion() );
    }
}
