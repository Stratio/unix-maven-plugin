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
 package com.stratio.mojo.unix.ar;

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

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class Ar
{
    public static NewAr create()
    {
        return new NewAr();
    }

    public static ArReader read( File file )
        throws IOException
    {
        return new ArReader( file );
    }

    public static class NewAr
    {
        private final List<ArFile> files = new ArrayList<ArFile>();

        public class NewArFile
        {
            ArFile file;

            public NewArFile withUid( int uid )
            {
                file.ownerId = uid;

                return this;
            }

            public NewArFile withGid( int gid )
            {
                file.groupId = gid;

                return this;
            }

            public NewAr done()
            {
                files.add( file );
                return NewAr.this;
            }
        }

        public NewAr addFileDone( File file )
        {
            files.add( ArFile.fromFile( file ) );

            return this;
        }

        public NewArFile addFile( File file )
        {
            NewArFile f = new NewArFile();
            f.file = ArFile.fromFile( file );
            return f;
        }

        public void storeToFile( File file )
            throws IOException
        {
            ArWriter writer = null;
            try
            {
                writer = new ArWriter( file );

                for ( ArFile arFile : files )
                {
                    writer.add( arFile );
                }
            }
            finally
            {
                ArUtil.close( writer );
            }
        }
    }
}
