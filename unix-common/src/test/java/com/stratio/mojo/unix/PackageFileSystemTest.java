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

import fj.*;
import fj.data.List;
import static fj.data.List.*;
import fj.data.*;
import static fj.data.Option.*;
import junit.framework.*;
import static com.stratio.mojo.unix.PackageFileSystem.*;
import static com.stratio.mojo.unix.UnixFileMode.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;

/**
 * TODO: Assert the creation of parent directories.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PackageFileSystemTest
    extends TestCase
{
    static LocalDateTime lm = new LocalDateTime( 2009, 2, 24, 9, 42 );

    static List<Replacer> nil = nil();

    static Option<String> mygroup = some( "mygroup" );

    static Option<String> myuser = Option.some( "myuser" );

    static FileAttributes fileA = new FileAttributes( myuser, mygroup, some( _0644 ) );

    static FileAttributes directoryA = new FileAttributes( myuser, mygroup, some( _0755 ) );

    static PlainPackageFileSystemObject root = p( directory( RelativePath.BASE, lm, directoryA ) );

    static PlainPackageFileSystemObject a = p( directory( relativePath( "/a" ), lm, directoryA ) );

    static PlainPackageFileSystemObject b = p( directory( relativePath( "/b" ), lm, directoryA.mode( UnixFileMode.none ) ) );

    static PlainPackageFileSystemObject a_x = p( regularFile( relativePath( "/a/a-x" ), lm, 10, fileA ) );

    static PlainPackageFileSystemObject a_y = p( regularFile( relativePath( "/a/a-y" ), lm, 10, fileA ) );

    static PlainPackageFileSystemObject b_x = p( regularFile( relativePath( "/b/b-x" ), lm, 10, fileA ) );

    static PlainPackageFileSystemObject c = p( directory( relativePath( "/c" ), lm, directoryA ) );

    static PlainPackageFileSystemObject c_x = p( directory( relativePath( "/c/c-x" ), lm, directoryA ) );

    static PlainPackageFileSystemObject c_x_u = p( regularFile( relativePath( "/c/c-x/c-x-u" ), lm, 10, fileA ) );

    Show<Stream<PackageFileSystemObject<Object>>> fsShow = Show.streamShow( Show.showS( new F<PackageFileSystemObject<Object>, String>()
    {
        public String f( PackageFileSystemObject o )
        {
            return o.getUnixFsObject().toString() + "\n";
        }
    } ) );

    public void testAddSingleNodeToRoot()
    {
        PackageFileSystem<Object> fileSystem = PackageFileSystem.<Object>create( root, root ).
            addDirectory( a );

        assertEquals( new LineFile().
            add( "." ).
            add( "    a" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );

        fsShow.println( fileSystem.toList() );
    }

    public void testAddingNestedDirectoryWithParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
//            addDirectory( c ).
            addDirectory( c_x );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    c" ).
            add( "        c-x" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testAddingNestedDirectoryWithoutParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( c_x );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    c" ).
            add( "        c-x" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testAddingNestedDirectoryWithOnlyOneRelatedParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( c ).
            addFile( c_x_u );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    c" ).
            add( "        c-x" ).
            add( "            c-x-u" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testAddingNestedFileWithUnrelatedParentDirectoryPresent()
    {
        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( b ).
            addFile( c_x_u );

        fsShow.println( fileSystem.toList() );

        assertEquals( new LineFile().
            add( "." ).
            add( "    b" ).
            add( "    c" ).
            add( "        c-x" ).
            add( "            c-x-u" ).
            toString(), PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ).toString() );
    }

    public void testBasic()
    {
        FileAttributes tjoho = a_x.getUnixFsObject().getFileAttributes().user( "tjoho" );

        PackageFileSystem<Object> fileSystem = create( root, root ).
            addDirectory( b ).
            addDirectory( a ).
            addFile( a_y ).
            addFile( a_x ).
            addFile( b_x ).
            addFile( c_x_u ).
            apply( filter( "a/", fileA.user( "tjoho" ) ) );

        System.out.println( "-----------------------------------" );
        System.out.print( PackageFileSystemFormatter.<Object>flatFormatter().print( fileSystem ) );
        System.out.println( "-----------------------------------" );

        fsShow.println( fileSystem.toList() );

        Stream<PackageFileSystemObject<Object>> actual = fileSystem.prettify().toList();
        assertEquals( 9, actual.length() );
        int i = 0;
        assertEquals( root.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( a.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( a_x.getUnixFsObject().setFileAttributes( tjoho ), actual.index( i++ ).getUnixFsObject() );
        assertEquals( a_y.getUnixFsObject().setFileAttributes( tjoho ), actual.index( i++ ).getUnixFsObject() );
        assertEquals( b.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( b_x.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( c.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( c_x.getUnixFsObject(), actual.index( i++ ).getUnixFsObject() );
        assertEquals( c_x_u.getUnixFsObject(), actual.index( i ).getUnixFsObject() );
    }

    public void testMutatingRootNode()
    {
        PackageFileSystem<Object> fs = create( root, root );

        assertEquals( root.getUnixFsObject(), fs.getObject( RelativePath.BASE ).some().getUnixFsObject() );

        FileAttributes newAttributes = root.getUnixFsObject().getFileAttributes().user( "woot" );

        fs = fs.addDirectory( p( directory( RelativePath.BASE, lm, newAttributes ) ) );

        assertEquals( newAttributes, fs.getObject( RelativePath.BASE ).some().getUnixFsObject().getFileAttributes() );
    }

    private F<UnixFsObject, Option<UnixFsObject>> filter( final String s,
                                                  final FileAttributes newAttributes )
    {
        return new F<UnixFsObject, Option<UnixFsObject>>()
        {
            public Option<UnixFsObject> f( UnixFsObject fsObject )
            {
                return !fsObject.path.string.startsWith( s )
                    ? Option.<UnixFsObject>none()
                    : some( fsObject.setFileAttributes( fsObject.attributes.useAsDefaultsFor( newAttributes ) ) );
            }
        };
    }

    public static PlainPackageFileSystemObject p( UnixFsObject unixFsObject )
    {
        return new PlainPackageFileSystemObject( unixFsObject );
    }
}
