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
 package com.stratio.mojo.unix.java;

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
import static fj.data.List.*;
import org.codehaus.plexus.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class StringF
{
    public static final F2<String, String, Boolean> equals = new F2<String, String, Boolean>()
    {
        public Boolean f( final String a, final String b )
        {
            return a.equals( b );
        }
    };

    public static final F2<String, String, String> concat = new F2<String, String, String>()
    {
        public String f( final String a, final String b )
        {
            return a.concat( b );
        }
    };

    public static final F<String, Boolean> isEmpty = new F<String, Boolean>()
    {
        public Boolean f( final String string )
        {
            return string.length() == 0;
        }
    };

    public static final F<String, Boolean> isNotEmpty = new F<String, Boolean>()
    {
        public Boolean f( final String string )
        {
            return string.length() != 0;
        }
    };

    public static final F3<String, String, String, String> replaceAll = new F3<String, String, String, String>()
    {
        public String f( final String string, final String regex, final String replacement )
        {
            return string.replaceAll( regex, replacement );
        }
    };

    public static final F<String, F<String, List<String>>> split = new F<String, F<String, List<String>>>()
    {
        public F<String, List<String>> f( final String string )
        {
            return new F<String, List<String>>()
            {
                public List<String> f( String regex )
                {
                    return list( string.split( regex ) );
                }
            };
        }
    };

    public static final F2<String, String, Boolean> startsWith = new F2<String, String, Boolean>()
    {
        public Boolean f( final String string, final String startsWith )
        {
            return string.startsWith( startsWith );
        }
    };

    public static final F<String, String> trim = new F<String, String>()
    {
        public String f( final String a )
        {
            return a.trim();
        }
    };

    // -----------------------------------------------------------------------
    // Extra
    // -----------------------------------------------------------------------

    public static final F<String, F<String, F<String, String>>> joiner = new F<String, F<String, F<String, String>>>()
    {
        public F<String, F<String, String>> f( final String separator )
        {
            return new F<String, F<String, String>>()
            {
                public F<String, String> f( final String a )
                {
                    return new F<String, String>()
                    {
                        public String f( String b )
                        {
                            return a + separator + b;
                        }
                    };
                }
            };
        }
    };

    public static final F2<Integer, String, String> leftPad = new F2<Integer, String, String>()
    {
        public String f( Integer size, String s )
        {
            return StringUtils.leftPad( s, size );
        }
    };

    public static final F2<Integer, String, String> rightPad = new F2<Integer, String, String>()
    {
        public String f( Integer size, String s )
        {
            return StringUtils.leftPad( s, size );
        }
    };
}
