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

import org.joda.time.*;

import java.io.*;
import java.lang.reflect.*;

import static com.stratio.mojo.unix.maven.plugin.ShittyUtil.*;

public class Timestamps
{
    private final File basedir;

    class Common
    {
        public final FileTimestamp hudsonWarTimestamp =
            new FileTimestamp( findArtifact( "org.jvnet.hudson.main", "hudson-war", "1.255", "war" ),
                               new LocalDateTime( 2010, 9, 30, 10, 12, 56 ) );

        public void setTimestamps()
            throws Exception
        {
            setTimestamps( getClass() );
        }

        public void setTimestamps(Class klass)
            throws Exception
        {
            for ( Field field : klass.getDeclaredFields() )
            {
                if ( !field.getType().isAssignableFrom( FileTimestamp.class ) )
                {
                    continue;
                }

                FileTimestamp ft = (FileTimestamp) field.get( this );

                if ( !ft.file.canRead() )
                {
                    throw new IOException( "No such file: " + ft.file.getAbsolutePath() );
                }

                if ( !ft.file.setLastModified( ft.timestamp.toDateTime().getMillis() ) )
                {
                    throw new IOException( "Unable to set last modified on file: " + ft.file.getAbsolutePath() );
                }
            }

            if(klass != Object.class) {
                setTimestamps( klass.getSuperclass() );
            }
        }
    }

    class Deb1
        extends Common
    {
        public final FileTimestamp configProperties =
            ft( "src/main/unix/files/opt/hudson/etc/config.properties", 2012, 2, 3, 4, 5, 6 );
    }

    public final Deb1 deb1;

    class Zip1
        extends Common
    {
        public final FileTimestamp filter1 =
            ft( "src/main/unix/files/opt/hudson/etc/filter-1.conf", 2013, 2, 19, 10, 25, 2 );
        public final FileTimestamp filter2 =
            ft( "src/main/unix/files/opt/hudson/etc/filter-2.conf", 2013, 1, 2, 3, 4, 6 );
        public final FileTimestamp filterAll =
            ft( "src/main/unix/files/opt/hudson/etc/filter-all.conf", 2013, 2, 20, 10, 37, 36 );
        public final FileTimestamp unfiltered =
            ft( "src/main/unix/files/opt/hudson/etc/unfiltered.properties", 2013, 2, 20, 10, 45, 20 );
    }

    public final Zip1 zip1;

    class Zip3
        extends Common
    {
        public final FileTimestamp readme_default =
            ft( "src/main/unix/files-default/usr/share/hudson/server/README.txt", 2011, 9, 23, 17, 30, 8 );

        public final FileTimestamp readme_slave =
            ft( "src/main/unix/files-slave/usr/share/hudson/slave/README.txt", 2011, 9, 23, 17, 30, 8 );

        public final FileTimestamp licenseDownstream =
            ft( "src/main/unix/files/usr/share/hudson/LICENSE-downstream.txt", 2011, 9, 23, 17, 30, 8 );
    }

    public final Zip3 zip3;

    public Timestamps( File basedir )
    {
        this.basedir = basedir;
        // These constructors depend on basedir being set.
        deb1 = new Deb1();
        zip1 = new Zip1();
        zip3 = new Zip3();
    }

    public class FileTimestamp
    {
        public final File file;

        public final LocalDateTime timestamp;

        public FileTimestamp( File file, LocalDateTime timestamp )
        {
            this.file = file;
            this.timestamp = timestamp;
        }
    }

    public FileTimestamp ft( String path, int y, int m, int d, int h, int min, int s )
    {
        return new FileTimestamp( new File( basedir, path ), new LocalDateTime( y, m, d, h, min, s ) );
    }
}
