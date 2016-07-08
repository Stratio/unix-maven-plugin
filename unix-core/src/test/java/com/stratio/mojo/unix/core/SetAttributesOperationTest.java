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

import fj.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import junit.framework.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.UnixFileMode.*;
import static com.stratio.mojo.unix.core.OperationTest.*;
import com.stratio.mojo.unix.util.*;
import org.easymock.*;
import org.easymock.internal.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class SetAttributesOperationTest
    extends TestCase
{
    public final static FileAttributes fileAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( _0755 ) );

    public final static FileAttributes directoryAttributes =
        new FileAttributes( some( "myuser" ), some( "mygroup" ), some( UnixFileMode._0644 ) );

    private final static List<String> nilStrings = nil();

    public void testBasic()
        throws Exception
    {
        Option<FileAttributes> changedAttributes = some( EMPTY.user( "myuser" ) );

//        assertEquals( FileType.FOLDER, files.files.getType() );
        MockControl control = MockControl.createControl( FileCollector.class );
        FileCollector fileCollector = (FileCollector) control.getMock();

        fileCollector.addFile( files.optJettyBinExtraApp, objects.optJettyBinExtraApp );
        control.setMatcher( new FsMatcher() );
        fileCollector.addFile( files.optJettyReadmeUnix, objects.optJettyReadmeUnix );
        fileCollector.addFile( files.optJettyBashProfile, objects.optJettyBashProfile );
        fileCollector.addDirectory( objects.optJettyBin );
        fileCollector.addDirectory( objects.optJetty );
        fileCollector.addDirectory( objects.opt );
        fileCollector.addDirectory( objects.base );
        fileCollector.apply( null );
        control.setMatcher( new AlwaysMatcher() );
        control.replay();

        new CopyDirectoryOperation( files.files, RelativePath.BASE, null, null, Option.<P2<String, String>>none(),
                                    fileAttributes, directoryAttributes ).perform( fileCollector );

        new SetAttributesOperation( RelativePath.BASE, nilStrings, nilStrings,
            Option.<FileAttributes>none(), Option.<FileAttributes>none() ).perform( fileCollector );

        new SetAttributesOperation( RelativePath.BASE, single( "**/bin/extra-app" ), nilStrings,
            changedAttributes, Option.<FileAttributes>none() ).perform( fileCollector );

        control.verify();
    }

    /*
    public void testApplyAttributes()
    {
        FileAttributes defaultAttributes = FileAttributes.EMPTY.user( "default" ).group( "default" ).mode( _0755 );

        SetAttributesOperation operation =
            new SetAttributesOperation( RelativePath.BASE, single( "**REMOVE ME/bin/*" ), nilStrings,
                                        some( EMPTY.user( "myuser" ) ), some( EMPTY ) );

        assertEquals( defaultAttributes,
                      operation.applyFileAttributes.some().f( objects.optJettyReadmeUnix, defaultAttributes ) );

        assertEquals( defaultAttributes,
                      operation.applyDirectoryAttributes.some().f( objects.optJettyBin, defaultAttributes ) );
        assertEquals( defaultAttributes.user( "myuser" ),
                      operation.applyFileAttributes.some().f( objects.optJettyBinExtraApp, defaultAttributes ) );
    }
    */
}
