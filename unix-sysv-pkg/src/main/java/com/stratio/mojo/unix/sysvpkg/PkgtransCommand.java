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

import java.io.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PkgtransCommand
{
    private File basedir;

    private boolean debug;

    /**
     * -a: Use public key certificate associated with friendlyName alias, and the corresponding private key. See
     * KEYSTORE LOCATIONS and  KEYSTORE AND CERTIFICATE FORMATS in pkgadd(1M) for more information.
     */
    private String alias;

    /**
     * -g: Sign resulting datastream.
     */
    private boolean signDatastream;

    /**
     * -i: Copies only the pkginfo(4) and pkgmap(4) files.
     */
    private boolean copyOnlyPkginfoAndPkgmap;

    /**
     * -k: Use keystore to retrieve private key used to generate signature. If it not specified, default locations are
     * searched to find the specified private key specified by -a. If no alias is given, and multiple keys exist in the
     * key store, pkgtrans will abort. See KEYSTORE LOCATIONS and KEYSTORE AND CERTIFICATE FORMATS in pkgadd(1M) for
     * more information on search locations and formats.
     * <p/>
     * When running as a user other than root, the default base directory for certificate searching is ~/.pkg/security,
     * where ~ is the home directory of the user invoking pkgtrans.
     */
    private File keystore;

    /**
     * -n: Creates a new instance of the package on the destination device if any instance of this package already
     * exists, up to the number specified by the MAXINST variable in the pkginfo(4) file.
     */
    private boolean newInstance;

    /**
     * -o: Overwrites the same instance on the destination  device. Package instance will be overwritten if it
     * already exists.
     */
    private boolean overwrite;

    /**
     * -P: Supply password used to decrypt the keystore. See PASS PHRASE ARGUMENTS in pkgadd(1M) for details on the
     * syntax of the argument to this option.
     */
    private String password;

    /**
     * -s: Indicates that the package should be written to device2 as a datastream rather than as a file system. The
     * default behavior is to write a file system format on devices that support both formats.
     */
    private boolean asDatastream;

    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }

    public PkgtransCommand setDebug( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public PkgtransCommand setAlias( String alias )
    {
        this.alias = alias;
        return this;
    }

    public PkgtransCommand setSignDatastream( boolean signDatastream )
    {
        this.signDatastream = signDatastream;
        return this;
    }

    public PkgtransCommand setCopyOnlyPkginfoAndPkgmap( boolean copyOnlyPkginfoAndPkgmap )
    {
        this.copyOnlyPkginfoAndPkgmap = copyOnlyPkginfoAndPkgmap;
        return this;
    }

    public PkgtransCommand setKeystore( File keystore )
    {
        this.keystore = keystore;
        return this;
    }

    public PkgtransCommand setNewInstance( boolean newInstance )
    {
        this.newInstance = newInstance;
        return this;
    }

    public PkgtransCommand setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
        return this;
    }

    public PkgtransCommand setPassword( String password )
    {
        this.password = password;
        return this;
    }

    public PkgtransCommand setAsDatastream( boolean asDatastream )
    {
        this.asDatastream = asDatastream;
        return this;
    }

    /**
     * @see #execute(java.io.File, java.io.File, String)
     */
    public void execute( File device1, File device2 )
        throws IOException
    {
        execute( device1, device2, null );
    }

    /**
     * @param device1 Indicates the source device. The package or packages on this device will be translated and
     *                placed on device2.
     * @param device2 Indicates the destination device. Translated packages will be placed on this device.
     * @param pkginst Specifies which package instance or instances on device1 should be translated. The token all may
     *                be used to indicate all packages. pkginst.* can be used to indicate all instances of a package. If no packages
     *                are defined, a prompt shows all packages on the device and asks which to translate.
     * @throws IOException
     */
    public void execute( File device1, File device2, String pkginst )
        throws IOException
    {
        if ( device1 == null || device2 == null )
        {
            throw new IOException( "Both device1 and device2 must be given." );
        }

        // Seems like pkgmk doesn't like its stderr/stdout to be closed.
        SystemCommand.StringBufferLineConsumer out = new SystemCommand.StringBufferLineConsumer();

        SystemCommand command = new SystemCommand().
            setCommand( "pkgtrans" ).
            setBasedir( basedir ).
            dumpCommandIf( debug ).
            withStderrConsumer( out ).
            withStdoutConsumer( out ).
            addArgumentIfNotEmpty( alias, "-a" ).addArgumentIfNotEmpty( alias ).
            addArgumentIf( signDatastream, "-g" ).
            addArgumentIf( copyOnlyPkginfoAndPkgmap, "-i" );

        if ( keystore != null )
        {
            command.addArgument( "-k" ).addArgument( keystore.getAbsolutePath() );
        }

        SystemCommand.ExecutionResult result = command.
            addArgumentIf( newInstance, "-n" ).
            addArgumentIf( overwrite, "-o" ).
            addArgumentIfNotEmpty( password, "-P" ).addArgumentIfNotEmpty( password ).
            addArgumentIf( asDatastream, "-s" ).
            addArgument( device1.getAbsolutePath() ).
            addArgument( device2.getAbsolutePath() ).
            addArgumentIfNotEmpty( pkginst ).
            execute();

        if ( debug )
        {
            System.out.println( "------------------------------------------------------" );
            System.out.println( "pkgtrans output:" );
            System.out.println( out );
        }

        result.assertSuccess();
    }
}
