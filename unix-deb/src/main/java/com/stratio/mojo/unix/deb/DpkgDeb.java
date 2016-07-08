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
 package com.stratio.mojo.unix.deb;

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
import com.stratio.mojo.unix.util.*;

import java.io.*;

/**
 * Builds a DEB package using fakeroot dpkg-deb.  This command creates a DEB
 * package in buildDir using the contents of stageDir.  This classes
 * assumes that stageDir is going to be a direct descendant of buildDir.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class DpkgDeb
{
    private boolean debug;

    private File packageRoot;

    private File debFile;

    private boolean useFakeroot;

    private Option<String> dpkgDeb = Option.none();

    public DpkgDeb setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public DpkgDeb setPackageRoot( File packageRoot )
    {
        this.packageRoot = packageRoot;
        return this;
    }

    public DpkgDeb setDebFile( File debFile )
    {
        this.debFile = debFile;
        return this;
    }

    public DpkgDeb setUseFakeroot( boolean useFakeroot )
    {
        this.useFakeroot = useFakeroot;
        return this;
    }

    public DpkgDeb setDpkgDeb( Option<String> dpkgDeb )
    {
        this.dpkgDeb = dpkgDeb;
        return this;
    }

    public void execute()
        throws IOException
    {
        if ( packageRoot == null )
        {
            throw new IOException( "Package root is not set." );
        }

        if ( debFile == null )
        {
            throw new IOException( "Path to output .deb is not set." );
        }

        new SystemCommand().
            setCommand( useFakeroot ? "fakeroot" : dpkgDeb() ).
            dumpCommandIf( debug ).
            withIgnoringStderrUnless( debug ).
            withIgnoringStdoutUnless( debug ).
            addArgumentIf( useFakeroot, dpkgDeb() ).
            addArgument( "-Zgzip" ).
            addArgument( "-b" ).
            addArgument( packageRoot.getAbsolutePath() ).
            addArgument( debFile.getAbsolutePath() ).
            execute().
            assertSuccess();
    }

    public boolean available()
    {
        return SystemCommand.available( dpkgDeb() );
    }

    private String dpkgDeb()
    {
        return dpkgDeb.orSome( "dpkg-deb" );
    }
}
