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

import com.stratio.mojo.unix.util.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class IncludeExcludeFilter
{
    public static final PathExpression[] DEFAULT_EXCLUDES = new PathExpression[]{
        new PathExpression( "**/*~" ),
        new PathExpression( "**/#*#" ),
        new PathExpression( "**/.#*" ),
        new PathExpression( "**/%*%" ),
        new PathExpression( "**/._*" ),
        new PathExpression( "**/CVS" ),
        new PathExpression( "**/CVS/**" ),
        new PathExpression( "**/.cvsignore" ),
        new PathExpression( "**/SCCS" ),
        new PathExpression( "**/SCCS/**" ),
        new PathExpression( "**/vssver.scc" ),
        new PathExpression( "**/.svn" ),
        new PathExpression( "**/.svn/**" ),
        new PathExpression( "**/.DS_Store" ),
        new PathExpression( "**/META-INF" ),
        new PathExpression( "**/META-INF/**" )
    };

    private final Collection<PathExpression> includes;
    private final Collection<PathExpression> excludes;

    IncludeExcludeFilter( Collection<PathExpression> includes,
                          Collection<PathExpression> excludes )
    {
        this.includes = includes;
        this.excludes = excludes;
    }

    /*
    public boolean includeFile( FileSelectInfo fileSelectInfo )
        throws Exception
    {
        FileObject fileObject = fileSelectInfo.getFile();
        FileName name = fileObject.getName();

        if ( fileType != null && fileObject.getType() != fileType )
        {
            return false;
        }

        String relativePath = root.getRelativeName( name );

        return matches( relativePath );
    }
    */

    public boolean matches( RelativePath path )
    {
        // -----------------------------------------------------------------------
        // Make sure that the relative path always starts with a leading slash as
        // people want to match against "**/.svn**" which would not match the
        // root .svn directory.
        // -----------------------------------------------------------------------

        String s = path.asAbsolutePath( "/" );

        boolean include;

        if ( includes.size() == 0 )
        {
            // Default to including the file is the list of includes is empty
            include = true;

            include = matchesAny( include, false, s, excludes );
        }
        else
        {
            // Default to not including the file if the list of includes is non-empty
            include = false;

            include = matchesAny( include, true, s, includes );

            include = matchesAny( include, false, s, excludes );
        }

        return include;
    }

    private boolean matchesAny( boolean include, boolean returnIfMatch, String relative, Iterable<PathExpression> pathExpressions )
    {
        for ( PathExpression pathExpression : pathExpressions )
        {
            if ( pathExpression.matches( relative ) )
            {
                return returnIfMatch;
            }
        }

        return include;
    }

    public static class TemplateIncludeExcludeFilter
    {
        private List<PathExpression> includes = new ArrayList<PathExpression>( 100 );
        private List<PathExpression> excludes = new ArrayList<PathExpression>( 100 );
        private boolean addDefaultExcludes = true;

        private TemplateIncludeExcludeFilter()
        {
        }

        public TemplateIncludeExcludeFilter noDefaultExcludes()
        {
            addDefaultExcludes = false;

            return this;
        }

        public TemplateIncludeExcludeFilter addInclude( PathExpression include )
        {
            includes.add( include );

            return this;
        }

        public TemplateIncludeExcludeFilter addStringIncludes( Iterable<String> includes )
        {
            if ( includes == null )
            {
                return this;
            }

            for ( String include : includes )
            {
                this.includes.add( new PathExpression( include ) );
            }

            return this;
        }

        public TemplateIncludeExcludeFilter addExclude( PathExpression exclude )
        {
            excludes.add( exclude );

            return this;
        }

        public TemplateIncludeExcludeFilter addStringExcludes( Iterable<String> excludes )
        {
            if ( excludes == null )
            {
                return this;
            }

            for ( String exclude : excludes )
            {
                this.excludes.add( new PathExpression( exclude ) );
            }

            return this;
        }

        public IncludeExcludeFilter create()
        {
            if ( addDefaultExcludes )
            {
                excludes.addAll( Arrays.asList( DEFAULT_EXCLUDES ) );
            }

            return new IncludeExcludeFilter( includes, excludes );
        }
    }

    public static TemplateIncludeExcludeFilter includeExcludeFilter()
    {
        return new TemplateIncludeExcludeFilter();
    }
}
