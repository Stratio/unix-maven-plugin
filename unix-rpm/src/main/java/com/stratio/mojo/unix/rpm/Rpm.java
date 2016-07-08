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

import com.stratio.mojo.unix.util.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class Rpm
{
    private final String operation;

    private GeneralOptions generalOptions;

    private SelectOptions selectOptions;

    private QueryOptions queryOptions;

    private Rpm( String operation )
    {
        this.operation = operation;
    }

    public static GeneralOptions query()
    {
        Rpm rpm = new Rpm( "query" );
        rpm.generalOptions = rpm.new GeneralOptions();
        rpm.selectOptions = rpm.new SelectOptions();
        rpm.queryOptions = rpm.new QueryOptions();
        return rpm.generalOptions;
    }

    public class GeneralOptions
    {
        File basedir;

        boolean debug;

        boolean verbose;

        public GeneralOptions setBasedir( File basedir )
        {
            this.basedir = basedir;
            return this;
        }

        public GeneralOptions setDebug( boolean debug )
        {
            this.debug = debug;
            return this;
        }

        public GeneralOptions setVerbose( boolean verbose )
        {
            this.verbose = verbose;
            return this;
        }

        public SelectOptions withSelectOptions()
        {
            return selectOptions;
        }
    }

    public class SelectOptions
    {
        boolean all;

        File packageFile;

        public QueryOptions withQuery()
        {
            return queryOptions;
        }

        public SelectOptions setAll( boolean all )
        {
            this.all = all;
            return this;
        }

        public SelectOptions setPackage( File packageFile )
        {
            this.packageFile = packageFile;
            return this;
        }
    }

    public class QueryOptions
    {
        public boolean list;

        public QueryOptions setList( boolean list )
        {
            this.list = list;
            return this;
        }

        public void execute()
            throws IOException
        {
            Rpm.this.execute();
        }
    }

    private void setGeneralOptions( SystemCommand systemCommand, GeneralOptions generalOptions )
    {
        if ( generalOptions.verbose )
        {
            systemCommand.addArgument( "-v" );
        }
    }

    private void setSelectOptions( SystemCommand systemCommand, SelectOptions selectOptions )
    {
        if ( selectOptions.all )
        {
            systemCommand.addArgument( "--all" );
        }

        if ( selectOptions.packageFile != null )
        {
            systemCommand.addArgument( "--package" );
            systemCommand.addArgument( selectOptions.packageFile.getAbsolutePath() );
        }
    }

    private void setQueryOptions( SystemCommand systemCommand, QueryOptions queryOptions )
    {
        if ( queryOptions.list )
        {
            systemCommand.addArgument( "--list" );
        }
    }

    public void execute()
        throws IOException
    {
        SystemCommand command = new SystemCommand().
            withClosedStderrUnless( generalOptions.debug ).
            withClosedStdoutUnless( generalOptions.debug ).
            setBasedir( generalOptions.basedir ).
            setCommand( "rpm" ).
            addArgument( "--" + operation );

        setGeneralOptions( command, generalOptions );
        setSelectOptions( command, selectOptions );
        setQueryOptions( command, queryOptions );

        command.execute().
            assertSuccess();
    }

    public static boolean available()
    {
        return SystemCommand.available( "rpm" );
    }
}
