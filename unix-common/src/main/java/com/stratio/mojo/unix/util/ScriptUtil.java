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
import static fj.data.List.*;
import static fj.data.Option.*;
import static java.lang.Math.*;
import com.stratio.mojo.unix.java.*;
import static com.stratio.mojo.unix.java.FileF.*;
import static com.stratio.mojo.unix.util.FileModulator.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * <h2>Single Format</h2>
 * <ul>
 * <li>Files use their "native" names.</li>
 * <li>Used for <code>packaging=FORMAT</code></li>
 * </ul>
 * <pre>
 * scripts/
 * |-- preinstall
 * |-- postinstall     <- Common install actions for all packages
 * `-- postinstall-a   <- Specific install actions for the "a" package
 * </pre>
 * <p/>
 * <h2>Multiple Formats</h2>
 * <ul>
 * <li>Files do not use their "native" names, has to be one of pre-install, post-install, pre-remove, post-remove.</li>
 * <li>Use for attached executions. Note that the format always has to be present because of how the attached mojos
 * work. They work independently and there is no way of telling if more that one attached mojo will run or not.</li>
 * </ul>
 * <pre>
 * scripts/
 * |-- post-install                 Common for all packages, all formats
 * |-- post-install-a               Common for package "a" in all formats
 * |-- post-install-a-deb
 * |-- post-install-a-pkg
 * |-- post-install-a-rpm
 * |-- post-install-rpm             Common for all packages in "rpm" format
 * |-- post-install-b-deb
 * |-- post-install-b-pkg
 * `-- post-install-b-rpm
 * </pre>
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public final class ScriptUtil
{
    private final ScriptFile preInstall;

    private final ScriptFile postInstall;

    private final ScriptFile preRemove;

    private final ScriptFile postRemove;

    private final List<String> customScripts;

    public enum Strategy
    {
        SINGLE( ScriptFile.specificNameF ),
        MULTIPLE( ScriptFile.commonNameF );

        private final F<ScriptFile, String> accessor;

        Strategy( F<ScriptFile, String> accessor )
        {
            this.accessor = accessor;
        }
    }

    private final static class ScriptFile
    {
        String commonName;

        String specificName;

        private ScriptFile( String commonName, String specificName )
        {
            this.commonName = commonName;
            this.specificName = specificName;
        }

        public File toFile( File toDir )
        {
            return new File( toDir, specificName );
        }

        public static F<ScriptFile, String> commonNameF = new F<ScriptFile, String>()
        {
            public String f( ScriptFile scriptFile )
            {
                return scriptFile.commonName;
            }
        };

        public static F<ScriptFile, String> specificNameF = new F<ScriptFile, String>()
        {
            public String f( ScriptFile scriptFile )
            {
                return scriptFile.specificName;
            }
        };
    }

    public ScriptUtil( String preInstall, String postInstall, String preRemove, String postRemove )
    {
        Validate.validateNotNull( preInstall, postInstall, preRemove, postRemove );
        this.preInstall = new ScriptFile( "pre-install", preInstall );
        this.postInstall = new ScriptFile( "post-install", postInstall );
        this.preRemove = new ScriptFile( "pre-remove", preRemove );
        this.postRemove = new ScriptFile( "post-remove", postRemove );
        customScripts = nil();
    }

    private ScriptUtil( ScriptFile preInstall, ScriptFile postInstall, ScriptFile preRemove, ScriptFile postRemove,
                        List<String> customScripts )
    {
        Validate.validateNotNull( preInstall, postInstall, preRemove, postRemove, customScripts );
        this.preInstall = preInstall;
        this.postInstall = postInstall;
        this.preRemove = preRemove;
        this.postRemove = postRemove;
        this.customScripts = customScripts;
    }

    public ScriptUtil customScript( String specificName )
    {
        return new ScriptUtil( preInstall, postInstall, preRemove, postRemove, customScripts.cons( specificName ) );
    }

    public final static class Execution
    {
        public final Option<Callable<File>> preInstall;

        public final Option<Callable<File>> postInstall;

        public final Option<Callable<File>> preRemove;

        public final Option<Callable<File>> postRemove;

        public final List<Callable<File>> customScripts;

        public Execution( Option<Callable<File>> preInstall, Option<Callable<File>> postInstall,
                          Option<Callable<File>> preRemove, Option<Callable<File>> postRemove,
                          List<Callable<File>> customScripts )
        {
            this.preInstall = preInstall;
            this.postInstall = postInstall;
            this.preRemove = preRemove;
            this.postRemove = postRemove;
            this.customScripts = customScripts;
        }

        public Result execute()
            throws Exception
        {
            List<File> scripts = nil();

            for ( Callable<File> customScript : customScripts )
            {
                scripts = scripts.cons( customScript.call() );
            }

            return new Result( preInstall.isSome() ? some( preInstall.some().call() ) : Option.<File>none(),
                               postInstall.isSome() ? some( postInstall.some().call() ) : Option.<File>none(),
                               preRemove.isSome() ? some( preRemove.some().call() ) : Option.<File>none(),
                               postRemove.isSome() ? some( postRemove.some().call() ) : Option.<File>none(), scripts );
        }
    }

    public final static class Result
    {
        public final Option<File> preInstall;

        public final Option<File> postInstall;

        public final Option<File> preRemove;

        public final Option<File> postRemove;

        public final List<File> customScripts;

        public Result( Option<File> preInstall, Option<File> postInstall, Option<File> preRemove,
                       Option<File> postRemove, List<File> customScripts )
        {
            this.preInstall = preInstall;
            this.postInstall = postInstall;
            this.preRemove = preRemove;
            this.postRemove = postRemove;
            this.customScripts = customScripts;
        }
    }

    public Execution createExecution( String id, String format, File scripts, File toDir, Strategy strategy )
    {
        F<String, List<String>> expand = curry( modulatePath, id, format );
        F<ScriptFile, List<String>> f = compose( expand, strategy.accessor );
        F<String, File> newScriptsFile = curry( FileF.newFile, scripts );

        List<File> preInstallFiles = f.f( preInstall ).map( newScriptsFile ).filter( canRead );
        List<File> postInstallFiles = f.f( postInstall ).map( newScriptsFile ).filter( canRead );
        List<File> preRemoveFiles = f.f( preRemove ).map( newScriptsFile ).filter( canRead );
        List<File> postRemoveFiles = f.f( postRemove ).map( newScriptsFile ).filter( canRead );

        List<Callable<File>> customFiles = nil();
        for ( String customScript : customScripts )
        {
            F<ScriptFile, List<String>> toFilesCustom = compose( expand, ScriptFile.specificNameF );
            List<File> list = toFilesCustom.f( new ScriptFile( null, customScript ) ).
                map( newScriptsFile ).
                filter( canRead );

            if ( list.isNotEmpty() )
            {
                customFiles = customFiles.cons( curry( copyFiles, new File( toDir, customScript ) ).f( list ) );
            }
        }

        return new Execution(
            iif( List.<File>isNotEmpty_(), preInstallFiles ).map( curry( copyFiles, preInstall.toFile( toDir ) ) ),
            iif( List.<File>isNotEmpty_(), postInstallFiles ).map( curry( copyFiles, postInstall.toFile( toDir ) ) ),
            iif( List.<File>isNotEmpty_(), preRemoveFiles ).map( curry( copyFiles, preRemove.toFile( toDir ) ) ),
            iif( List.<File>isNotEmpty_(), postRemoveFiles ).map( curry( copyFiles, postRemove.toFile( toDir ) ) ), customFiles );
    }

    F2<File, File, Callable<File>> customToCallable = new F2<File, File, Callable<File>>()
    {
        public Callable<File> f( final File toDir, final File file )
        {
            return new Callable<File>()
            {
                public File call()
                    throws Exception
                {
                    return copyFiles( new File( toDir, file.getName() ), single( file ) );
                }
            };
        }
    };

    private static File copyFiles( File to, List<File> files )
        throws IOException
    {
        Show.listShow( Show.<File>anyShow() ).println( files );
        FileOutputStream fos = null;
        FileInputStream fis = null;

        try
        {
            long lastModified = 0;

            if ( !to.getParentFile().isDirectory() )
            {
                FileUtils.forceMkdir( to.getParentFile() );
            }

            fos = new FileOutputStream( to );

            for ( File file : files )
            {
                fis = new FileInputStream( file );
                IOUtil.copy( fis, fos );
                fis.close();
                lastModified = max( lastModified, file.lastModified() );
            }

            fos.close();

            // TODO: Check that the files differ to prevent writing new files.

            if ( !to.setLastModified( lastModified ) )
            {
                throw new IOException( "Unable to set last modified on '" + to.getAbsolutePath() + "'." );
            }

            return to;
        }
        finally
        {
            IOUtil.close( fos );
            IOUtil.close( fis );
        }
    }

    private static F2<File, List<File>, Callable<File>> copyFiles = new F2<File, List<File>, Callable<File>>()
    {
        public Callable<File> f( final File toFile, final List<File> files )
        {
            return new Callable<File>()
            {
                public File call()
                    throws Exception
                {
                    return copyFiles( toFile, files );
                }
            };
        }
    };
}
