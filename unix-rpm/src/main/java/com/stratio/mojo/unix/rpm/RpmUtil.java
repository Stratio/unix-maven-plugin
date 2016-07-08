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
 package com.stratio.mojo.unix.rpm;

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

import static java.lang.Long.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class RpmUtil
{
    public static final SimpleDateFormat DATE_FORMAT_SHORTER = new SimpleDateFormat( "MMM dd HH:mm" );

    public static final SimpleDateFormat DATE_FORMAT_LONGER = new SimpleDateFormat( "MMM dd yyyy" );

    public static final class FileInfo
        implements EqualsIgnoreNull<FileInfo>, LineProducer
    {
        public final String path;

        public final String user;

        public final String group;

        public final String mode;

        public final int size;

        public final Date date;

        public FileInfo( String path, String user, String group, String mode, int size, Date date )
        {
            this.path = path;
            this.user = user;
            this.group = group;
            this.mode = mode;
            this.size = size;
            this.date = date;
        }

        public boolean equalsIgnoreNull( FileInfo that )
        {
            return size == that.size &&
                (date == null || date.equals( that.date )) &&
                group.equals( that.group ) &&
                mode.equals( that.mode ) &&
                path.equals( that.path ) &&
                user.equals( that.user );
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

            return path.equals( that.path );
        }

        public final int hashCode()
        {
            return path.hashCode();
        }

        public void streamTo( LineStreamWriter stream )
        {
            stream.add( new StringBuffer().
                append( mode ).append( " " ).
                append( user ).append( " " ).
                append( group ).append( " " ).
                append( size ).append( " " ).
                append( date != null ? DATE_FORMAT_SHORTER.format( date ) : "<not set>" ).append( " " ).
                append( path ).toString() );
        }
    }

    public static List<FileInfo> queryPackageForFileInfo( File rpm )
        throws IOException
    {
        RpmQueryParser parser = new RpmQueryParser();
        new SystemCommand().
            withStdoutConsumer( parser ).
            setCommand( "rpm" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            addArgument( "-q" ).
            addArgument( "-l" ).
            addArgument( "-v" ).
            execute().
            assertSuccess();

        return parser.getList();
    }

    public static SpecFile getSpecFileFromRpm( File rpm )
        throws IOException
    {
        // rpm -q --queryformat "%{NAME}\n%{SIZE}" -p ./unix-maven-plugin/src/it/jetty/target/jetty-1.1-2.rpm

        SpecFile specFile = new SpecFile();

        RpmSpecParser parser = new RpmSpecParser( specFile );
        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( parser ).
            withStderrConsumer( System.err ).
            setCommand( "rpm" ).
            addArgument( "--query" ).
            addArgument( "--queryformat" ).
            addArgument( "%{NAME}\\n%{VERSION}\\n%{RELEASE}\\n%{SUMMARY}\\n%{LICENSE}\\n%{GROUP}\\n" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            execute().
            assertSuccess();

        ByteArrayOutputStream description = new ByteArrayOutputStream();

        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( description ).
            withStderrConsumer( System.err ).
            setCommand( "rpm" ).
            addArgument( "--query" ).
            addArgument( "--queryformat" ).
            addArgument( "%{DESCRIPTION}" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            execute().
            assertSuccess();

        RpmDumpParser rpmDumpParser = new RpmDumpParser( specFile );
        new SystemCommand().
            dumpCommandIf( true ).
            withStdoutConsumer( rpmDumpParser ).
            setCommand( "rpm" ).
            addArgument( "--query" ).
            addArgument( "--dump" ).
            addArgument( "--package" ).
            addArgument( rpm.getAbsolutePath() ).
            execute().
            assertSuccess();

        specFile.description = description.toString();
        return specFile;
    }

    private static class RpmQueryParser
        implements SystemCommand.LineConsumer
    {
        private List<FileInfo> list = new ArrayList<FileInfo>();

        public void onLine( String line )
            throws IOException
        {
            String[] parts = line.replaceAll( " +", " " ).split( " " );

            // Dunno what the second element is

            Date date;
            try
            {
                date = DATE_FORMAT_SHORTER.parse( parts[5] + " " + parts[6] + " " + parts[7] );
            }
            catch ( ParseException e )
            {
                try
                {
                    date = DATE_FORMAT_LONGER.parse( parts[5] + " " + parts[6] + " " + parts[7] );
                }
                catch ( Exception e2 )
                {
                    e2.printStackTrace();
                    return;
                }
            }

            list.add( new FileInfo( parts[8].trim(), parts[2].trim(), parts[3].trim(), parts[0].trim(),
                                    Integer.parseInt( parts[4].trim() ), date ) );
        }

        public List<FileInfo> getList()
        {
            return list;
        }
    }

    private static class RpmSpecParser
        implements SystemCommand.LineConsumer
    {
        private final SpecFile specFile;

        private int count;

        public RpmSpecParser( SpecFile specFile )
        {
            this.specFile = specFile;
        }

        public void onLine( String line )
            throws IOException
        {
            switch ( count++ )
            {
                case 0:
                    specFile.name = line;
                    break;
                case 1:
                    specFile.version = line;
                    break;
                case 2:
                    specFile.release = line;
                    break;
                case 3:
                    specFile.summary = line;
                    break;
                case 4:
                    specFile.license = line;
                    break;
                case 5:
                    specFile.group = line;
                    break;
            }
        }
    }

    public static class RpmDumpParser
        implements SystemCommand.LineConsumer
    {
        private final SpecFile specFile;

        public RpmDumpParser( SpecFile specFile )
        {
            this.specFile = specFile;
        }

        public void onLine( String line )
        {
            // Each of these lines look like this:
            // /etc/openldap/schema/dnszone.schema 5114 1232540847 2294a352407600431f736427587345e2 0100644 root root 1 0 0 X
            // path size time md5 mode user group config? doc? wtf? symlink (not "X" if symlink)
            //  0    1    2    3    4   5    6     7       8    9     10

            String[] parts = line.split( " " );

            RelativePath path = relativePath( parts[0] );

            long size = parseLong( parts[1] );

            LocalDateTime lastModified = new LocalDateTime( parseLong( parts[2] ) );

            // #4 is the md5

            int mode = Integer.parseInt( parts[4], 8 );
            FileAttributes attributes = EMPTY.
                user( parts[5] ).
                group( parts[6] ).
                mode( UnixFileMode.fromInt( mode ) );

            if ( "1".equals( parts[7] ) )
            {
                attributes = attributes.addTag( "config" );
            }

            if ( "1".equals( parts[8] ) )
            {
                attributes = attributes.addTag( "doc" );
            }

            if ( (mode & 0x4000) != 0 )
            {
                specFile.addDirectory( directory( path, lastModified, attributes ) );
            }
            else
            {
                if ( "X".equals( parts[10] ) )
                {
                    specFile.addFile( regularFile( path, lastModified, size, attributes ) );
                }
                else
                {
                    specFile.addSymlink( symlink( path, lastModified, attributes.user, attributes.group, parts[10] ) );
                }
            }
        }
    }
}
