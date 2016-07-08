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

import fj.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import junit.framework.*;
import com.stratio.mojo.unix.util.*;
import com.stratio.mojo.unix.util.line.*;

import static com.stratio.mojo.unix.deb.DebControlParser.DPKG_EOL;
import static com.stratio.mojo.unix.util.line.LineFile.*;

import java.io.*;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 */
public class DebControlParserTest
    extends TestCase
{
    private static final TestUtil testUtil = new TestUtil( DebControlParserTest.class );

    public void testParseSingleLineField()
    {
        LineFile file = (LineFile) new LineFile().
            add( "Field: 123" );

        List<String> lines = iterableList( file );
        P2<String, List<String>> p = DebControlParser.parseField( lines );

        assertEquals( "123", p._1() );
        assertTrue( p._2().isEmpty() );
    }

    public void testParseMultipleLineWhichEndsTheFileField()
    {
        LineFile file = new LineFile();
        file.
            add( "Field: 123" ).
            add( " 234" );

        List<String> lines = iterableList( file );
        P2<String, List<String>> p = DebControlParser.parseField( lines );

        assertTrue( p._2().isEmpty() );
        assertEquals( "123" + DPKG_EOL + "234", p._1() );
    }

    public void testParseMultipleLineWhichDoesNotEndsTheFileField()
    {
        LineFile file = new LineFile();
        file.
            add( "Field: 123" ).
            add( " 234" ).
            add( "Field2: abc" );

        List<String> lines = iterableList( file );
        P2<String, List<String>> p = DebControlParser.parseField( lines );

        assertEquals( 1, p._2().length() );
        assertEquals( "123" + DPKG_EOL + "234", p._1() );
    }

    public void testParseMultipleLineWithBlankLine()
    {
        LineFile file = new LineFile();
        file.
            add( "Field: 123" ).
            add( " ." ).
            add( " 234" ).
            add( "Field2: abc" );

        List<String> lines = iterableList( file );
        P2<String, List<String>> p = DebControlParser.parseField( lines );

        assertEquals( 1, p._2().length() );
        assertEquals( "123" + DPKG_EOL + DPKG_EOL + "234", p._1() );
    }

    public void testParseMultipleLineWithBlankLineLast()
    {
        LineFile file = new LineFile();
        file.
            add( "Field: 123" ).
            add( " 234" ).
            add( " ." ).
            add( "Field2: abc" );

        List<String> lines = iterableList( file );
        P2<String, List<String>> p = DebControlParser.parseField( lines );

        assertEquals( 1, p._2().length() );
        assertEquals( "123" + DPKG_EOL + "234" + DPKG_EOL, p._1() );
    }

    public void failingtestParseAnt()
        throws Exception
    {
        List<String> depends = list( "java-gcj-compat-dev | java-virtual-machine",
                                     "java-gcj-compat | java1-runtime | java2-runtime",
                                     "libxerces2-java" );

        ControlFile expectedControlFile = new ControlFile( "ant" ).
            version( some( "1.7.0-3" ) ).
            maintainer( some( "Ubuntu Core Developers <ubuntu-devel-discuss@lists.ubuntu.com>" ) ).
            installedSize( some( "1025" ) ).
            architecture( some( "all" ) ).
            priority( some( "optional" ) ).
            section( some( "devel" ) ).
            description( some( "Java based build tool like make\n" +
                "A system independent (i.e. not shell based) build tool that uses XML\n" +
                "files as \"Makefiles\". This package contains the scripts and the core\n" +
                "tasks libraries.\n" ) ).
            depends( depends ).
            recommends( list( "ant-optional", "ant-gcj" ) ).
            suggests( list( "ant-doc" ) ).
            conflicts( list( "libant1.6-java", "ant-doc (<= 1.6.5-1)" ) );

        assertControlFile( "src/test/resources/control/" + expectedControlFile.packageName + ".txt",
                           expectedControlFile );
    }

    public void failingtestParseBash()
        throws Exception
    {
        ControlFile expectedControlFile = new ControlFile( "bash" ).
            version( some( "3.2-0ubuntu16" ) ).
            maintainer( some( "Ubuntu Core developers <ubuntu-devel-discuss@lists.ubuntu.com>" ) ).
            installedSize( some( "1316" ) ).
            architecture( some( "amd64" ) ).
            priority( some( "required" ) ).
            section( some( "base" ) ).
            description( some( "The GNU Bourne Again SHell\n" +
                "Bash is an sh-compatible command language interpreter that executes\n" +
                "commands read from the standard input or from a file.  Bash also\n" +
                "incorporates useful features from the Korn and C shells (ksh and csh).\n" +
                "\n" +
                "Bash is ultimately intended to be a conformant implementation of the\n" +
                "IEEE POSIX Shell and Tools specification (IEEE Working Group 1003.2).\n" +
                "\n" +
                "The Programmable Completion Code, by Ian Macdonald, is now found in\n" +
                "the bash-completion package." ) ).
            depends( list( "base-files (>= 2.1.12)", "debianutils (>= 2.15)" ) ).
            recommends( single( "bash-completion (>= 20060301)" ) ).
            suggests( single( "bash-doc" ) );

        assertControlFile( "src/test/resources/control/" + expectedControlFile.packageName + ".txt",
            expectedControlFile );
    }

    public void failingtestParseLibc6()
        throws Exception
    {
        ControlFile expectedControlFile = new ControlFile( "libc6" ).
            version( some( "2.7-10ubuntu3" ) ).
            maintainer( some( "Ubuntu Core developers <ubuntu-devel-discuss@lists.ubuntu.com>" ) ).
            installedSize( some( "11348" ) ).
            architecture( some( "amd64" ) ).
            priority( some( "required" ) ).
            section( some( "base" ) ).
            description( some( "GNU C Library: Shared libraries\n" +
                "Contains the standard libraries that are used by nearly all programs on\n" +
                "the system. This package includes shared versions of the standard C library\n" +
                "and the standard math library, as well as many others." ) ).
            depends( single( "libgcc1" ) ).
            suggests( list( "locales", "glibc-doc" ) );

        assertControlFile( "src/test/resources/control/" + expectedControlFile.packageName + ".txt",
            expectedControlFile );
    }

    public void testGenerateA()
    {
        ControlFile controlFile = new ControlFile( "my-package" ).
            description( some( "GNU C Library: Shared libraries\n" +
                "Contains the standard libraries that are used by nearly all programs on the system. " +
                "This package includes shared versions of the standard C library and the standard math library, " +
                "as well as many others." ) );

        // TODO: This is just too hard for now
//        assertEquals( new LineFile().
//            add("Package: my-package").
//            add("Description: GNU C Library: Shared libraries").
//            add( " Contains the standard libraries that are used by nearly all programs on the").
//            add( " system. This package includes shared versions of the standard C library and").
//            add( " the standard math library, as well as many others.").toString(),
//                      LineFile.fromList( controlFile.toList() ).toString() );

        assertEquals( new LineFile( DPKG_EOL ).
            add( "Package: my-package" ).
            add( "Description: GNU C Library: Shared libraries" ).
            add( "Contains the standard libraries that are used by nearly all programs on the" +
                " system. This package includes shared versions of the standard C library and" +
                " the standard math library, as well as many others." ).toString(),
                LineFile.fromList( controlFile.toList() ).toString( DPKG_EOL ) );
    }

    private void assertControlFile( String path, ControlFile expectedControlFile )
        throws IOException
    {
        List<String> list = iterableList( fromFile( testUtil.getTestFile( path ) ) );

        ControlFile actualControlFile = new DebControlParser().parse( list );

        TestUtil.tester( Equal.stringEqual, Show.stringShow ).
            assertEquals( "version", expectedControlFile.version, actualControlFile.version ).
            assertEquals( "description", expectedControlFile.description, actualControlFile.description ).
            assertEquals( "maintainer", expectedControlFile.maintainer, actualControlFile.maintainer ).
            assertEquals( "installedSize", expectedControlFile.installedSize, actualControlFile.installedSize).
            assertEquals( "package", expectedControlFile.packageName, actualControlFile.packageName ).
            assertEquals( "architecture", expectedControlFile.architecture, actualControlFile.architecture ).
            assertEquals( "priority", expectedControlFile.priority, actualControlFile.priority ).
            assertEquals( "section", expectedControlFile.section, actualControlFile.section );

        TestUtil.tester( Equal.listEqual( Equal.stringEqual ), Show.listShow( Show.stringShow ) ).
            assertEquals( "depends", expectedControlFile.depends, actualControlFile.depends ).
            assertEquals( "recommends", expectedControlFile.recommends, actualControlFile.recommends ).
            assertEquals( "suggests", expectedControlFile.suggests, actualControlFile.suggests ).
            assertEquals( "preDepends", expectedControlFile.preDepends, actualControlFile.preDepends ).
            assertEquals( "replaces", expectedControlFile.replaces, actualControlFile.replaces );
    }
}
