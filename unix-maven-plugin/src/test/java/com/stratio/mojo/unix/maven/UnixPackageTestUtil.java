/**
 * Copyright (C) 2003 Stratio (http://stratio.com)
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
 package com.stratio.mojo.unix.maven;

import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import static java.util.regex.Pattern.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.apache.maven.plugin.logging.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.PackageParameters.*;
import static com.stratio.mojo.unix.PackageVersion.*;
import static com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.io.LineEnding.*;
import com.stratio.mojo.unix.io.fs.*;
import com.stratio.mojo.unix.maven.plugin.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import static com.stratio.mojo.unix.util.ScriptUtil.Strategy.*;
import static org.codehaus.plexus.PlexusTestCase.*;
import static org.codehaus.plexus.util.FileUtils.*;
import org.joda.time.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class UnixPackageTestUtil<UP extends UnixPackage<UP, PP>, PP extends UnixPackage.PreparedPackage>
{
    private final String id;

    private final PackagingFormat<UP> packagingFormat;

    private final LocalDateTime now = new LocalDateTime();

    private static final PackageVersion version = packageVersion( "1.0", "123", false, some( "1" ) );

    public final PackageParameters parameters =
        packageParameters( "mygroup", "myartifact", version, "id", "default-name", Option.<java.lang.String>none(),
                           EMPTY, EMPTY ).
            contact( "Kurt Cobain" ).
            architecture( "all" ).
            license( "BSD" ).excludeDirs(List.<String>nil());

    public UnixPackageTestUtil( String id, PackagingFormat<UP> packagingFormat )
    {
        this.id = id;
        this.packagingFormat = packagingFormat;
    }

    protected UP extraStuff( UP up )
    {
        return up;
    }

    public void testFiltering()
        throws Exception
    {
        LocalFs resources = new LocalFs( getTestFile( "src/test/resources/test-filtering" ) );
        LocalFs root = new LocalFs( getTestFile( "target/filtering-" + id ) );
        deleteDirectory( root.file );
        root.mkdir();
        LocalFs workingDirectory = root.resolve( "working-directory" );

        UP pkg = packagingFormat.start( new SystemStreamLog() ).
            parameters( parameters ).
            debug( true ).
            workingDirectory( workingDirectory );

        pkg = extraStuff( pkg );

        pkg.beforeAssembly( EMPTY, now );
        List<UnixFsObject.Replacer> replacers =
            single( new UnixFsObject.Replacer( quote( "${project.version}" ), "1.0" ) );
        UnixFsObject.RegularFile file =
            regularFile( relativePath( "/config.properties" ), now, 0, EMPTY, replacers, unix );
        pkg.addFile( resources.resolve( "config.properties" ), file );

        pkg.prepare( SINGLE );

        LocalFs config = workingDirectory.resolve( "assembly" ).resolve( "config.properties" );
        assertTrue( config.isFile() );
        assertEquals( 12, config.size() );
    }
}
