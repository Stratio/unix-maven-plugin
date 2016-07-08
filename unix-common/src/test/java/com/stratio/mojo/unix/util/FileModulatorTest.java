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

import fj.data.*;
import static fj.data.List.*;
import static fj.Equal.*;
import junit.framework.*;
import static com.stratio.mojo.unix.util.FileModulator.*;

/**
 */
public class FileModulatorTest
    extends TestCase
{
    public void testScripts()
    {
        List<String> modulated = modulatePath( "id", "format", "src/main/unix/scripts/postinstall" );

        assertTrue( listEqual( stringEqual ).eq( modulated, list( "src/main/unix/scripts/postinstall",
                                                                  "src/main/unix/scripts/postinstall-id",
                                                                  "src/main/unix/scripts/postinstall-format",
                                                                  "src/main/unix/scripts/postinstall-id-format" ) ) );
    }

    public void testFilesModulation()
    {
        List<String> modulated = modulatePath( "id", "format", "src/main/unix/files" );

        assertTrue( listEqual( stringEqual ).eq( modulated, list( "src/main/unix/files", "src/main/unix/files-id",
                                                                  "src/main/unix/files-format",
                                                                  "src/main/unix/files-id-format" ) ) );
    }
}
