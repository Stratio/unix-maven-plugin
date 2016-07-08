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
import fj.data.*;
import static fj.data.List.*;
import static com.stratio.mojo.unix.deb.ControlFile.*;

import java.io.*;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 */
public class DebControlParser
{
    /**
     * Dpkg should always use '\n' as line separator. I can't find this in any documentation, but that's the only
     * thing that makes sense.
     */
    public static final String DPKG_EOL = "\n";

    public ControlFile parse( List<String> rest )
        throws IOException
    {
        List<P2<String, String>> values = nil();

        while ( rest.isNotEmpty() )
        {
            String line = rest.head();

            int i = line.indexOf( ':' );

            if ( i >= line.length() + 2 )
            {
                rest = rest.drop( 1 );
                continue;
            }

            String field = line.substring( 0, i );

            P2<String, List<String>> p = parseField( rest );

            String value = p._1();
            rest = p._2();

            values = values.cons( P.p( field, value ) );
        }

        return controlFileFromList( values );
    }

    public static P2<String, List<String>> parseField( List<String> rest )
    {
        String line = rest.head();
        rest = rest.drop( 1 );

        String value = line.substring( line.indexOf( ':' ) + 2 );

        while ( rest.isNotEmpty() )
        {
            line = rest.head();

            if ( !line.startsWith( " " ) )
            {
                return p( value, rest );
            }

            if ( line.equals( " ." ) )
            {
                value += DPKG_EOL;
            }
            else
            {
                value += DPKG_EOL + line.substring( 1 );
            }

            rest = rest.drop( 1 );
        }

        return p( value.trim(), rest );
    }
}
