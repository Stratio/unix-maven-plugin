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
 package com.stratio.mojo.unix.sysvpkg.prototype;

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
import static java.lang.Boolean.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.UnixFileMode.*;
import com.stratio.mojo.unix.util.*;
import com.stratio.mojo.unix.util.line.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class PrototypeEntry<U extends UnixFsObject>
    implements LineProducer, PackageFileSystemObject<PrototypeEntry>
{
    protected final String pkgClass;

    protected final Option<Boolean> relative;

    protected final U object;

    protected PrototypeEntry( Option<String> pkgClass, Option<Boolean> relative, U object )
    {
        Validate.validateNotNull( pkgClass, relative, object );
        this.pkgClass = pkgClass.orSome( "none" );
        this.relative = relative;
        this.object = object;
    }

    public UnixFsObject getUnixFsObject()
    {
        return object;
    }

    public PrototypeEntry getExtension()
    {
        return this;
    }

    protected abstract String generatePrototypeLine();

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public void streamTo( LineStreamWriter stream )
    {
        stream.
            add( generatePrototypeLine() );
    }

    public String getPath()
    {
        if ( TRUE.equals( relative.orSome( false ) ) )
        {
            return object.path.string;
        }

        return object.path.asAbsolutePath( "/" );
    }

    // -----------------------------------------------------------------------
    // ObjectF Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || PrototypeEntry.class != o.getClass() )
        {
            return false;
        }

        PrototypeEntry that = (PrototypeEntry) o;

        return object.path.equals( that.object.path );
    }

    public int hashCode()
    {
        return object.path.hashCode();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    protected String getProcessedPath( Option<File> realPath )
    {
        return realPath.map( new F<File, String>()
        {
            public String f( File file )
            {
                return getPath() + "=" + file.getAbsolutePath();
            }
        } ).orSome( getPath() );
    }

    protected String toString( FileAttributes attributes )
    {
        return attributes.mode.map( showOcalString ).orSome( "?" ) + " " +
            attributes.user.orSome( "?" ) + " " +
            attributes.group.orSome( "?" );
    }
}
