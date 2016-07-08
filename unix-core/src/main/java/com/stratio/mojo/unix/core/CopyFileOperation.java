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

import com.stratio.mojo.unix.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.util.*;

import static com.stratio.mojo.unix.UnixFsObject.regularFile;
import static com.stratio.mojo.unix.util.Validate.*;
import com.stratio.mojo.unix.util.line.*;
import org.joda.time.LocalDateTime;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class CopyFileOperation
    implements AssemblyOperation
{
    private final FileAttributes attributes;

    private final Fs<?> fromFile;

    private final RelativePath toFile;

    public CopyFileOperation( FileAttributes attributes, Fs<?> fromFile, RelativePath toFile )
    {
        validateNotNull( attributes, fromFile, toFile );

        this.attributes = attributes;
        this.fromFile = fromFile;
        this.toFile = toFile;
    }

    public void perform( FileCollector fileCollector )
        throws IOException
    {
        LocalDateTime lastModified = new LocalDateTime( fromFile.lastModified() );
        UnixFsObject.RegularFile file = regularFile( toFile, lastModified, fromFile.size(), attributes );
        fileCollector.addFile( fromFile, file );
    }

    public void streamTo( LineStreamWriter streamWriter )
    {
        streamWriter.add( "Copy file" ).
            add( " From: " + fromFile.absolutePath() ).
            add( " To: " + toFile ).
            add( " Attributes: " + attributes );
    }
}
