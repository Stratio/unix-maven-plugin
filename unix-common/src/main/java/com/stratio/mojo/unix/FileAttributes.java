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
import static fj.data.List.*;
import static fj.data.Option.*;
import static com.stratio.mojo.unix.UnixFileMode.*;
import static com.stratio.mojo.unix.util.UnixUtil.*;
import static com.stratio.mojo.unix.util.Validate.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class FileAttributes
{
    public final Option<String> user;

    public final Option<String> group;

    public final Option<UnixFileMode> mode;

    public final List<String> tags;

    public final static Show<FileAttributes> singleLineShow = Show.anyShow();

    /**
     * A file object with all none fields. Use this when creating template objects.
     */
    public final static FileAttributes EMPTY = new FileAttributes( Option.<String>none(), Option.<String>none(),
        Option.<UnixFileMode>none(), List.<String>nil() );

    public FileAttributes( String user, String group, UnixFileMode mode )
    {
        this( some( user ), some( group ), some( mode ), List.<String>nil() );
    }

    public FileAttributes( Option<String> user, Option<String> group, Option<UnixFileMode> mode )
    {
        this( user, group, mode, List.<String>nil() );
    }

    public FileAttributes( Option<String> user, Option<String> group, Option<UnixFileMode> mode, List<String> tags )
    {
        validateNotNull( user, group, mode, tags );
        this.user = user;
        this.group = group;
        this.mode = mode;
        this.tags = tags;
    }

    public FileAttributes user( String user )
    {
        return new FileAttributes( fromNull( user ), group, mode, tags );
    }

    public FileAttributes user( Option<String> user )
    {
        return new FileAttributes( user, group, mode, tags );
    }

    public FileAttributes group( String group )
    {
        return new FileAttributes( user, fromNull( group ), mode, tags );
    }

    public FileAttributes group( Option<String> group )
    {
        return new FileAttributes( user, group, mode, tags );
    }

    public FileAttributes mode( UnixFileMode mode )
    {
        return new FileAttributes( user, group, fromNull( mode ), tags );
    }

    public FileAttributes mode( Option<UnixFileMode> mode )
    {
        return new FileAttributes( user, group, mode, tags );
    }

    public FileAttributes addTag( String tag )
    {
        return new FileAttributes( user, group, mode, tags.append( single( tag ) ) );
    }

    public FileAttributes tags( List<String> tags )
    {
        return new FileAttributes( user, group, mode, this.tags.append( tags ) );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public FileAttributes useAsDefaultsFor( FileAttributes other )
    {
        return new FileAttributes(
            other.user.orElse( user ),
            other.group.orElse( group ),
            other.mode.orElse( mode ),
            other.tags );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public final static F2<FileAttributes, FileAttributes, FileAttributes> useAsDefaultsFor = new F2<FileAttributes, FileAttributes, FileAttributes>()
    {
        public FileAttributes f( FileAttributes defaults, FileAttributes other )
        {
            return defaults.useAsDefaultsFor( other );
        }
    };

    public final static F<FileAttributes, Option<String>> userF = new F<FileAttributes, Option<String>>()
    {
        public Option<String> f( FileAttributes attributes )
        {
            return attributes.user;
        }
    };

    public final static F<FileAttributes, Option<String>> groupF = new F<FileAttributes, Option<String>>()
    {
        public Option<String> f( FileAttributes attributes )
        {
            return attributes.group;
        }
    };

    public final static F<FileAttributes, Option<UnixFileMode>> modeF = new F<FileAttributes, Option<UnixFileMode>>()
    {
        public Option<UnixFileMode> f( FileAttributes attributes )
        {
            return attributes.mode;
        }
    };

    public final static F<FileAttributes, List<String>> tagsF = new F<FileAttributes, List<String>>()
    {
        public List<String> f( FileAttributes attributes )
        {
            return attributes.tags;
        }
    };

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

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

        FileAttributes that = (FileAttributes) o;

        return optionEquals( user, that.user ) &&
            optionEquals( group, that.group ) &&
            optionEquals( mode, that.mode );
    }

    public String toString()
    {
        return "user=" + user.orSome( "<not set>" ) + ", " +
            "group=" + group.orSome( "<not set>" ) + ", " +
            "mode=" + mode.map( showLong ).orSome( "<not set>" );
    }
}
