 package com.stratio.mojo.unix.maven;

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
import static fj.data.Option.*;

import junit.framework.TestCase;

import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.PackageVersion.*;
import static com.stratio.mojo.unix.maven.deb.DebUnixPackage.*;
import com.stratio.mojo.unix.maven.rpm.*;
import static com.stratio.mojo.unix.maven.sysvpkg.PkgUnixPackage.*;

 /**
 * This test is here and not in unix-common because it depend on the actual implementations.
 * <p/>
 * The purpose is to assert that the version parsing/generation is similar across all formats.
 */
@SuppressWarnings( {"unchecked"} )
public class VersionTest
    extends TestCase
{
    // -----------------------------------------------------------------------
    // Snapshots
    // -----------------------------------------------------------------------

    public void testSnapshotWithoutRevision()
    {
        verify( packageVersion( "1.2-SNAPSHOT", "20090423095107", true, Option.<String>none() ),
            deb( "1.2~20090423095107-1" ),
            pkg( "1.2-20090423095107" ),
            rpm( "1.2", "0.1.20090423095107" ) );
    }

    public void testSnapshotWithConfiguredRevision()
    {
        verify( packageVersion( "1.2-SNAPSHOT", "20090423095107", true, some( "3" ) ),
            deb( "1.2~20090423095107-3" ),
            pkg( "1.2-20090423095107-3" ),
            rpm( "1.2", "0.1.20090423095107.3" ) );
    }

    public void testSnapshotWithEmbeddedRevision()
    {
        verify( packageVersion( "1.2-3-SNAPSHOT", "20090423095107", true, Option.<String>none() ),
            deb( "1.2-3~20090423095107-1" ),
            pkg( "1.2-3-20090423095107" ),
            rpm( "1.2_3", "0.1.20090423095107" ) );
    }

    public void testSnapshotWithEmbeddedAndConfiguredRevision()
    {
        verify( packageVersion( "1.2-3-SNAPSHOT", "20090423095107", true, some( "3" ) ),
            deb( "1.2-3~20090423095107-3" ),
            pkg( "1.2-3-20090423095107-3" ),
            rpm( "1.2_3", "0.1.20090423095107.3" ) );
    }

    public void testPrereleaseSnapshotWithoutRevision()
    {
        verify( packageVersion( "1.2-rc1-SNAPSHOT", "20090423095107", true, Option.<String>none() ),
            deb( "1.2~rc1~20090423095107-1" ),
            pkg( "1.2-rc1-20090423095107" ),
            rpm( "1.2_rc1", "1" ) );
    }

    public void testPrereleaseSnapshotWithConfiguredRevision()
    {
        verify( packageVersion( "1.2-rc1-SNAPSHOT", "20090423095107", true, some( "3" ) ),
            deb( "1.2~rc1~20090423095107-3" ),
            pkg( "1.2-rc1-20090423095107-3" ),
            rpm( "1.2_rc1", "3" ) );
    }

    public void testPrereleaseSnapshotWithEmbeddedRevision()
    {
        verify( packageVersion( "1.2-rc1-3-SNAPSHOT", "20090423095107", true, Option.<String>none() ),
            deb( "1.2~rc1-3~20090423095107-1" ),
            pkg( "1.2-rc1-3-20090423095107" ),
            rpm( "1.2_rc1_3", "1" ) );
    }

    public void testPrereleaseSnapshotWithEmbeddedAndConfiguredRevision()
    {
        verify( packageVersion( "1.2-rc1-3-SNAPSHOT", "20090423095107", true, some( "3" ) ),
            deb( "1.2~rc1-3~20090423095107-3" ),
            pkg( "1.2-rc1-3-20090423095107-3" ),
            rpm( "1.2_rc1_3", "3" ) );
    }

    // -----------------------------------------------------------------------
    // Release
    // -----------------------------------------------------------------------

    public void testReleaseWithoutRevision()
    {
        verify( packageVersion( "1.2", "20090423095107", false, Option.<String>none() ),
            deb( "1.2-1" ),
            pkg( "1.2" ),
            rpm( "1.2", "1" ) );
    }

    public void testReleaseWithConfiguredRevision()
    {
        verify( packageVersion( "1.2", "20090423095107", false, some( "3" ) ),
            deb( "1.2-3" ),
            pkg( "1.2-3" ),
            rpm( "1.2", "3" ) );
    }

    public void testReleaseWithEmbeddedRevision()
    {
        verify( packageVersion( "1.2-3", "20090423095107", false, Option.<String>none() ),
            deb( "1.2-3-1" ),
            pkg( "1.2-3" ),
            rpm( "1.2_3", "1" ) );
    }

    public void testReleaseWithConfiguredAndEmbeddedRevision()
    {
        verify( packageVersion( "1.2-3", "20090423095107", false, some( "3" ) ),
            deb( "1.2-3-3" ),
            pkg( "1.2-3-3" ),
            rpm( "1.2_3", "3" ) );
    }

    public void testPrereleaseWithoutRevision()
    {
        verify( packageVersion( "1.2-rc1", "20090423095107", false, Option.<String>none() ),
            deb( "1.2~rc1-1" ),
            pkg( "1.2-rc1" ),
            rpm( "1.2_rc1", "1" ) );
    }

    public void testPrereleaseWithConfiguredRevision()
    {
        verify( packageVersion( "1.2-rc1", "20090423095107", false, some( "3" ) ),
            deb( "1.2~rc1-3" ),
            pkg( "1.2-rc1-3" ),
            rpm( "1.2_rc1", "3" ) );
    }

    public void testPrereleaseWithEmbeddedRevision()
    {
        verify( packageVersion( "1.2-rc1-3", "20090423095107", false, Option.<String>none() ),
            deb( "1.2~rc1-3-1" ),
            pkg( "1.2-rc1-3" ),
            rpm( "1.2_rc1_3", "1" ) );
    }

    public void testPrereleaseWithConfiguredAndEmbeddedRevision()
    {
        verify( packageVersion( "1.2-rc1-3", "20090423095107", false, some( "3" ) ),
            deb( "1.2~rc1-3-3" ),
            pkg( "1.2-rc1-3-3" ),
            rpm( "1.2_rc1_3", "3" ) );
    }

    public static <A> void verify( A a, Verifier<A>... verifiers )
    {
        for ( Verifier<A> verifier : verifiers )
        {
            verifier.verify( a );
        }
    }

    private interface Verifier<A>
    {
        void verify( A a );
    }

    private Verifier<PackageVersion> deb( final String expected )
    {
        return new Verifier<PackageVersion>()
        {
            public void verify( PackageVersion packageVersion )
            {
                assertEquals( "deb", expected, getDebianVersion( packageVersion ) );
            }
        };
    }

    private Verifier<PackageVersion> pkg( final String expected )
    {
        return new Verifier<PackageVersion>()
        {
            public void verify( PackageVersion packageVersion )
            {
                assertEquals( "pkg", expected, getPkgVersion( packageVersion ) );
            }
        };
    }

    private Verifier<PackageVersion> rpm( final String expected, final String revision )
    {
        return new Verifier<PackageVersion>()
        {
            public void verify( PackageVersion packageVersion )
            {
                P2<String, String> rpmVersion = RpmUnixPackage.getRpmVersion( packageVersion );
                assertEquals( "rpm.version", expected, rpmVersion._1() );
                assertEquals( "rpm.revision", revision, rpmVersion._2() );
            }
        };
    }
}
