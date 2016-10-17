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

import static fj.Bottom.*;
import fj.*;
import static fj.Function.*;
import fj.data.List;
import fj.data.*;
import static fj.data.Option.*;
import com.stratio.mojo.unix.*;
import static com.stratio.mojo.unix.FileAttributes.*;
import static com.stratio.mojo.unix.PackageFileSystem.*;
import com.stratio.mojo.unix.UnixFsObject.*;
import static com.stratio.mojo.unix.java.FileF.*;
import static com.stratio.mojo.unix.java.StringF.*;
import com.stratio.mojo.unix.util.*;
import static com.stratio.mojo.unix.util.RelativePath.*;
import com.stratio.mojo.unix.util.line.*;
import static com.stratio.mojo.unix.util.line.LineStreamUtil.*;
import org.codehaus.plexus.util.*;
import org.joda.time.*;
import static org.joda.time.LocalDateTime.*;

import java.io.*;
import java.util.*;

/**
 * TODO: Split this file into two parts; a part which has all the meta data and one that has all the file data.
 * <p/>
 * A purely meta data one is useful for parts of the code that just create SPEC files and testing.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class SpecFile
    implements LineProducer
{

    /*
    Really ugly...
     */
    public static java.util.List<String> excludedSysPaths  = new ArrayList();

    public String version;

    public String release;

    // Will be generated if not set
    public String name;

    public String summary;

    public String license;

    public String distribution;

    public String vendor;

    public String url;

    public String group;

    public String packager;

    public List<String> defineStatements = List.nil();

    public List<String> provides = List.nil();

    public List<String> requires = List.nil();

    public List<String> conflicts = List.nil();

    public String prefix;

    public File buildRoot;

    public static java.util.List<String> confFile = new ArrayList<String>();

    public String buildArch;

    public String description;

    public boolean dump;

    // Create a default default file system for testing
    private final UnixFsObject DEFAULT_FS_ROOT = UnixFsObject.directory( RelativePath.BASE, LocalDateTime.fromDateFields( new Date() ), EMPTY );
    private final UnixFsObject DEFAULT_DEFAULT = UnixFsObject.directory( RelativePath.BASE, LocalDateTime.fromDateFields( new Date() ), EMPTY );

    private PackageFileSystem<Object> fileSystem = create( new PlainPackageFileSystemObject( DEFAULT_FS_ROOT ),
                                                           new PlainPackageFileSystemObject( DEFAULT_DEFAULT ) );

    public Option<File> includePre = none();

    public Option<File> includePost = none();

    public Option<File> includePreun = none();

    public Option<File> includePostun = none();

    public void beforeAssembly( Directory defaultDirectory )
    {
        excludedSysPaths.add("/DEBIAN");
        excludedSysPaths.add("/DEBIAN/conffiles");

        Validate.validateNotNull( defaultDirectory );

        Directory root = UnixFsObject.directory( BASE, fromDateFields( new Date( 0 ) ), EMPTY );

        fileSystem = create( new PlainPackageFileSystemObject( root ),
                             new PlainPackageFileSystemObject( defaultDirectory ) );
    }

    public void addFile( UnixFsObject.RegularFile file )
    {
        fileSystem = fileSystem.addFile( new PlainPackageFileSystemObject( file ) );
    }

    public void addDirectory( UnixFsObject.Directory directory )
    {
        fileSystem = fileSystem.addDirectory( new PlainPackageFileSystemObject( directory ) );
    }

    public void addSymlink( UnixFsObject.Symlink symlink )
    {
        fileSystem = fileSystem.addSymlink( new PlainPackageFileSystemObject( symlink ) );
    }

    public PackageFileSystem<Object> getFileSystem()
    {
        return fileSystem;
    }

    public void apply( F<UnixFsObject, Option<UnixFsObject>> f )
    {
        fileSystem = fileSystem.apply( f );
    }

    public void streamTo( LineStreamWriter spec )
    {
        for ( String defineStatement : defineStatements )
        {
            spec.add( "%define " + defineStatement );
        }

        // Resolve issue with OSGI dependencies: http://blog.agilepartner.net/building-rpm-packages-2/         
        spec.add( "%define __jar_repack %{nil}" );
        spec.add( "AutoReqProv: no" );

        UnixUtil.assertField("version", version);
        UnixUtil.assertField("release", release);

        spec.
            add( "Name: " + name ).
            add( "Version: " + version ).
            add( "Release: " + release ).
            add( "Summary: " + UnixUtil.getField( "summary", summary ) ).
            add( "License: " + UnixUtil.getField( "license", license ) ).
            addIfNotEmpty( "Distribution: ", distribution ).
            add( "Group: " + UnixUtil.getField( "group", group ) ).
            addIfNotEmpty( "Packager: ", packager ).
            addAllLines(prefix(provides, "Provides: "));

            spec.addAllLines(prefix(requires, "Requires: "));

            spec. addAllLines(prefix(conflicts, "Conflicts: " ) ).
            addIfNotEmpty( fromNull( buildRoot ).map( compose( curry( concat, "BuildRoot: " ), getAbsolutePath ) ).orSome( "" ) ).
            add( "BuildArch: " + buildArch ).
            add();

        // The %description tag is required even if it is empty.
        spec.
            add( "%description" ).
            addIf( StringUtils.isNotEmpty( description ), description ).
            add();

        spec.
            add( "%files" );

        spec.
            addAllLines( fileSystem.prettify().toList().filter( excludePaths ).map( SpecFile.showUnixFsObject() ) );

        spec.addIf( includePre.isSome() || includePost.isSome() || includePreun.isSome() || includePostun.isSome(), "" );
        if ( includePre.isSome() )
        {
            spec.add( "%pre" );
            spec.add( "%include " + includePre.map( getAbsolutePath ).some() );
        }
        if ( includePost.isSome() )
        {
            spec.add( "%post" );
            spec.add( "%include " + includePost.map( getAbsolutePath ).some() );
        }
        if ( includePreun.isSome() )
        {
            spec.add( "%preun" );
            spec.add( "%include " + includePreun.map( getAbsolutePath ).some() );
        }
        if ( includePostun.isSome() )
        {
            spec.add( "%postun" );
            spec.add( "%include " + includePostun.map( getAbsolutePath ).some() );
        }

        spec.addIf( dump, "%dump" );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private static <A extends UnixFsObject> F<PackageFileSystemObject<Object>, String> showUnixFsObject()
    {
        return new F<PackageFileSystemObject<Object>, String>()
        {
            public String f( PackageFileSystemObject p2 )
            {
                @SuppressWarnings( {"unchecked"} ) UnixFsObject<A> unixFsObject = p2.getUnixFsObject();
                FileAttributes attributes = unixFsObject.attributes;

                String s = "";

                s += formatTags.f( attributes.tags ).orSome( "" );

                if (confFile.contains("/"+unixFsObject.path.toString()) &&
                    (unixFsObject instanceof UnixFsObject.RegularFile || unixFsObject instanceof UnixFsObject.Symlink )) {
                    s += "%config(noreplace) ";
                }

                s += "%attr(" +
                    attributes.mode.map( UnixFileMode.showOcalString ).orSome( "-" ) + "," +
                    attributes.user.orSome( "-" ) + "," +
                    attributes.group.orSome( "-" ) + ") ";

                // UGLY but works TODO fixit
                if (ignorePath(unixFsObject.path.asAbsolutePath("/"))){
                    return "";
                }
                s += unixFsObject.path.asAbsolutePath( "/" );

                if ( !ignorePath(unixFsObject.path.asAbsolutePath("/") +".*") &&
                        (unixFsObject instanceof UnixFsObject.RegularFile || unixFsObject instanceof UnixFsObject.Symlink ))
                {
                    return s;
                }
                else if ( unixFsObject instanceof UnixFsObject.Directory )
                {
                    return "%dir " + s;
                }
                else {
                    return "";
                }
//                throw error( "Unknown type UnixFsObject type: " + unixFsObject );
            }

            private boolean ignorePath(String s) {
                return excludedSysPaths.contains(s);
            }

        };
    }

    private static final F<PackageFileSystemObject<Object>, Boolean> excludePaths = new F<PackageFileSystemObject<Object>, Boolean>()
    {
        public Boolean f( PackageFileSystemObject object )
        {
            if ( !object.getUnixFsObject().path.isBase() )
            {
                return true;
            }

            FileAttributes fileAttributes = object.getUnixFsObject().getFileAttributes();

            if ( fileAttributes.user.isNone() && fileAttributes.group.isNone() && fileAttributes.mode.isNone() )
            {
                return false;
            }

            return false;
        }
    };

    private static final F<List<String>, Option<String>> formatTags = new F<List<String>, Option<String>>()
    {
        public Option<String> f( List<String> tags )
        {
            if ( tags.find( curry( equals, "config" ) ).isSome() )
            {
                return some( "%config " );
            }

            if ( tags.find( curry( equals, "rpm:noreplace" ) ).isSome() )
            {
                return some( "%config(noreplace) " );
            }

            if ( tags.find( curry( equals, "rpm:missingok" ) ).isSome() )
            {
                return some( "%config(missingok) " );
            }

            if ( tags.find( curry( equals, "doc" ) ).isSome() )
            {
                return some( "%doc " );
            }

            if ( tags.find( curry( equals, "rpm:ghost" ) ).isSome() )
            {
                return some( "%ghost " );
            }

            return none();
        }
    };
}
