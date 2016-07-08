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

import fj.data.*;
import com.stratio.mojo.unix.util.*;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class Rpmbuild
{
    private boolean debug;

    private File buildroot;

    private File specFile;

    private Option<String> rpmbuild = Option.none();

    private List<String> defines = new LinkedList<String>();

    public Rpmbuild setDebug( boolean debug )
    {
        this.debug = debug;

        return this;
    }

    public Rpmbuild setBuildroot( File buildroot )
    {
        this.buildroot = buildroot;
        return this;
    }

    public Rpmbuild setSpecFile( File specFile )
    {
        this.specFile = specFile;
        return this;
    }

    public Rpmbuild setRpmbuild( Option<String> rpmbuild )
    {
        this.rpmbuild = rpmbuild;
        return this;
    }

    public Rpmbuild define( String define )
    {
        this.defines.add( define );
        return this;
    }

    public void buildBinary()
        throws IOException
    {
        if ( specFile == null )
        {
            throw new IOException( "Package specFile is not set." );
        }

        // Seems like pkgmk doesn't like its stderr/stdout to be closed.
        SystemCommand.StringBufferLineConsumer out = new SystemCommand.StringBufferLineConsumer();

        SystemCommand command = new SystemCommand().
            setBasedir( new File( "/" ) ).
            dumpCommandIf( true ).
            withStderrConsumer( out ).
            withStdoutConsumer( out ).
            setCommand( rpmbuild() ).
            addArgument( "-bb" ).
            addArgument( "--buildroot" ).
            addArgument( buildroot.getAbsolutePath() ).
            addArgument( specFile.getAbsolutePath() );

        // --target <arch> used to be a part of the rpmbuild command but to allow different architectures (not
        // only "noarch") it has to be specified in the spec file.

        // TODO: Only the _topdir defines should be there, the others should be in the spec file
        // TODO: This should be configurable
        for ( String define : defines )
        {
            command.
                addArgument( "--define" ).
                addArgument( define );
        }

        SystemCommand.ExecutionResult result = command.execute();




        if ( debug )
        {

            OutputStreamWriter logsOut = new OutputStreamWriter(new FileOutputStream(buildroot.getParentFile().getAbsolutePath() +"/build.log", true));
            logsOut.write(result.command + " output:" );
            logsOut.write(out.toString());

            logsOut.close();

            System.out.println( "------------------------------------------------------" );
            System.out.println( result.command + " output:" );
            System.out.println( out );
        }

        result.
            assertSuccess();
    }

    public boolean available()
    {
        return SystemCommand.available( rpmbuild() );
    }

    private String rpmbuild()
    {
        return rpmbuild.orSome( "rpmbuild" );
    }
}
