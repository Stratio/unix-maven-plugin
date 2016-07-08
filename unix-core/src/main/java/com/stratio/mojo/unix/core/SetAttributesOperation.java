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
 package com.stratio.mojo.unix.core;

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
import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.core.AssemblyOperationUtil.*;
import com.stratio.mojo.unix.io.*;
import static com.stratio.mojo.unix.io.IncludeExcludeFilter.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.Validate.*;
import static com.stratio.mojo.unix.util.line.LineStreamUtil.*;
import com.stratio.mojo.unix.util.line.*;

import java.io.*;
import java.lang.Class;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class SetAttributesOperation
    implements AssemblyOperation
{
    private final IncludeExcludeFilter selector;

    private final RelativePath basedir;
    private final List<String> includes;
    private final List<String> excludes;
    private final Option<FileAttributes> fileAttributes;
    private final Option<FileAttributes> directoryAttributes;

    public SetAttributesOperation( RelativePath basedir, List<String> includes, List<String> excludes,
                                   Option<FileAttributes> fileAttributes, Option<FileAttributes> directoryAttributes )
    {
        validateNotNull( basedir, includes, excludes, fileAttributes, directoryAttributes );

        this.basedir = basedir;
        this.includes = includes;
        this.excludes = excludes;
        this.fileAttributes = fileAttributes;
        this.directoryAttributes = directoryAttributes;

        selector = includeExcludeFilter().
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();
    }

    public void perform( final FileCollector fileCollector )
        throws IOException
    {
        Effect<ApplyAttributes> effect = new Effect<ApplyAttributes>()
        {
            public void e( ApplyAttributes applyAttributes )
            {
                fileCollector.apply( applyAttributes );
            }
        };

        fileAttributes.map( new F<FileAttributes, ApplyAttributes>()
        {
            public ApplyAttributes f( FileAttributes fileAttributes )
            {
                return new ApplyAttributes( UnixFsObject.RegularFile.class, basedir, fileAttributes );
            }
        } ).foreach( effect );

        directoryAttributes.map( new F<FileAttributes, ApplyAttributes>()
        {
            public ApplyAttributes f( FileAttributes fileAttributes )
            {
                return new ApplyAttributes( UnixFsObject.Directory.class, basedir, directoryAttributes.some() );
            }
        } ).foreach( effect );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Set attributes:" ).
            add( " Basedir: " + basedir );
        streamIncludesAndExcludes( streamWriter, includes, excludes );
        streamWriter.add( " Attributes: " ).
            add( "  File     : " + fileAttributes.map( singleLineShow.showS_() ).orSome( "None" ) ).
            add( "  Directory: " + directoryAttributes.map( singleLineShow.showS_() ).orSome( "None" ) );
    }

    private final class ApplyAttributes
        extends F<UnixFsObject, Option<UnixFsObject>>
    {
        private final Class<? extends UnixFsObject> klass;
        private final RelativePath basedir;
        private final FileAttributes attributes;

        private ApplyAttributes( Class<? extends UnixFsObject> klass, RelativePath basedir, FileAttributes attributes )
        {
            this.klass = klass;
            this.basedir = basedir;
            this.attributes = attributes;
        }

        public Option<UnixFsObject> f( UnixFsObject fsObject )
        {
            if ( !klass.isAssignableFrom( fsObject.getClass() ) )
            {
                return none();
            }

            // Remove the basedir part of the path before matching
            RelativePath massagedPath;

            if ( basedir == RelativePath.BASE )
            {
                massagedPath = fsObject.path;
            }
            else
            {
                Option<RelativePath> option = fsObject.path.subtract( basedir );
                if ( option.isNone() )
                {
                    return none();
                }

                massagedPath = option.some();
            }

            if ( !selector.matches( massagedPath ) )
            {
                return none();
            }

            // TODO: check that the attributes was changed. return none() if not.
            return some( fsObject.setFileAttributes( fsObject.attributes.useAsDefaultsFor( attributes ) ) );
        }
    }
}
