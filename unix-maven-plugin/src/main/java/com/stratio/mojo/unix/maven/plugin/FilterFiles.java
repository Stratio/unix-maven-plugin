/**
 * Copyright (C) 2003 Stratio (http://stratio.com)
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
 package com.stratio.mojo.unix.maven.plugin;

/*
 * The MIT License
 *
 * Copyright 2012 The Codehaus.
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

import fj.data.List;
import org.apache.maven.plugin.*;
import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.core.*;
import com.stratio.mojo.unix.io.*;

import java.util.*;
import java.util.regex.*;

import static fj.data.List.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.io.LineEnding.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@SuppressWarnings("UnusedDeclaration")
public class FilterFiles
    extends AssemblyOp
{
    private List<String> includes = nil();

    private List<String> excludes = nil();

    private String lineEnding = keep.name();

    private Regex[] regexes = new Regex[0];

    public FilterFiles()
    {
        super( "filter-files" );
    }

    public void setIncludes( String[] includes )
    {
        this.includes = list( includes );
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = list( excludes );
    }

    public void setLineEnding( String lineEnding )
    {
        this.lineEnding = lineEnding;
    }

    public void setRegexes( Regex[] regexes )
    {
        this.regexes = regexes;
    }

    public AssemblyOperation createOperation( CreateOperationContext context )
        throws MojoFailureException, UnknownArtifactException
    {
        LineEnding lineEnding1 = valueOf();

        List<Replacer> replacers;
        if ( regexes.length > 0 )
        {
            replacers = regexReplacers();
        }
        else
        {
            replacers = propertyReplacers( context.project.properties );
        }

        return new FilterFilesOperation( includes, excludes, replacers, lineEnding1 );
    }

    private LineEnding valueOf()
        throws MojoFailureException
    {
        try
        {
            return LineEnding.valueOf( lineEnding );
        }
        catch ( IllegalArgumentException e )
        {
            throw new MojoFailureException( "Unknown line ending: " + this.lineEnding );
        }
    }

    /**
     * See comment in mavenProjectWrapper on the keys.
     *
     * @see MavenProjectWrapper#mavenProjectWrapper
     */
    private List<Replacer> propertyReplacers( Map<String, String> properties )
        throws MojoFailureException
    {
        List<Replacer> replacers = nil();

        for ( Map.Entry<String, String> entry : properties.entrySet() )
        {
            String key = "${" + entry.getKey() + "}";

            try
            {
                // This needs to be quoted, patterns like "${project.version}" are not very useful regular expressions.
                String pattern = Pattern.quote( key );

                replacers = replacers.cons( new Replacer( pattern, entry.getValue() ) );
            }
            catch ( PatternSyntaxException e )
            {
                throw new MojoFailureException( "Illegal pattern: " + key );
            }
        }

        return replacers;
    }

    private List<Replacer> regexReplacers()
    {
        List<Replacer> replacers = nil();

        Replacer[] rs = new Replacer[regexes.length];
        for ( int i = regexes.length - 1; i >= 0; i-- )
        {
            replacers = replacers.cons( regexes[i].toReplacer() );
        }

        return replacers.reverse();
    }
}
