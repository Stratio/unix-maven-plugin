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
 package com.stratio.mojo.unix.util;

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
import com.stratio.mojo.unix.java.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;
import org.joda.time.format.*;

import java.io.*;
import java.security.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class UnixUtil
{
    public static String md5String( File file )
        throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance( "MD5" );

        InputStream is = null;
        try
        {
            is = new FileInputStream( file );

            byte[] buffer = new byte[128 * 1024];
            while ( true )
            {
                int read = is.read( buffer, 0, buffer.length );

                if ( read == -1 )
                {
                    break;
                }

                digest.update( buffer, 0, read );
            }

            StringBuffer string = new StringBuffer( digest.getDigestLength() * 2 );
            for ( byte b : digest.digest() )
            {
                int x = b & 0xff;
                if ( x < 16 )
                {
                    string.append( "0" );
                }
                string.append( Integer.toHexString( x ) );
            }

            return string.toString();
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static void assertField( String field, Object value )
        throws MissingSettingException
    {
        if ( value == null )
        {
            throw new MissingSettingException( field );
        }
    }

    public static String getField( String field, String value )
        throws MissingSettingException
    {
        if ( StringUtils.isEmpty( value ) )
        {
            throw new MissingSettingException( field );
        }

        return value;
    }

    public static File getField( String field, File value )
        throws MissingSettingException
    {
        if ( value == null )
        {
            throw new MissingSettingException( field );
        }

        return value;
    }

    public static String getFieldOrDefault( String value, String defaultValue )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            return defaultValue;
        }

        return value;
    }

    // -----------------------------------------------------------------------
    // Unix commands
    // -----------------------------------------------------------------------

    public static void chmodIf( boolean b, File file, String mode )
        throws IOException
    {
        if ( b )
        {
            chmod( file, mode );
        }
    }

    public static void chmodIf( Option<File> file, String mode )
        throws IOException
    {
        if ( file.isSome() )
        {
            chmod( file.some(), mode );
        }
    }

    public static void chmod( File file, String mode )
        throws IOException
    {
        new SystemCommand().
            setCommand( "chmod" ).
            addArgument( mode ).
            addArgument( file.getAbsolutePath() ).
            execute().
            assertSuccess( "Error while running chmod on " + file );
    }

    public static void symlink( File basedir, String source, RelativePath target )
        throws IOException
    {
        new SystemCommand().
            setBasedir( basedir ).
            setCommand( "ln" ).
            addArgument( "-s" ).
            addArgument( source ).
            addArgument( target.string ).
            execute().
            assertSuccess( "Error while running ln -s in " + basedir.getAbsolutePath() );
    }

    // -----------------------------------------------------------------------
    // Functional Java
    // -----------------------------------------------------------------------

    public static <A> boolean optionEquals( Option<A> tis, java.lang.Object o )
    {
        return !(o == null || !(o instanceof Option)) && (o == tis || optionEquals( tis, (Option) o ));
    }

    public static <A, B> F2<Option<A>, F<A, B>, Option<B>> optionMap()
    {
        return new F2<Option<A>, F<A, B>, Option<B>>()
        {
            public Option<B> f( Option<A> option, F<A, B> f )
            {
                return option.map( f );
            }
        };
    }

    public static <A> boolean optionEquals( Option<A> tis, Option that )
    {
        // This logic would be in None
        if ( tis.isNone() )
        {
            return that.isNone();
        }

        if ( that.isNone() )
        {
            return false;
        }

        // This logic would be in Some
        return tis.some().equals( that.some() );
    }

    public static <A> A someE( Option<A> option, String msg )
    {
        if ( option.isSome() )
        {
            return option.some();
        }

        throw Bottom.error( msg );
    }

    public static final F2<DateTimeFormatter, LocalDateTime, String> formatLocalDateTime =
        new F2<DateTimeFormatter, LocalDateTime, String>()
        {
            public String f( DateTimeFormatter dateTimeFormatter, LocalDateTime partial )
            {
                return dateTimeFormatter.print( partial );
            }
        };

    public static final F2<DateTimeFormatter, String, Option<DateTime>> parseDateTime =
        new F2<DateTimeFormatter, String, Option<DateTime>>()
        {
            public Option<DateTime> f( DateTimeFormatter dateTimeFormatter, String text )
            {
                try
                {
                    return some( dateTimeFormatter.parseDateTime( text ) );
                }
                catch ( IllegalArgumentException e )
                {
                    return none();
                }
            }
        };

    public static final F<DateTime, LocalDateTime> toLocalDateTime = new F<DateTime, LocalDateTime>()
    {
        public LocalDateTime f( DateTime dateTime )
        {
            return dateTime.toLocalDateTime();
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static void close( Closeable closeable )
    {
        try
        {
            closeable.close();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    public static void flush( Flushable flusable )
    {
        try
        {
            flusable.flush();
        }
        catch ( IOException e )
        {
            // ignore
        }
    }

    // ----------------------------------------------------------------------
    // Helper methods for test methods
    // ----------------------------------------------------------------------

    public static File getTestFile( String path )
    {
        return new File( basedir, path );
    }

    public static String getTestPath( String path )
    {
        return getTestFile( path ).getAbsolutePath();
    }

    private final static File basedir = getBasedir();

    public static File getBasedir()
    {
        return new File( System.getProperty( "basedir", new File( "" ).getAbsolutePath() ) );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static final class Filter
    {
        public static <T> F<T, Boolean> instanceOfFilter( java.lang.Class cls )
        {
            return compose( curry( ClassF.isAssignableFrom, cls ), ObjectF.<T>getClass_() );
        }
    }
}
