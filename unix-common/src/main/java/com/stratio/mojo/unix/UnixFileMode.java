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
import fj.data.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class UnixFileMode
{
    /**
     * Mode equal to <code>-rw-r--r--</code>.
     */
    @SuppressWarnings( {"OctalInteger"} )
    public static final UnixFileMode _0644 = UnixFileMode.fromInt( 0644 );

    /**
     * Mode equal to <code>-rwxr-xr-x</code>.
     */
    @SuppressWarnings( {"OctalInteger"} )
    public static final UnixFileMode _0755 = UnixFileMode.fromInt( 0755 );

    /**
     * Mode equal to <code>-rwxrwxrwx</code>.
     */
    @SuppressWarnings( {"OctalInteger"} )
    public static final UnixFileMode _0777 = UnixFileMode.fromInt( 0777 );

    /**
     * Special file modes used for symbolic links (lrwxrwxrwx)
     */
    @SuppressWarnings( {"OctalInteger"} )
    public static final UnixFileMode _SYMLINK = UnixFileMode.fromInt( 0777 );

    public static final Option<UnixFileMode> none = Option.none();

    public static final F<UnixFileMode, String> showLong = new F<UnixFileMode, String>()
    {
        public String f( UnixFileMode unixFileMode )
        {
            return unixFileMode.toString();
        }
    };

    public static final F<UnixFileMode, String> showOcalString = new F<UnixFileMode, String>()
    {
        public String f( UnixFileMode unixFileMode )
        {
            return unixFileMode.toOctalString();
        }
    };

    private final int mode;

    public UnixFileMode( int mode )
    {
        this.mode = mode;
    }

    public int toInt()
    {
        return mode;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        UnixFileMode that = (UnixFileMode) o;

        return mode == that.mode;
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder( "         " );
        buffer.setCharAt( 0, (mode & 0x100) > 0 ? 'r' : '-' );
        buffer.setCharAt( 1, (mode & 0x080) > 0 ? 'w' : '-' );
        buffer.setCharAt( 2, (mode & 0x040) > 0 ? 'x' : '-' );
        buffer.setCharAt( 3, (mode & 0x020) > 0 ? 'r' : '-' );
        buffer.setCharAt( 4, (mode & 0x010) > 0 ? 'w' : '-' );
        buffer.setCharAt( 5, (mode & 0x400) > 0 ? 's' : (mode & 0x008) > 0 ? 'x' : '-' );
        buffer.setCharAt( 6, (mode & 0x004) > 0 ? 'r' : '-' );
        buffer.setCharAt( 7, (mode & 0x002) > 0 ? 'w' : '-' );
        buffer.setCharAt( 8, (mode & 0x001) > 0 ? 'x' : '-' );

        return buffer.toString();
    }

    public String toOctalString()
    {
        if ( mode < 10 )
        {
            return "000" + Integer.toOctalString( toInt() );
        }
        else if ( mode < 100 )
        {
            return "00" + Integer.toOctalString( toInt() );
        }
        else if ( mode < 1000 )
        {
            return "0" + Integer.toOctalString( toInt() );
        }

        return "0" + Integer.toOctalString( toInt() );
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static UnixFileMode fromInt( int mode )
    {
        return new UnixFileMode( mode );
    }

    public static UnixFileMode fromString( String string )
    {
        // TODO: it really should support all bits and be able to parse them. Lots of figuring out to do here...
        if ( string.length() != 9 )
        {
            throw new RuntimeException( "Illegal string format; string.length has to be 9 characters" );
        }

        int mode = 0;

        mode += expect( string, 0, 'r' );
        mode += expect( string, 1, 'w' );
        mode += expect( string, 2, 'x' );
        mode += expect( string, 3, 'r' );
        mode += expect( string, 4, 'w' );
        mode += expect( string, 5, 'x' );
        mode += expect( string, 6, 'r' );
        mode += expect( string, 7, 'w' );
        mode += expect( string, 8, 'x' );

        return new UnixFileMode( mode );
    }

    private static int expect( String string, int i, char one )
    {
        char c = string.charAt( i );

        if ( c == '-' )
        {
            return 0;
        }
        else if ( c == one )
        {
            return 1 << 8 - i;
        }

        throw new RuntimeException( "Illegal format, expected '-' or '" + c + "' at position " + i + "." );
    }
}
