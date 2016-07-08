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
 package com.stratio.mojo.unix.sysvpkg;

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
import fj.data.*;
import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.Validate.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;
import org.joda.time.format.*;

import java.io.*;
import java.text.*;
import java.util.Locale;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkgchkUtil
{
    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder().
        appendMonthOfYearShortText().
        appendLiteral( ' ' ).
        appendDayOfMonth( 2 ).
        appendLiteral( ' ' ).
        appendHourOfDay( 2 ).
        appendLiteral( ':' ).
        appendMinuteOfHour( 2 ).
        appendLiteral( ':' ).
        appendSecondOfMinute( 2 ).
        appendLiteral( ' ' ).
        appendYear( 4, 4 ).
        toFormatter().
        withLocale(Locale.ENGLISH);

    public static final F<LocalDateTime, String> formatter = curry( UnixUtil.formatLocalDateTime, FORMAT );

    public static final F<String, Option<DateTime>> parser = curry( UnixUtil.parseDateTime, FORMAT );

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "MMM dd HH:mm:ss yyyy" );

    public static abstract class FileInfo
        implements EqualsIgnoreNull<FileInfo>, LineProducer
    {
        public final String pathname;

        public final String type;

        public final int fileSize;

        public final int sum;

        public final Option<LocalDateTime> lastModification;

        private FileInfo( String pathname, String type, int fileSize, int sum, Option<LocalDateTime> lastModification )
        {
            validateNotNull( pathname, type );
            this.pathname = pathname;
            this.type = type;
            this.fileSize = fileSize;
            this.sum = sum;
            this.lastModification = lastModification;
        }

        public boolean equalsIgnoreNull( FileInfo that )
        {
            return this.getClass().isAssignableFrom( that.getClass() ) &&
                pathname.equals( that.pathname ) &&
                type.equals( that.type ) &&
                (fileSize == 0 || fileSize == that.fileSize) &&
                (sum == 0 || sum == that.sum) &&
                (lastModification.isNone() || lastModification.some().equals( that.lastModification.some() ));
        }

        public final boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !(o instanceof FileInfo) )
            {
                return false;
            }

            FileInfo that = (FileInfo) o;

            return pathname.equals( that.pathname );
        }

        public final int hashCode()
        {
            return pathname.hashCode();
        }
    }

    private static class AbstractFile
        extends FileInfo
    {
        public final String mode;

        public final String owner;

        public final String group;

        public AbstractFile( String pathname, String type, String mode, String owner, String group, int fileSize,
                             int sum, Option<LocalDateTime> lastModification )
        {
            super( pathname, type, fileSize, sum, lastModification );
            validateNotNull( mode, owner, group );
            this.mode = mode;
            this.owner = owner;
            this.group = group;
        }

        public boolean equalsIgnoreNull( FileInfo t )
        {
            AbstractFile that = (AbstractFile) t;

            return super.equalsIgnoreNull( that ) &&
                mode.equals( that.mode ) &&
                owner.equals( that.owner ) &&
                group.equals( that.group );
        }

        public void streamTo( LineStreamWriter stream )
        {
            stream.
                add( "Pathname: " + pathname ).
                add( "Type: " + type ).
                add( "Expected mode: " + mode ).
                add( "Expected owner: " + owner ).
                add( "Expected group: " + group ).
                add( "Expected file size (bytes): " + fileSize ).
                add( "Expected sum(1) of contents: " + sum ).
                add( "Expected last modification: " + lastModification.map( formatter ).orSome( "not set" ) );
        }
    }

    public static FileInfo directory( String pathname, String mode, String owner, String group,
                                      Option<LocalDateTime> lastModification )
    {
        return new AbstractFile( pathname, "directory", mode, owner, group, 0, 0, lastModification );
    }

    public static FileInfo regularFile( String pathname, String mode, String owner, String group, int fileSize, int sum,
                                        Option<LocalDateTime> lastModification )
    {
        return new AbstractFile( pathname, "regular file", mode, owner, group, fileSize, sum, lastModification );
    }

    public static FileInfo installationFile( String pathname, int fileSize, int sum,
                                             Option<LocalDateTime> lastModification )
    {
        return new FileInfo( pathname, "installation file", fileSize, sum, lastModification )
        {
            public void streamTo( LineStreamWriter stream )
            {
                stream.
                    add( "Pathname: " + pathname ).
                    add( "Type: " + type ).
                    add( "Expected file size (bytes): " + fileSize ).
                    add( "Expected sum(1) of contents: " + sum ).
                    add( "Expected last modification: " + lastModification.map( formatter ).orSome( "not set" ) );
            }
        };
    }

    public static FileInfo symlink( String pathname, final String source )
    {
        return new FileInfo( pathname, "symbolic link", 0, 0, Option.<LocalDateTime>none() )
        {
            public void streamTo( LineStreamWriter stream )
            {
                stream.
                    add( "Pathname: " + pathname ).
                    add( "Type: " + type ).
                    add( "Source of link:" + source );
            }
        };
    }

    public static List<FileInfo> getPackageInforForDevice( File device )
        throws IOException
    {
        return getPackageInforForDevice( device, "all" );
    }

    public static List<FileInfo> getPackageInforForDevice( File device, String instance )
        throws IOException
    {
        PkgchkParser parser = new PkgchkParser();
        new SystemCommand().
            withStdoutConsumer( parser ).
            setCommand( "/usr/sbin/pkgchk" ).
            addArgument( "-l" ).
            addArgument( "-d" ).
            addArgument( device.getAbsolutePath() ).
            addArgument( instance ).
            execute().
            assertSuccess();

        return parser.getList();
    }

    public static boolean available()
    {
        return SystemCommand.available( "pkgchk" );
    }

    public static class PkgchkParser
        implements SystemCommand.LineConsumer
    {
        public String pathname;

        public String type;

        public String mode;

        public String owner;

        public String group;

        public int fileSize;

        public int sum;

        public Option<LocalDateTime> lastModification = none();

        public String source;

        private List<FileInfo> list = List.nil();

        public void onLine( String line )
        {
            if ( line.startsWith( "Pathname: " ) )
            {
                pathname = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Type: " ) )
            {
                type = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected mode: " ) )
            {
                mode = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected owner: " ) )
            {
                owner = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected group: " ) )
            {
                group = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.startsWith( "Expected file size (bytes): " ) )
            {
                fileSize = Integer.parseInt( line.substring( line.indexOf( ':' ) + 1 ).trim() );
            }
            else if ( line.startsWith( "Expected sum(1) of contents: " ) )
            {
                sum = Integer.parseInt( line.substring( line.indexOf( ':' ) + 1 ).trim() );
            }
            else if ( line.startsWith( "Expected last modification: " ) )
            {
                line = line.substring( line.indexOf( ':' ) + 1 ).trim();
                lastModification = parser.f( line ).map( UnixUtil.toLocalDateTime );
            }
            else if ( line.startsWith( "Expected sum(1) of contents: " ) )
            {
                source = line.substring( line.indexOf( ':' ) + 1 ).trim();
            }
            else if ( line.trim().length() == 0 )
            {
                if ( type.equals( "regular file" ) )
                {
                    list = list.cons( regularFile( pathname, mode, owner, group, fileSize, sum, lastModification ) );
                }
                else if ( type.equals( "installation file" ) )
                {
                    list = list.cons( installationFile( pathname, fileSize, sum, lastModification ) );
                }
                else if ( type.equals( "directory" ) )
                {
                    list = list.cons( directory( pathname, mode, owner, group, lastModification ) );
                }
                else if ( type.equals( "symbolic link" ) )
                {
                    list = list.cons( symlink( pathname, source ) );
                }
                else
                {
                    throw new RuntimeException( "Unknown type: " + type );
                }
            }
        }

        public List<FileInfo> getList()
        {
            return list.reverse();
        }
    }
}
