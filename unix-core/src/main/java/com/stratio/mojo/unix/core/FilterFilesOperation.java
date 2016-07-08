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

import fj.*;
import fj.data.*;
import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.core.AssemblyOperationUtil.*;
import com.stratio.mojo.unix.io.*;
import static com.stratio.mojo.unix.io.IncludeExcludeFilter.*;
import com.stratio.mojo.unix.util.line.*;

import java.io.*;

/**
 * TODO: support basedir parameter like SetAttributesOperation.
 * TODO: Is it really correct that filtered files should retain the old timestamp?
 */
public class FilterFilesOperation
    implements AssemblyOperation
{
    public final List<String> includes;
    public final List<String> excludes;
    public final List<Replacer> replacers;
    public final LineEnding lineEnding;

    public FilterFilesOperation( List<String> includes, List<String> excludes, List<Replacer> replacers, LineEnding lineEnding )
    {
        this.includes = includes;
        this.excludes = excludes;
        this.replacers = replacers;
        this.lineEnding = lineEnding;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        final IncludeExcludeFilter selector = includeExcludeFilter().
            addStringIncludes( includes ).
            addStringExcludes( excludes ).
            create();

        fileCollector.apply( new F<UnixFsObject, Option<UnixFsObject>>()
        {
            public Option<UnixFsObject> f( UnixFsObject object )
            {
                if ( !( object instanceof RegularFile ) )
                {
                    return none();
                }

                RegularFile f = (RegularFile) object;

                if ( !selector.matches( f.path ) )
                {
                    return none();
                }

                return some( object.addReplacers( replacers, lineEnding ) );
            }
        } );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Filter Files:" );
        streamIncludesAndExcludes( streamWriter, includes, excludes );
        streamWriter.add( " Replacers:" );
        for ( Replacer replacer : replacers )
        {
            streamWriter.add( "  " + replacer.toString() );
        }
    }
}
