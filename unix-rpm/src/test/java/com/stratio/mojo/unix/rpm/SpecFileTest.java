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
 package com.stratio.mojo.unix.rpm;

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
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class SpecFileTest
    extends TestCase
{
    final LineFile header;
    final LineFile buildHeader;

    final LocalDateTime lastModified = new LocalDateTime( 2009, 2, 24, 9, 42 );

    public SpecFileTest()
        throws IOException
    {
        header = new LineFile();
        header.
            add( "Name: groupid-artifactid" ).
            add( "Version: 1.0" ).
            add( "Release: 1" ).
            add( "Summary: My summary" ).
            add( "License: License" ).
            add( "Group: My Group" );

        buildHeader = new LineFile();
        buildHeader.
            add( "BuildRoot: " + new File( "build-root" ).getAbsolutePath() ).
            add( "BuildArch: noarch" );
    }

    public void testFilesGeneration()
        throws Exception
    {
        SpecFile specFile = testSpecFile();
        SpecFile.excludedSysPaths.add("/bin");
        SpecFile.excludedSysPaths.add("/usr");
        Option<String> mygroup = some( "mygroup" );
        Option<String> myuser = Option.some( "myuser" );

        FileAttributes usrbinAttributes = new FileAttributes( myuser, mygroup, some( _0755 ) );
        FileAttributes binAttributes = new FileAttributes( myuser, mygroup, UnixFileMode.none );

        UnixFsObject.Directory usrbin = directory( relativePath( "/usr/bin" ), lastModified, usrbinAttributes );
        UnixFsObject.Directory bin = directory( relativePath( "/bin" ), lastModified, binAttributes );

        FileAttributes fileAttributes = new FileAttributes( myuser, mygroup, some( _0644 ) ).addTag( "unused" );
        RelativePath extract2Jar = relativePath( "/extract2.jar" );
        FileAttributes extract2JarAttributes = fileAttributes.user( "extract" );

        specFile.addDirectory( usrbin );
        specFile.addDirectory( bin );
        specFile.addFile( regularFile( relativePath( "/extract.jar" ), lastModified, 10, fileAttributes ) );
        specFile.addFile( regularFile( extract2Jar, lastModified, 10, fileAttributes ) );
        specFile.addFile( regularFile( relativePath( "/a" ), lastModified, 10, fileAttributes.addTag( "doc" ) ) );
        specFile.addFile( regularFile( relativePath( "/b" ), lastModified, 10, fileAttributes.addTag( "config" ) ) );
        specFile.addFile( regularFile( relativePath( "/c" ), lastModified, 10, fileAttributes.addTag( "rpm:missingok" ) ) );
        specFile.addFile( regularFile( relativePath( "/d" ), lastModified, 10, fileAttributes.addTag( "rpm:noreplace" ) ) );
        specFile.addFile( regularFile( relativePath( "/e" ), lastModified, 10, fileAttributes.addTag( "rpm:ghost" ) ) );
        specFile.apply( filter( extract2Jar, extract2JarAttributes ) );

        specFile.provides = List.list( "foo", "bar" );

        assertEquals( header.
            add( "Provides: foo" ).
            add( "Provides: bar" ).
            addAllLines( buildHeader.getLines() ).
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add( "%doc %attr(0644,myuser,mygroup) /a" ).
            add( "%config %attr(0644,myuser,mygroup) /b" ).
            add("").
            add( "%config(missingok) %attr(0644,myuser,mygroup) /c" ).
            add( "%config(noreplace) %attr(0644,myuser,mygroup) /d" ).
            add( "%ghost %attr(0644,myuser,mygroup) /e" ).
            add( "%attr(0644,myuser,mygroup) /extract.jar" ).
            add( "%attr(0644,extract,mygroup) /extract2.jar" ).
            add( "" ).
            add( "%dir %attr(0755,myuser,mygroup) /usr/bin" ).
            toString(), toString( specFile ) );
    }

    public void testDescriptionGeneration()
        throws Exception
    {
        SpecFile specFile = testSpecFile();

        specFile.description = "Yo yo";

        assertEquals( header.
            addAllLines( buildHeader.getLines() ).
            add().
            add( "%description" ).
            add( "Yo yo" ).
            add().
            add( "%files" ).
            toString(), toString( specFile ) );
    }

    public void testScriptGeneration()
        throws Exception
    {
        SpecFile specFile = testSpecFile();

        specFile.includePost = some( new File( "pom.xml" ) );

        assertEquals( header.
            addAllLines( buildHeader.getLines() ).
            add().
            add( "%description" ).
            add().
            add( "%files" ).
            add().
            add( "%post" ).
            add( "%include " + specFile.includePost.some().getAbsolutePath() ).toString(), toString( specFile ) );
    }

    public void testParseRpm()
    {
        LineFile dumpOutput = new LineFile();
        // This dump was generated from Centos 5.3 bind-sdb-9.3.4-10-P1.el5.i386.rpm
        // Some md5 sums has beed shortened to not mess up the formatting
        dumpOutput.
            add( "/etc/openldap/schema/dnszone.schema 5114 1232540847 2294a35240760043 0100644 root root 1 0 0 X" ).
            add( "/usr/sbin/ldap2zone 13620 1232540860 a9ac9badbe029be47132a5becb9e1f8b 0100755 root named 0 0 0 X" ).
            add( "/usr/sbin/named_sdb 390116 1232540860 0613cc2f0367554726b57ab03ba365db 0100755 root named 0 0 0 X" ).
            add( "/usr/sbin/zone2ldap 17832 1232540860 cb4b9d6e17e65ef88d0185a74d74f63b 0100755 root named 0 0 0 X" ).
            add( "/usr/sbin/zonetodb 13620 1232540860 fbc0ef188d07d448137d4c8f7f23c81c 0100755 root named 0 0 0 X" ).
            add( "/usr/share/doc/bind-sdb-9.3.4 4096 1232540861 0000000000000000000 040755 root named 0 0 0 X" ).
            add( "/usr/share/doc/bind-sdb-9.3.4/INSTALL.ldap 3792 1093565578 b0e1f35 0100644 root named 0 1 0 X" ).
            add( "/usr/share/doc/bind-sdb-9.3.4/README.ldap 2419 1093565579 45fbd89e246 0100644 root named 0 1 0 X" ).
            add( "/usr/share/doc/bind-sdb-9.3.4/README.sdb_pgsql 2590 1129753368 cffa8cf 0100644 root named 0 1 0 X" ).
            add( "/usr/share/man/man1/zone2ldap.1.gz 1168 1232540846 6e3f7430678f7 0100644 root named 0 1 0 X" ).
            toString();

        SpecFile specFile = new SpecFile();
        Directory defaultDirectory = UnixFsObject.directory( BASE, new LocalDateTime(), EMPTY );
        specFile.beforeAssembly( defaultDirectory );
        RpmUtil.RpmDumpParser parser = new RpmUtil.RpmDumpParser( specFile );

        for ( String line : dumpOutput )
        {
            parser.onLine( line );
        }

        PackageFileSystem<Object> fileSystem = specFile.getFileSystem();
        // Add the intermediate paths
        // BASE, etc, etc/openldap, etc/openldap/schema, usr, usr/bin, usr/share, usr/share/doc, usr/share/man, usr/share/man/man1
        assertEquals( dumpOutput.size() + 10, fileSystem.toList().toCollection().size() );
        assertTrue( fileSystem.hasPath( relativePath( "/etc/openldap/schema/dnszone.schema" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/sbin/ldap2zone" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/sbin/named_sdb" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/sbin/zone2ldap" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/sbin/zonetodb" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/share/doc/bind-sdb-9.3.4" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/share/doc/bind-sdb-9.3.4/INSTALL.ldap" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/share/doc/bind-sdb-9.3.4/README.ldap" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/share/doc/bind-sdb-9.3.4/README.sdb_pgsql" ) ) );
        assertTrue( fileSystem.hasPath( relativePath( "/usr/share/man/man1/zone2ldap.1.gz" ) ) );

        RelativePath zone2ldap = relativePath( "/usr/share/man/man1/zone2ldap.1.gz" );
        PackageFileSystemObject<Object> o = fileSystem.getObject( zone2ldap ).some();

        assertEquals( "root", o.getUnixFsObject().getFileAttributes().user.some() );
        assertEquals( "named", o.getUnixFsObject().getFileAttributes().group.some() );
        //noinspection OctalInteger
        assertEquals( fromInt( 0100644 ), o.getUnixFsObject().getFileAttributes().mode.some() );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private String toString( SpecFile specFile )
        throws Exception
    {
        LineFile spec = new LineFile();
        specFile.streamTo( spec );
        return spec.toString();
    }

    private SpecFile testSpecFile()
    {
        FileAttributes fileAttributes = EMPTY.user( "root" ).group( "root" );

        SpecFile specFile = new SpecFile();
        specFile.name = "groupid-artifactid";
        specFile.version = "1.0";
        specFile.release = "1";
        specFile.summary = "My summary";
        specFile.license = "License";
        specFile.group = "My Group";
        specFile.buildRoot = new File( "build-root" );
        specFile.buildArch = "noarch";
        specFile.beforeAssembly( UnixFsObject.directory( BASE, new LocalDateTime(), fileAttributes ) );
        return specFile;
    }

    private F<UnixFsObject, Option<UnixFsObject>> filter( final RelativePath path,
                                                          final FileAttributes newAttributes )
    {
        return new F<UnixFsObject, Option<UnixFsObject>>()
        {
            public Option<UnixFsObject> f( UnixFsObject object )
            {
                return !object.path.isBelowOrSame( path ) ?
                    Option.<UnixFsObject>none() :
                    some( object.setFileAttributes( object.attributes.useAsDefaultsFor( newAttributes ) ) );
            }
        };
    }
}
