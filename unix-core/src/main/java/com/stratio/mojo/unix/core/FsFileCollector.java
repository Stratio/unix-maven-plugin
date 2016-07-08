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
import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.io.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;

import java.io.*;
import java.util.*;
import java.util.List;

import static fj.P.p;
import static fj.data.Option.some;
import static com.stratio.mojo.unix.UnixFsObject.Replacer;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class FsFileCollector
    implements FileCollector
{
    private final List<IoEffect> operations = new ArrayList<IoEffect>();

    private final List<F<UnixFsObject, Option<UnixFsObject>>> applications = new ArrayList<F<UnixFsObject, Option<UnixFsObject>>>();

    public final LocalFs root;

    public FsFileCollector( LocalFs root) throws IOException {
        this.root = root;
        root.mkdir();
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        operations.add( packageDirectory( directory.path ) );
    }

    public void addFile( Fs fromFile, UnixFsObject.RegularFile file )
    {
        operations.add( new CopyFileIoEffect( fromFile, file ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
        throws IOException
    {
        operations.add( packageSymlink( symlink ) );
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        applications.add( f );
    }

    public void collect()
        throws Exception
    {
        for ( IoEffect operation : operations )
        {
            operation.run();
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private IoEffect packageDirectory( final RelativePath path )
    {
        return new IoEffect()
        {
            public void run()
                throws IOException
            {
                mkdirs( root.resolve( path ).file );
            }
        };
    }

    private IoEffect packageSymlink( final UnixFsObject.Symlink symlink )
    {
        return new IoEffect()
        {
            public void run()
                throws IOException
            {
                mkdirs( root.resolve( symlink.path ).file.getParentFile() );

                UnixUtil.symlink( root.file, symlink.value, symlink.path );
            }
        };
    }

    private void mkdirs( File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return;
        }

        if ( !file.mkdirs() )
        {
            throw new IOException( "Unable to create root directory: " + root.file.getAbsolutePath() );
        }
    }

    private class CopyFileIoEffect
        implements IoEffect
    {
        private final Fs from;
        private final UnixFsObject.RegularFile to;

        public CopyFileIoEffect( Fs from, UnixFsObject.RegularFile to )
        {
            this.from = from;
            this.to = to;
        }

        public void run()
            throws IOException
        {
            UnixFsObject adjustedTo = to;

            for ( F<UnixFsObject, Option<UnixFsObject>> f : applications )
            {
                adjustedTo = f.f( adjustedTo ).orSome( adjustedTo );
            }

            P2<InputStream, Option<Long>> p2 =
                filtersAndLineEndingHandingInputStream( adjustedTo, from.inputStream() );

            root.resolve( adjustedTo.path ).copyFrom( from, p2._1() );
        }
    }

    /**
     * Returns a pair with the new size and an InputStream that will contain the properly filtered data.
     * <p/>
     * Technically this could be streaming, but *should* only be applied to smaller files. The main problem is only
     * heap usage so a 100MB file should be easily process in-memory. But not many of them.
     */
    public static P2<InputStream, Option<Long>> filtersAndLineEndingHandingInputStream( UnixFsObject file,
                                                                                        InputStream inputStream )
        throws IOException
    {
        // With no filters *and* keeping the line endings we can stream the file directly. Like a BOSS!
        if ( !needsFiltering( file ) )
        {
            return p( inputStream, Option.<Long>none() );
        }

        // If the file has to be either filtered or have its line endings changed, it has to be read through a Reader.
        // Detecting the line endings and skipping line ending conversion might fail (because of inconsistent line
        // endings) so we'll convert those too.

        // We have to buffer the file in memory. It might be a good idea to check if the file
        // is big (> 10MB) and copy it to disk. It might be smart to print a warning if that
        // happens as the user probably has a weird configuration. Like trying to filter a 100MB EAR file.

        byte[] eol;
        if ( file.lineEnding.isKeep() )
        {
            P2<InputStream, LineEnding> x = LineEnding.detect( inputStream );
            inputStream = x._1();
            eol = x._2().eol();
        }
        else
        {
            eol = file.lineEnding.eol();
        }

        // TODO: Ideally create an output stream that doesn't create a new array on
        // toByteArray, but instead can be used as an InputStream directly.
        // TODO: This is going to add additional EOL at the end of the file. Fuck it.
        ByteArrayOutputStream output = new ByteArrayOutputStream( (int) file.size );

        // This implicitly uses the platform encoding. This will probably bite someone.
        BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ), 1024 * 128 );

        String line = reader.readLine();

        while ( line != null )
        {
            Iterable<Replacer> replacers = file.replacers;

            for ( Replacer replacer : replacers )
            {
                line = replacer.replace( line );
            }

            output.write( line.getBytes() );
            output.write( eol );

            line = reader.readLine();
        }
        inputStream.close();

        inputStream = new ByteArrayInputStream( output.toByteArray() );
        long size = output.size();

        return p( inputStream, some( size ) );
    }

    public static boolean needsFiltering( UnixFsObject file )
    {
        return file.replacers.isNotEmpty() || !file.lineEnding.isKeep();
    }
}
