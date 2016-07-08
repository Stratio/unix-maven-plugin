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

import junit.framework.*;
import static com.stratio.mojo.unix.io.IncludeExcludeFilter.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PathExpressionTest
    extends TestCase
{
    public void testBasic()
    {
        String[] patterns = new String[]{
            "^.*/[^/]*~$",                  // **/*~
            "^.*/#[^/]*#$",                 // **/#*#
            "^.*/\\.#[^/]*$",               // **/.#*
            "^.*/%[^/]*%$",                 // **/%*%
            "^.*/\\._[^/]*$",               // **/._*
            "^.*/CVS$",                     // **/CVS
            "^.*/CVS/.*$",                  // **/CVS/**
            "^.*/\\.cvsignore$",            // **/.cvsignore
            "^.*/SCCS$",                    // **/SCCS
            "^.*/SCCS/.*$",                 // **/SCCS/**
            "^.*/vssver\\.scc$",            // **/vssver.scc
            "^.*/\\.svn$",                  // **/.svn
            "^.*/\\.svn/.*$",               // **/.svn/**
            "^.*/\\.DS_Store$",             // **/.DS_Store
            "^.*/META-INF$",                // **/META-INF
            "^.*/META-INF/.*$"              // **/META-INF/**
        };

        assertEquals( DEFAULT_EXCLUDES.length, patterns.length );

        for ( int i = 0; i < DEFAULT_EXCLUDES.length; i++ )
        {
            String pattern = DEFAULT_EXCLUDES[i].getPattern().pattern();
            if ( !pattern.equals( patterns[i] ) )
            {
                System.out.println( i + ": ok=" + pattern.equals( patterns[i] ) + ", expression = " + DEFAULT_EXCLUDES[i].getExpression() + ", expected = " + patterns[i] + ", actual = " + pattern );
            }
            assertEquals( patterns[i], DEFAULT_EXCLUDES[i].getPattern().pattern() );
        }

        assertEquals( "^/[^/]*\\.java$", new PathExpression( "*.java" ).getPattern().pattern() );
        assertEquals( "^/copyright$", new PathExpression( "copyright" ).getPattern().pattern() );
    }
}
