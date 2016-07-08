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
import static fj.Function.*;
import static fj.P.*;
import fj.data.*;
import com.stratio.mojo.unix.io.*;
import static com.stratio.mojo.unix.java.StringF.*;
import com.stratio.mojo.unix.util.*;

import static fj.data.Option.some;
import static com.stratio.mojo.unix.util.Validate.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;
import org.joda.time.format.*;

import java.util.regex.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class UnixFsObject<A extends UnixFsObject>
    implements Comparable<UnixFsObject>, LineProducer
{
    public final RelativePath path;
    public final LocalDateTime lastModified;
    public final long size;
    public final FileAttributes attributes;
    public final List<Replacer> replacers;
    public final LineEnding lineEnding;

    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().
        appendMonthOfYearShortText().
        appendLiteral( ' ' ).
        appendDayOfMonth( 2 ).
        appendLiteral( ' ' ).
        appendYear( 4, 4 ).
        appendLiteral( ' ' ).
        appendHourOfDay( 2 ).
        appendLiteral( ':' ).
        appendMinuteOfHour( 2 ).
        appendLiteral( ':' ).
        appendSecondOfMinute( 2 ).
        toFormatter();

    /**
     * Consider creating a more unix-like variant that 1) doesn't include seconds and
     * 2) shortens the date if the file is over 1 year old.
     *
     * Very old file: drwxr-xr-x   3 root     sys            3 Dec 17  2008 export
     * Recent file:   -rwxr-----   1 efa      efa         9480 Sep 20 11:45 operate.sh
     */
    public static final F<LocalDateTime, String> formatter = curry( UnixUtil.formatLocalDateTime, FORMAT );

    private final char prefixChar;

    protected UnixFsObject( char prefixChar, RelativePath path, LocalDateTime lastModified, long size,
                            FileAttributes attributes, List<Replacer> replacers, LineEnding lineEnding )
    {
        validateNotNull( path, lastModified, attributes, replacers );

        this.prefixChar = prefixChar;
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
        this.attributes = attributes;
        this.replacers = replacers;
        this.lineEnding = lineEnding;
    }

    public final A setPath( RelativePath path )
    {
        return copy( path, lastModified, size, attributes, replacers, lineEnding );
    }

    public final A setLastModified( LocalDateTime lastModified )
    {
        return copy( path, lastModified, size, attributes, replacers, lineEnding );
    }

    public FileAttributes getFileAttributes()
    {
        return attributes;
    }

    public final A setFileAttributes( FileAttributes attributes )
    {
        Validate.validateNotNull( attributes );
        return copy( path, lastModified, size, attributes, replacers, lineEnding );
    }

    public final A addReplacers( List<Replacer> replacers, LineEnding lineEnding )
    {
        return copy( path, lastModified, size, attributes, this.replacers.append( replacers ), lineEnding );
    }

    protected abstract A copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes,
                               List<Replacer> filters, LineEnding lineEnding );

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static RegularFile regularFile( RelativePath path, LocalDateTime lastModified, long size,
                                           FileAttributes attributes )
    {
        return new RegularFile( path, lastModified, size, attributes, List.<Replacer>nil(), LineEnding.keep );
    }

    public static RegularFile regularFile( RelativePath path, LocalDateTime lastModified, long size,
                                           FileAttributes attributes, List<Replacer> filters, LineEnding lineEnding )
    {
        return new RegularFile( path, lastModified, size, attributes, filters, lineEnding );
    }

    public static Directory directory( RelativePath path, LocalDateTime lastModified, FileAttributes attributes )
    {
        return new Directory( path, lastModified, attributes );
    }

    public static Symlink symlink( RelativePath path, LocalDateTime lastModified, Option<String> user,
                                   Option<String> group, String target )
    {
        return new Symlink( path, lastModified, user, group, target );
    }

    // -----------------------------------------------------------------------
    // Object Overrides
    // -----------------------------------------------------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !(o instanceof UnixFsObject) )
        {
            return false;
        }

        UnixFsObject that = (UnixFsObject) o;

        return path.equals( that.path ) &&
            lastModified.equals( that.lastModified ) &&
            size == that.size &&
            attributes.equals( that.attributes );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    public void streamTo( LineStreamWriter stream )
    {
        stream.add( toString() );
    }

    public int compareTo( UnixFsObject other )
    {
        return path.compareTo( other.path );
    }

    public String toString()
    {
        F<String, String> leftPad10 = curry( leftPad, 10 );
        F<String, String> rightPad10 = curry( rightPad, 10 );

        return prefixChar + attributes.mode.map( UnixFileMode.showLong ).orSome( "<unknown>" ) +
            " " + attributes.user.map( leftPad10 ).orSome( " <unknown>" ) +
            " " + attributes.group.map( leftPad10 ).orSome( " <unknown>" ) +
            " " + p( String.valueOf( size ) ).map( rightPad10 )._1() +
            " " + p( lastModified ).map( formatter )._1() +
            " " + path.string + "\"";
    }

    // -----------------------------------------------------------------------
    // Sub classes
    // -----------------------------------------------------------------------

    public static class RegularFile
        extends UnixFsObject<RegularFile>
    {
        private RegularFile( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes,
                             List<Replacer> filters, LineEnding lineEnding )
        {
            super( '-', path, lastModified, size, attributes, filters, lineEnding );
        }

        protected RegularFile copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes,
                                    List<Replacer> filters, LineEnding lineEnding )
        {
            return new RegularFile( path, lastModified, size, attributes, filters, lineEnding );
        }
    }

    public static class Directory
        extends UnixFsObject<Directory>
    {
        private Directory( RelativePath path, LocalDateTime lastModified, FileAttributes attributes )
        {
            super( 'd', path, lastModified, 0, attributes, List.<Replacer>nil(), LineEnding.keep );
        }

        protected Directory copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes,
                                  List<Replacer> filters, LineEnding lineEnding )
        {
            return new Directory( path, lastModified, attributes );
        }
    }

    public static class Symlink
        extends UnixFsObject<Symlink>
    {
        public final String value;

        private Symlink( RelativePath path, LocalDateTime lastModified, Option<String> user, Option<String> group, String value )
        {
            super( 'l', path, lastModified, value.length(), new FileAttributes( user, group, some( UnixFileMode._SYMLINK ) ), List.<Replacer>nil(), LineEnding.keep );
            validateNotNull( value );

            this.value = value;
        }

        protected Symlink copy( RelativePath path, LocalDateTime lastModified, long size, FileAttributes attributes,
                                List<Replacer> filters, LineEnding lineEnding )
        {
            return new Symlink( path, lastModified, attributes.user, attributes.group, value );
        }

        public String toString() {
            return super.toString() + " -> " + value;
        }
    }

    public static class Replacer
    {
        public final Pattern pattern;

        public final String replacement;

        public Replacer( String pattern, String replacement )
        {
            this.pattern = Pattern.compile( pattern );
            this.replacement = replacement;
        }

        public String toString()
        {
            return "pattern=" + pattern.pattern() + ", replacement=" + replacement;
        }

        public String replace( String line )
        {
            return pattern.matcher( line ).replaceAll( replacement );
        }
    }
}
