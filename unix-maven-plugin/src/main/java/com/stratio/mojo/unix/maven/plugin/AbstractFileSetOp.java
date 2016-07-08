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

import static fj.P.*;
import fj.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import org.apache.maven.plugin.*;
import com.stratio.mojo.unix.core.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractFileSetOp
    extends AssemblyOp
{
    private RelativePath to = RelativePath.BASE;

    private List<String> includes = nil();

    private List<String> excludes = nil();

    private String pattern;

    private String replacement;

    private MojoFileAttributes fileAttributes = new MojoFileAttributes();

    private MojoFileAttributes directoryAttributes = new MojoFileAttributes();

    public AbstractFileSetOp( String operationType )
    {
        super( operationType );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setTo( String to )
    {
        this.to = relativePath( to );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setIncludes( String[] includes )
    {
        this.includes = List.list( includes );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setExcludes( String[] excludes )
    {
        this.excludes = List.list( excludes );
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setPattern( String pattern )
    {
        this.pattern = nullIfEmpty(pattern);
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setReplacement( String replacement )
    {
        this.replacement = nullIfEmpty(replacement);
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setFileAttributes( MojoFileAttributes fileAttributes )
    {
        this.fileAttributes = fileAttributes;
    }

    @SuppressWarnings( "UnusedDeclaration" )
    public void setDirectoryAttributes( MojoFileAttributes directoryAttributes )
    {
        this.directoryAttributes = directoryAttributes;
    }

    protected AssemblyOperation createCopyArchiveOperation( Fs<?> archive,
                                                            CreateOperationContext context  )
        throws MojoFailureException
    {
        Option<P2<String, String>> pattern = none();
        if ( this.pattern != null )
        {
            if ( replacement == null )
            {
                throw new MojoFailureException( "A replacement expression has to be set if a pattern is given." );
            }

            pattern = some( p( this.pattern, replacement ) );
        }

        return new CopyDirectoryOperation( archive, to, includes, excludes, pattern,
                                           context.defaultFileAttributes.useAsDefaultsFor( fileAttributes.create() ),
                                           context.defaultDirectoryAttributes.useAsDefaultsFor( directoryAttributes.create() ) );
    }
}
