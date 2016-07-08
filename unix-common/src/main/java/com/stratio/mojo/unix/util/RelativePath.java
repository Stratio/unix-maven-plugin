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
import fj.data.List;

import java.io.File;
import java.util.*;

import static fj.data.List.*;
import static fj.data.Option.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class RelativePath
    implements Comparable<RelativePath>
{
    public final String string;

    private static final char PATH_SEPARATOR;

    static
    {
        String s = System.getProperty( "file.separator" );
        if ( s.length() != 1 )
        {
            throw new RuntimeException( "Unsupported platform, file.separator has to be exactly one character long" );
        }
        PATH_SEPARATOR = s.charAt( 0 );
    }

    public final static RelativePath BASE = new RelativePath( "." )
    {
        public RelativePath add( String string )
        {
            String cleaned = clean( string );

            if ( cleaned == null )
            {
                return this;
            }

            return new RelativePath( cleaned );
        }

        public RelativePath add( RelativePath relativePath )
        {
            return relativePath;
        }

        public RelativePath parent()
        {
            throw new IllegalStateException( "parent() on BASE" );
        }

        public boolean isBelowOrSame( RelativePath other )
        {
            return other.isBase();
        }

        public Option<RelativePath> subtract( RelativePath parent )
        {
            if ( parent.isBase() )
            {
                return some( BASE );
            }

            return none();
        }

        public List<String> toList()
        {
            return nil();
        }

        public String asAbsolutePath( String basePath )
        {
            return basePath;
        }

        public boolean isBase()
        {
            return true;
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static final Ord<RelativePath> ord = Ord.ord( new F<RelativePath, F<RelativePath, Ordering>>()
    {
        public F<RelativePath, Ordering> f( final RelativePath a )
        {
            return new F<RelativePath, Ordering>()
            {
                public Ordering f( RelativePath b )
                {
                    return Ord.stringOrd.compare( a.string, b.string );
                }
            };
        }
    } );

    public static final Comparator<RelativePath> comparator = new Comparator<RelativePath>()
    {
        public int compare( RelativePath a, RelativePath b )
        {
            return a.string.compareTo( b.string );
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private RelativePath( String string )
    {
        if ( string.contains( "\\" ) )
        {
            throw new IllegalStateException( "A relative path can't contain '\\'." );
        }

        this.string = string;
    }

    public RelativePath add( String string )
    {
        string = clean( string );

        if ( string == null )
        {
            return this;
        }

        return new RelativePath( this.string + "/" + string );
    }

    public RelativePath add( RelativePath relativePath )
    {
        return new RelativePath( this.string + "/" + relativePath.string );
    }

    public RelativePath parent()
    {
        int i = this.string.lastIndexOf( '/' );

        if( i >= 0 )
        {
            return new RelativePath( string.substring( 0, i ) );
        }

        return BASE;
    }

    public String asAbsolutePath( String basePath )
    {
        return basePath + (basePath.endsWith( "/" ) ? "" : "/") + string;
    }

    public boolean isBase()
    {
        return false;
    }

    public String name()
    {
        int i = string.lastIndexOf( '/' );

        if ( i == -1 )
        {
            return string;
        }

        return string.substring( i + 1 );
    }

    /**
     * Returns true if <code>other</code> is further down the path than this path.
     * <p/>
     * <ul>
     * <li>".".isBelowOrSame(..) -> true. Everything is below the base path</li>
     * <li>"..".isBelowOrSame(".") -> true. Everything is below the base path</li>
     * <li>"/foo".isBelowOrSame( "/foo") -> true</li>
     * <li>"/foo/bar".isBelowOrSame( "/foo") -> true</li>
     * <li>"/foo".isBelowOrSame( "/foo/bar") -> false</li>
     * </ul>
     */
    public boolean isBelowOrSame( RelativePath parent )
    {
        return parent.isBase() || // Everything is below or equal to the base path
            string.startsWith( parent.string );
    }

    public Option<RelativePath> subtract( RelativePath parent )
    {
        if ( isBelowOrSame( parent ) )
        {
            if ( parent.isBase() || this.string.equals( parent.string ) )
            {
                return some( this );
            }

            return some( new RelativePath( this.string.substring( parent.string.length() + 1 ) ) );
        }

        return none();
    }

    public List<String> toList()
    {
        int i = string.lastIndexOf( '/' );

        if ( i == -1 )
        {
            return List.single( string );
        }

        List<String> list = List.single( string.substring( i + 1 ) );

        String s = string.substring( 0, i );

        do
        {
            i = s.lastIndexOf( '/' );

            if ( i == -1 )
            {
                return list.cons( s );
            }

            list = list.cons( s.substring( i + 1 ) );

            s = s.substring( 0, i );
        }
        while ( true );
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static RelativePath relativePath( String string )
    {
        string = string == null ? "/" : string.trim();

        String s = clean( string );

        if ( s == null )
        {
            return BASE;
        }

        return new RelativePath( s );
    }

    static String clean( final String string )
    {
        String s = removeDuplicateSlashes( string );

        if ( isRoot( s ) )
        {
            return null;
        }

        if ( s.startsWith( "./" ) )
        {
            s = s.substring( 2 );
        }
        else if ( s.startsWith( "/" ) )
        {
            s = s.substring( 1 );
        }

        if ( s.endsWith( "/." ) )
        {
            s = s.substring( 0, s.length() - 2 );
        }
        else if ( s.endsWith( "/" ) )
        {
            s = s.substring( 0, s.length() - 1 );
        }

        if ( isRoot( s ) )
        {
            return null;
        }

        return s;
    }

    private static String removeDuplicateSlashes( String string )
    {
        StringBuffer buffer = new StringBuffer();

        boolean lastWasSlash = false;

        for ( int i = 0; i < string.length(); i++ )
        {
            char c = string.charAt( i );

            if ( c == '/' )
            {
                if ( !lastWasSlash )
                {
                    buffer.append( c );
                    lastWasSlash = true;
                }
            }
            else
            {
                buffer.append( c );
                lastWasSlash = false;
            }
        }

        return buffer.toString();
    }

    private static boolean isRoot( String s )
    {
        return s.length() == 0 || s.equals( "/" ) || s.equals( "." );
    }

    public static RelativePath relativePathFromFiles( File parent, File child )
    {
        String c = child.getAbsolutePath();
        String p = parent.getAbsolutePath();

        if ( !c.startsWith( p ) )
        {
            throw new RuntimeException( "Not a child path." );
        }

        String s = c.substring( p.length() );

        if ( PATH_SEPARATOR != '/' )
        {
            s = s.replace( PATH_SEPARATOR, '/' );
        }

        return relativePath( s );
    }

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !(o instanceof RelativePath) )
        {
            return false;
        }

        RelativePath path = (RelativePath) o;

        return string.equals( path.string );
    }

    public int hashCode()
    {
        return string.hashCode();
    }

    public String toString()
    {
        return string;
    }

    // -----------------------------------------------------------------------
    // Comparable
    // -----------------------------------------------------------------------

    public int compareTo( RelativePath other )
    {
        return string.compareTo( other.string );
    }
}
