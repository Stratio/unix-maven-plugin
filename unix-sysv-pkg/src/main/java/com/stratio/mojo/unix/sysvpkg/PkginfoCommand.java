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
 package com.stratio.mojo.unix.sysvpkg;

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
import org.codehaus.plexus.util.*;

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkginfoCommand
{
    private File basedir = new File( "/" );

    private boolean debug;

    /**
     * -a: Specify the architecture of the package as arch.
     */
    private String arch;

    /**
     * -c: Display packages that match category. Categories are defined with the CATEGORY parameter in the pkginfo(4)
     * file. If more than one category is supplied, the package needs to match only one category in the list. The
     * match is not case specific.
     */
    private String category;

    /**
     * -d: Defines a device, device, on which the software resides. device can be an absolute directory pathname or
     * the identifiers for tape, floppy disk, removable disk, and so forth. The special token spool may be used to
     * indicate the default installation spool directory (/var/spool/pkg).
     */
    private File device;

    /**
     * -i: Display information for fully installed packages only.
     */
    private boolean fullyInstalledOnly;

    /**
     * -l: Specify long format, which includes all available information about the designated package(s).
     */
    private boolean longFormat;

    /**
     * -p: Display information for partially installed packages only.
     */
    private boolean partiallyInstalledOnly;

    /**
     * -r: List the installation base for relocatable packages.
     */
    private boolean listRelocationBaseForRelocatablePackages;

    /**
     * -r: Defines the full path name of a directory to use as the root_path. All files, including package system
     * information files, are relocated to a directory tree starting in the specified root_path.
     */
    private File rootPath;

    /**
     * -v: Specify the version of the package as version. The version is defined with the VERSION parameter in the
     * pkginfo(4) file. All compatible versions can be requested by preceding the version name with a tilde (~).
     * Multiple white spaces are replaced with a single white space during version comparison.
     */
    private String version;

    public PkginfoCommand setBasedir( File basedir )
    {
        this.basedir = basedir;
        return this;
    }

    public PkginfoCommand setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public PkginfoCommand setArch( String arch )
    {
        this.arch = arch;
        return this;
    }

    public PkginfoCommand setCategory( String category )
    {
        this.category = category;
        return this;
    }

    public PkginfoCommand setDevice( File device )
    {
        this.device = device;
        return this;
    }

    public PkginfoCommand setFullyInstalledOnly( boolean fullyInstalledOnly )
    {
        this.fullyInstalledOnly = fullyInstalledOnly;
        return this;
    }

    public PkginfoCommand setLongFormat( boolean longFormat )
    {
        this.longFormat = longFormat;
        return this;
    }

    public PkginfoCommand setPartiallyInstalledOnly( boolean partiallyInstalledOnly )
    {
        this.partiallyInstalledOnly = partiallyInstalledOnly;
        return this;
    }

    public PkginfoCommand setListRelocationBaseForRelocatablePackages(
        boolean listRelocationBaseForRelocatablePackages )
    {
        this.listRelocationBaseForRelocatablePackages = listRelocationBaseForRelocatablePackages;
        return this;
    }

    public PkginfoCommand setRootPath( File rootPath )
    {
        this.rootPath = rootPath;
        return this;
    }

    public PkginfoCommand setVersion( String version )
    {
        this.version = version;
        return this;
    }

    /**
     * Executes pkginfo with -q.
     * <p/>
     * Used from a program to check whether or not a package has been installed.
     */
    public int executeQuiet()
        throws IOException
    {
        return execute( "-q" );
    }

    /**
     * Executes pkginfo with -l.
     * <p/>
     * The -l flag includes all available information about the designated package(s).
     */
    public void executeWithLongFormat()
        throws IOException
    {
        if ( execute( "-l" ) != 0 )
        {
            throw new IOException( "pkginfo exited with a non-null exit code." );
        }
    }

    /**
     * Executes pkginfo with -x.
     * <p/>
     * The -x flag designate an extracted listing of package information. The listing contains the package
     * abbreviation, package name, package architecture (if available) and package version (if available).
     */
    public void executeWithExtractedListing()
        throws IOException
    {
        if ( execute( "-x" ) != 0 )
        {
            throw new IOException( "pkginfo exited with a non-null exit code." );
        }
    }

    private int execute( String mode )
        throws IOException
    {
        SystemCommand command = new SystemCommand().
            setCommand( "pkginfo" ).
            setBasedir( basedir ).
            withClosedStderrUnless( debug ).
            withClosedStdoutUnless( debug );

        if ( StringUtils.isNotEmpty( arch ) )
        {
            command.addArgument( "-a" ).addArgument( arch );
        }

        if ( StringUtils.isNotEmpty( category ) )
        {
            command.addArgument( "-c" ).addArgument( category );
        }

        if ( device != null )
        {
            command.addArgument( "-d" ).addArgument( device.getAbsolutePath() );
        }

        if ( fullyInstalledOnly )
        {
            command.addArgument( "-i" );
        }

        if ( longFormat )
        {
            command.addArgument( "-l" );
        }

        if ( partiallyInstalledOnly )
        {
            command.addArgument( "-p" );
        }

        if ( listRelocationBaseForRelocatablePackages )
        {
            command.addArgument( "-r" );
        }

        if ( rootPath != null )
        {
            command.addArgument( "-R" ).addArgument( rootPath.getAbsolutePath() );
        }

        if ( StringUtils.isNotEmpty( version ) )
        {
            command.addArgument( "-v" ).addArgument( version );
        }

        command.addArgument( mode );

        return command.execute().exitValue;
    }

    public static boolean available()
    {
        return SystemCommand.available( "pkginfo" );
    }
}
