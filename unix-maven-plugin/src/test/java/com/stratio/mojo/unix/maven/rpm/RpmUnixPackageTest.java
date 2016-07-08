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
 package com.stratio.mojo.unix.maven.rpm;

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
import org.apache.maven.plugin.logging.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.PackageParameters.*;
import static com.stratio.mojo.unix.PackageVersion.*;
import static com.stratio.mojo.unix.UnixFsObject.*;

import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.maven.*;
import com.stratio.mojo.unix.rpm.*;

import static com.stratio.mojo.unix.io.fs.FsUtil.resolve;
import static com.stratio.mojo.unix.util.RelativePath.*;
import static com.stratio.mojo.unix.util.ScriptUtil.Strategy.SINGLE;
import org.codehaus.plexus.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class RpmUnixPackageTest
    extends PlexusTestCase
{
    private final LocalDateTime now = new LocalDateTime();

    public void testBasic()
        throws Exception
    {
        LocalFs pomXml = new LocalFs( getTestFile( "pom.xml" ) );
        Fs archive = resolve( getTestFile( "../unix-core/src/test/resources/operation/extract.jar" ) );
        Fs<?> fooLicense = archive.resolve( relativePath( "foo-license.txt" ) );
        Fs<?> barLicense = archive.resolve( relativePath( "mydir/bar-license.txt" ) );

        RpmPackagingFormat packagingFormat = new RpmPackagingFormat();

        LocalFs root = new LocalFs( getTestFile( "target/rpm-test" ) );
        File packageFile = root.resolve( "file.rpm" ).file;

        PackageVersion version = packageVersion( "1.0-1", "123", false, Option.<String>none() );
        PackageParameters parameters = packageParameters( "mygroup", "myartifact", version, "id", "default-name",
                                                          Option.<String>none(), EMPTY, EMPTY ).
            contact( "Kurt Cobain" ).
            architecture( "noarch" ).
            name( "Yo!" ).
            license( "BSD" ).excludeDirs(List.<String>nil());

        RpmUnixPackage unixPackage = packagingFormat.start( new SystemStreamLog() ).
            parameters( parameters ).
            rpmParameters( "Fun", Option.<String>none(), "wget" ).
            workingDirectory( root.resolve( "working-directory" ) );

        unixPackage.beforeAssembly( EMPTY, now );

        unixPackage.addFile( pomXml, regularFile( relativePath( "/pom.xml" ), now, 0, EMPTY ) );
        unixPackage.addFile( fooLicense, regularFile( relativePath( "/foo-license.txt" ), now, 0, EMPTY ) );
        unixPackage.addFile( barLicense, regularFile( relativePath( "/bar-license.txt" ), now, 0, EMPTY ) );

        RpmUnixPackage.RpmPreparedPackage preparedPackage = unixPackage.
            debug( true ).
            prepare( SINGLE );

        if ( !new Rpmbuild().available() )
        {
            System.err.println( "Skipping test: " + super.getName() );
            return;
        }

        preparedPackage.
            packageToFile( packageFile );

        assertTrue( packageFile.canRead() );
    }

    public void testFiltering()
        throws Exception
    {
        RpmPackagingFormat packagingFormat = new RpmPackagingFormat();

        UnixPackageTestUtil<RpmUnixPackage, RpmUnixPackage.RpmPreparedPackage> unixPackageTestUtil =
            new UnixPackageTestUtil<RpmUnixPackage, RpmUnixPackage.RpmPreparedPackage>( "rpm", packagingFormat  )
            {
                protected RpmUnixPackage extraStuff( RpmUnixPackage rpmUnixPackage )
                {
                    return rpmUnixPackage.rpmParameters( "my-group", Option.<String>none(), "wget" );
                }
            };
        unixPackageTestUtil.testFiltering();
    }
}
