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
 package com.stratio.mojo.unix.util;

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
import static com.stratio.mojo.unix.util.line.LineStreamWriter.*;

import java.lang.Class;
import java.io.*;
import java.net.*;

public class TestUtil
{
    private final Class klass;

    public TestUtil( Object object )
    {
        this.klass = object.getClass();
    }

    public TestUtil( Class klass )
    {
        this.klass = klass;
    }

    public String getTestPath( String path )
    {
        return getTestFile( path ).getAbsolutePath();
    }

    public File getTestFile( String path )
    {
        String b = System.getProperty( "basedir" );

        if ( b != null )
        {
            return new File( b, path ).getAbsoluteFile();
        }

        try
        {
            URL root = klass.getResource( "/" );

            File file = new File( root.toURI().getPath() );

            if ( file.getName().equals( "test-classes" ) || file.getName().equals( "classes" ) )
            {
                return new File( file.getParentFile().getParentFile(), path );
            }

            return new File( path ).getAbsoluteFile();
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static <T> Tester<T> tester( Equal<T> equal, Show<T> show )
    {
        return new Tester<T>( equal, show );
    }

    public static class Tester<T>
    {
        public final Equal<T> equal;

        public final Show<T> show;

        public Tester( Equal<T> equal, Show<T> show )
        {
            this.equal = equal;
            this.show = show;
        }

        public Tester<T> assertEquals( String message, T expected, T actual )
        {
            if ( !equal.eq( expected, actual ) )
            {
                throw new RuntimeException( message + "." + EOL +
                    "Expected: " + show.showS( expected ) + EOL +
                    "Actual: " + show.showS( actual ) );
            }

            return this;
        }

        public Tester<T> assertEquals( String message, Option<T> expected, Option<T> actual )
        {
            if ( expected.isNone() )
            {
                if ( actual.isSome() )
                {
                    throw new RuntimeException( message + ". Expected None, was Some: " + show.showS( actual.some() ) );
                }

                return this;
            }

            if ( expected.isSome() && actual.isNone() )
            {
                throw new RuntimeException( message + ". Expected Some, was None." );
            }

            if ( !equal.eq( expected.some(), actual.some() ) )
            {
                throw new RuntimeException( message + "." + EOL +
                    "Expected " + show.showS( expected.some() ) + EOL +
                    "Actual: " + show.showS( actual.some() ) );
            }

            return this;
        }
    }
}
