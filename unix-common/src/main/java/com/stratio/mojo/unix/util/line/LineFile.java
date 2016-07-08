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
 package com.stratio.mojo.unix.util.line;

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

import static java.util.Collections.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class LineFile
    extends AbstractLineStreamWriter
    implements Iterable<String>
{
    private final List<String> lines = new ArrayList<String>( 1000 );

    private final String eol;

    public LineFile()
    {
        this.eol = EOL;
    }

    public LineFile( String eol )
    {
        this.eol = eol;
    }

    protected void onLine( String line )
    {
        lines.add( line != null ? line : "" );
    }

    public List<String> getLines()
    {
        return unmodifiableList( lines );
    }

    public int size()
    {
        return lines.size();
    }

    public Iterator<String> iterator()
    {
        return unmodifiableList( new ArrayList<String>( lines ) ).iterator();
    }

    public String toString()
    {
        return toString( eol );
    }

    public String toString( String eol )
    {
        StringBuilder buffer = new StringBuilder();
        for ( String line : lines )
        {
            buffer.append( line ).append( eol );
        }
        return buffer.toString();
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof LineFile ) )
        {
            return false;
        }

        LineFile strings = (LineFile) o;

        if ( !eol.equals( strings.eol ) )
        {
            return false;
        }

        if ( lines.size() != strings.lines.size() )
        {
            return false;
        }

        if ( !lines.equals( strings.lines ) )
        {
            return false;
        }

        for ( int i = 0, linesSize = lines.size(); i < linesSize; i++ )
        {
            if ( !lines.get( i ).equals( strings.lines.get( i ) ) )
            {
                return false;
            }
        }

        return true;
    }

    public int hashCode()
    {
        int result = eol.hashCode();
        for ( String line : lines )
        {
            result = 31 * result + line.hashCode();
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static LineFile fromFile( File file )
        throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new FileReader( file ) );

            LineFile lineFile = new LineFile();
            String line = reader.readLine();

            while ( line != null )
            {
                lineFile.lines.add( line );
                line = reader.readLine();
            }

            return lineFile;
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    public static LineFile fromList( fj.data.List<String> lines )
    {
        LineFile lineFile = new LineFile();
        lineFile.lines.addAll( lines.toCollection() );
        return lineFile;
    }

    public void writeTo( File file )
        throws IOException
    {
        LineWriterWriter writer = null;
        try
        {
            writer = new LineWriterWriter( new FileWriter( file ) );

            for ( String line : lines )
            {
                writer.add( line );
            }
        }
        finally
        {
            if ( writer != null )
            {
                writer.close();
            }
        }
    }
}
