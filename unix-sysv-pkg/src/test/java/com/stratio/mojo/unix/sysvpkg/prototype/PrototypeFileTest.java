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
 package com.stratio.mojo.unix.sysvpkg.prototype;

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
import junit.framework.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.UnixFileMode.*;
import static com.stratio.mojo.unix.UnixFsObject.*;

import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import static com.stratio.mojo.unix.util.UnixUtil.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PrototypeFileTest
    extends TestCase
{
    private final LocalDateTime dateTime = new LocalDateTime();

    RelativePath extractJarPath = relativePath( "extract.jar" );
    RelativePath bashProfilePath = relativePath( "/opt/jetty/.bash_profile" );
    RelativePath specialPath = relativePath( "/special" );
    RelativePath smfManifestXmlPath = relativePath( "/smf/manifest.xml" );

    FileAttributes fileAttributes = new FileAttributes( some( "nouser" ), some( "nogroup" ), some( _0644 ) );
    FileAttributes dirAttributes = new FileAttributes( some( "nouser" ), some( "nogroup" ), some( _0755 ) );

    FileAttributes defaultAttributes = EMPTY.user( "default" ).group( "default" );
    Directory defaultDirectory = directory( BASE, new LocalDateTime( 0 ), defaultAttributes );
    DirectoryEntry defaultEntry = new DirectoryEntry( Option.<String>none(), defaultDirectory );

    public void testBasic()
        throws Exception
    {
        LocalFs root = new LocalFs( getTestFile( "target/prototype-test/assembly" ) );
        root.mkdir();

        PrototypeFile prototypeFile = new PrototypeFile( defaultEntry );

        LocalFs bashProfileObject = root.resolve( "src/test/non-existing/bash_profile" );
        LocalFs extractJarObject = root.resolve( "src/test/non-existing/extract.jar" );
        UnixFsObject.RegularFile extractJar = regularFile( extractJarPath, dateTime, 0, fileAttributes );
        UnixFsObject.RegularFile bashProfile = regularFile( bashProfilePath, dateTime, 0, fileAttributes );
        UnixFsObject.RegularFile smfManifestXml = regularFile( smfManifestXmlPath, dateTime, 0, fileAttributes.addTag( "class:smf" ) );

        prototypeFile.addFile( bashProfileObject, bashProfile );
        prototypeFile.addFile( extractJarObject, extractJar );
        prototypeFile.addFile( extractJarObject, smfManifestXml );
        prototypeFile.addDirectory( directory( BASE, dateTime, dirAttributes ) );
        prototypeFile.addDirectory( directory( specialPath, dateTime, dirAttributes ) );
        prototypeFile.apply( filter( extractJarPath, fileAttributes.user( "funnyuser" ) ) );
        prototypeFile.apply( filter( specialPath, dirAttributes.group( "funnygroup" ) ) );

        LineFile stream = new LineFile();

        prototypeFile.streamTo( stream );

        assertEquals( new LineFile().
            add( "f none /extract.jar=" + extractJarObject.absolutePath() + " 0644 funnyuser nogroup" ).
            add( "d none /opt ? default default" ).
            add( "d none /opt/jetty ? default default" ).
            add( "f none /opt/jetty/.bash_profile=" + bashProfileObject.absolutePath() + " 0644 nouser nogroup" ).
            add( "d none /smf ? default default" ).
            add( "f smf /smf/manifest.xml=" + extractJarObject.absolutePath() + " 0644 nouser nogroup" ).
            add( "d none /special 0755 nouser funnygroup" ).
            toString(), stream.toString() );
    }

    private F<UnixFsObject, Option<UnixFsObject>> filter( final RelativePath s, final FileAttributes newAttributes )
    {
        return new F<UnixFsObject, Option<UnixFsObject>>()
        {
            public Option<UnixFsObject> f( UnixFsObject object )
            {
                return !object.path.string.startsWith( s.string ) ?
                    Option.<UnixFsObject>none() :
                    some( object.setFileAttributes( object.attributes.useAsDefaultsFor( newAttributes ) ) );
            }
        };
    }
}
