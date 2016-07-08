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
 package com.stratio.mojo.unix.core;

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
import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;
import org.codehaus.plexus.*;
import org.easymock.*;

import java.io.*;

import static fj.P.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.util.RelativePath.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class OperationTest
    extends PlexusTestCase
{
    public final static FileAttributes fileAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( UnixFileMode._0755 ) );

    public final static FileAttributes directoryAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( UnixFileMode._0644 ) );

    private static final TestUtil testUtil = new TestUtil( OperationTest.class );

    public static final Paths paths = new Paths();
    public static final Files files = new Files();
    public static final Objects objects = new Objects();

    public static class Paths
    {
        RelativePath base = RelativePath.BASE;
        RelativePath opt = relativePath( "/opt/" );
        RelativePath optJetty = opt.add( "jetty" );
        RelativePath optJettyBin = optJetty.add( "bin" );
        RelativePath optJettyBinExtraApp = optJettyBin.add( "extra-app" );
        RelativePath optJettyReadmeUnix = optJetty.add( "README-unix.txt" );
        RelativePath optJettyBashProfile = optJetty.add( ".bash_profile" );
    }

    public static class Files
    {
        LocalFs files = new LocalFs( testUtil.getTestFile( "src/test/resources/operation/files" ) );
        LocalFs optJettyReadmeUnix = files.resolve( paths.optJettyReadmeUnix );
        LocalFs optJettyBinExtraApp = files.resolve( paths.optJettyBinExtraApp );
        LocalFs optJettyBashProfile = files.resolve( paths.optJettyBashProfile );
    }

    public static class Objects
    {
        Directory optJettyBin = directory( paths.optJettyBin, files.files.resolve( paths.optJettyBin ).lastModified(), directoryAttributes );
        Directory optJetty = directory( paths.optJetty, files.files.resolve( paths.optJetty ).lastModified(), directoryAttributes );
        Directory opt = directory( paths.opt, files.files.resolve( paths.opt ).lastModified(), directoryAttributes );
        Directory base = directory( paths.base, files.files.resolve( paths.base ).lastModified(), directoryAttributes );
        RegularFile optJettyBashProfile = regularFile( paths.optJettyBashProfile, files.optJettyBashProfile.lastModified(), files.optJettyBashProfile.size(), fileAttributes );
        RegularFile optJettyBinExtraApp = regularFile( paths.optJettyBinExtraApp, files.optJettyBinExtraApp.lastModified(), files.optJettyBinExtraApp.size(), fileAttributes );
        RegularFile optJettyReadmeUnix = regularFile( paths.optJettyReadmeUnix, files.optJettyReadmeUnix.lastModified(), files.optJettyReadmeUnix.size(), fileAttributes );
    }

    /**
     * Based on the <code>&gt;copy&lt;</code> operation that is in the jetty IT.
     */
    public void testCopyOnACompleteDirectoryStructure()
        throws Exception
    {
        assertTrue( files.files.isDirectory() );
        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        fileCollector.addFile( files.optJettyBinExtraApp, objects.optJettyBinExtraApp );
        control.setMatcher( new FsMatcher() );
        fileCollector.addFile( files.optJettyReadmeUnix, objects.optJettyReadmeUnix );
        fileCollector.addFile( files.optJettyBashProfile, objects.optJettyBashProfile );
        fileCollector.addDirectory( objects.optJettyBin );
        fileCollector.addDirectory( objects.optJetty );
        fileCollector.addDirectory( objects.opt );
        fileCollector.addDirectory( objects.base );
        control.replay();

        new CopyDirectoryOperation( files.files, RelativePath.BASE, null, null, Option.<P2<String, String>>none(),
                                    fileAttributes, directoryAttributes ).
            perform( fileCollector );

        control.verify();
    }

    public void testExtractWithPattern()
        throws Exception
    {
        File archivePath = testUtil.getTestFile( "src/test/resources/operation/extract.jar" );

        Fs archive = FsUtil.resolve( archivePath );

        Fs fooLicense = archive.resolve( relativePath( "foo-license.txt" ) );
        assertTrue( fooLicense.isFile() );
        RegularFile fooLicenseUnixFile = regularFile( relativePath( "licenses/foo-license.txt" ),
                                                      fooLicense.lastModified(), fooLicense.size(), fileAttributes );

        Fs barLicense = archive.resolve( relativePath( "mydir/bar-license.txt" ) );
        RegularFile barLicenseUnixFile = regularFile( relativePath( "licenses/bar-license.txt" ),
                                                      barLicense.lastModified(), barLicense.size(), fileAttributes );

        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        fileCollector.addFile( barLicense, barLicenseUnixFile );
        fileCollector.addFile( fooLicense, fooLicenseUnixFile );
        control.replay();

        new CopyDirectoryOperation( archive, relativePath( "licenses" ), single( "**/*license.txt" ), null,
                                    some( p( ".*/(.*license.*)", "$1" ) ), fileAttributes, directoryAttributes ).
            perform( fileCollector );

        control.verify();

        archive.close();
    }

    static class FsMatcher
        extends AbstractMatcher
    {
        int i = 0;

        protected boolean argumentMatches( Object e, Object a )
        {
            i++;
            if ( i != 1 )
            {
                return super.argumentMatches( e, a );
            }

            Fs expected = (Fs) e;
            Fs actual = (Fs) a;

            return MockControl.EQUALS_MATCHER.matches( new Object[]{expected.basedir() + "/" + expected.relativePath().string},
                                                       new Object[]{actual.basedir() + "/" + actual.relativePath().string} );
        }
    }

//    private static UnixFsObject.Directory createDirectory( String path, FileObject files, FileAttributes directoryAttributes )
//    {
//        try
//        {
//            return AssemblyOperationUtil.dirFromFileObject( relativePath( path ), files.resolveFile( path ), directoryAttributes );
//        }
//        catch ( FileSystemException e )
//        {
//            throw new RuntimeException( e );
//        }
//    }
//
//    private static RegularFile fromFileObject( RelativePath path, FileObject file,
//                                                            FileAttributes attributes )
//    {
//        try
//        {
//            return AssemblyOperationUtil.fromFileObject( path, file, attributes );
//        }
//        catch ( FileSystemException e )
//        {
//            throw new RuntimeException( e );
//        }
//    }
}
