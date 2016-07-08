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
import static fj.Function.curry;
import static fj.P.p;
import fj.data.*;
import static com.stratio.mojo.unix.java.StringF.leftPad;
import static com.stratio.mojo.unix.java.StringF.rightPad;
import com.stratio.mojo.unix.util.line.*;
import org.codehaus.plexus.util.*;

public abstract class PackageFileSystemFormatter<A>
{
    /*
    TODO: Create strategies for printing in "flat", "flat with details" and "tree" modes

    flat:
    .
    a
    b
    c
    c/c-x
    c/c-x/c-x-r

    flat with details:
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 .
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 a
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 b
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 c
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 c/c-x
    drwxr-xr-x     myuser    mygroup          0 Feb 09:42 c/c-x/c-x-r

    tree:
    .
    |-- a
    |-- b
    `-- c
        `-- c-x
            `-- c-x-r
    */

    private PackageFileSystemFormatter()
    {
    }

    // TODO: This should be streaming instead of creating an entire collection
    public LineFile print( PackageFileSystem<A> fs )
    {
        LineFile lines = new LineFile();

        Tree<PackageFileSystemObject<A>> tree = fs.prettify().getTree();

        lines.add( "." );

        print( 1, lines, tree.subForest()._1() );

        return lines;
    }

    protected abstract void print( int i, LineFile lines, Stream<Tree<PackageFileSystemObject<A>>> children );

    public static <A> PackageFileSystemFormatter<A> flatFormatter()
    {
        return new PackageFileSystemFormatter<A>()
        {
            protected void print( int i, LineFile lines, Stream<Tree<PackageFileSystemObject<A>>> children )
            {
                String indent = StringUtils.repeat( "    ", i );
                i++;

                for ( Tree<PackageFileSystemObject<A>> child : children )
                {
//                    UnixFsObject unixFsObject = child.root().getUnixFsObject();
//                    lines.add( indent + unixFsObject.path.name() + ", filters=" + unixFsObject.filters );
                    lines.add( indent + child.root().getUnixFsObject().path.name() );
                    print( i, lines, child.subForest()._1() );
                }
            }
        };
    }

    public static <A> PackageFileSystemFormatter<A> detailedFormatter()
    {
        return new PackageFileSystemFormatter<A>()
        {
            protected void print( int i, LineFile lines, Stream<Tree<PackageFileSystemObject<A>>> children )
            {
                String indent = StringUtils.repeat( "    ", i );
                i++;

                for ( Tree<PackageFileSystemObject<A>> child : children )
                {
                    UnixFsObject o = child.root().getUnixFsObject();
                    FileAttributes attributes = o.getFileAttributes();

                    F<String, String> leftPad10 = curry( leftPad, 10 );
                    F<String, String> rightPad10 = curry( rightPad, 10 );

                    String line = '-' + attributes.mode.map( UnixFileMode.showLong ).orSome( "<unknown>" ) +
                        " " + attributes.user.map( leftPad10 ).orSome( " <unknown>" ) +
                        " " + attributes.group.map( leftPad10 ).orSome( " <unknown>" ) +
                        " " + p( String.valueOf( o.size ) ).map( rightPad10 )._1() +
                        " " + p( o.lastModified ).map( UnixFsObject.formatter )._1() +
                        " " + attributes.toString() + indent + o.path.name();

                    lines.add( line );
                    print( i, lines, child.subForest()._1() );
                }
            }
        };
    }
}
