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
 package com.stratio.mojo.unix.io;

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
import static fj.P.*;
import fj.data.List;
import fj.data.*;
import static fj.data.Option.*;
import static fj.data.Stream.*;
import static java.util.Arrays.*;
import com.stratio.mojo.unix.io.*;
import static com.stratio.mojo.unix.io.IncludeExcludeFilter.*;
import static com.stratio.mojo.unix.util.RelativePath.*;

import java.io.*;
import java.util.*;

public class FileScanner
{
    private final File file;

    private final String absolutePath;

    private final IncludeExcludeFilter selector;

    public FileScanner( File file, String[] includes, String[] excludes )
        throws IOException
    {
        this.file = file.getAbsoluteFile();

        if ( !file.isDirectory() )
        {
            throw new IOException( "Not a directory." );
        }

        selector = includeExcludeFilter().
            addStringIncludes( includes == null ? Collections.<String>emptyList() : asList( includes ) ).
            addStringExcludes( excludes == null ? Collections.<String>emptyList() : asList( excludes ) ).
            create();

        absolutePath = file.getAbsolutePath();
    }

    public Stream<File> toStream()
    {
        class State
        {
            private List<File> stack;

            public State( File file )
            {
                stack = List.single( file );
            }
        }

        return unfold( new F<State, Option<P2<File, State>>>()
        {
            public Option<P2<File, State>> f( State state )
            {
                if ( state.stack.isEmpty() )
                {
                    return none();
                }

                String s;
                while ( state.stack.isNotEmpty() )
                {
                    File next = state.stack.head();
                    state.stack = state.stack.drop( 1 );

                    if ( next.isDirectory() )
                    {
                        File[] files = next.listFiles();
                        for ( File file : files )
                        {
                            state.stack = state.stack.cons( file );
                        }
                    }

                    if ( selector.matches( relativePathFromFiles( file, next ) ) )
                    {
                        return some( p( next, state ) );
                    }
                }

                return none();
            }
        }, new State( file ) );
    }
}
