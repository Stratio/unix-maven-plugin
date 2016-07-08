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
 package com.stratio.mojo.unix;

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
import static fj.data.Option.*;
import static com.stratio.mojo.unix.util.Validate.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PackageParameters
{
    public final String groupId;

    public final String artifactId;

    public final PackageVersion version;

    /**
     * A quasi-unique id. Typically artifact id (+ classifier).
     */
    public final String id;

    /**
     * A single-line description of the package.
     */
    public final String name;

    public final Option<String> classifier;

    /**
     * A multi-line description of the package.
     */
    public final Option<String> description;

    public final Option<String> contact;

    public final Option<String> size;

    public final Option<String> outputFileName;

    public final Option<String> contactEmail;

    public final Option<String> license;

    public final Option<String> architecture;

    public final FileAttributes defaultFileAttributes;

    public final FileAttributes defaultDirectoryAttributes;

    public final List<String> excludeDirs;

    public PackageParameters( String groupId, String artifactId, PackageVersion version, String id, String name,
                              FileAttributes defaultFileAttributes, FileAttributes defaultDirectoryAttributes,
                              Option<String> classifier, Option<String> description, Option<String> contact,
                              Option<String> size,
                              Option<String> contactEmail, Option<String> license, Option<String> architecture,
                              Option<String> outputFileName, List<String> excludeDirs)
    {
        validateNotNull( groupId, artifactId, version, id, name, defaultFileAttributes, defaultDirectoryAttributes,
            classifier, description, contact,size, contactEmail, license, architecture );

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.id = id;
        this.name = name;
        this.classifier = classifier;
        this.description = description;
        this.contact = contact;
        this.size= size;
        this.contactEmail = contactEmail;
        this.license = license;
        this.architecture = architecture;
        this.defaultFileAttributes = defaultFileAttributes;
        this.defaultDirectoryAttributes = defaultDirectoryAttributes;
        this.outputFileName = outputFileName;
        this.excludeDirs = excludeDirs;
    }

    // -----------------------------------------------------------------------
    // Static
    // -----------------------------------------------------------------------

    public static PackageParameters packageParameters( String groupId, String artifactId, PackageVersion version,
                                                       String id, String name, Option<String> classifier,
                                                       FileAttributes defaultFileAttributes,
                                                       FileAttributes defaultDirectoryAttributes )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, Option.<String>none(), Option.<String>none(),
                                      Option.<String>none(),
                                      Option.<String>none(), Option.<String>none(), Option.<String>none(), Option.<String>none(), null);
    }

    public PackageParameters name( String name )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName, excludeDirs);
    }

    public PackageParameters description( String description )
    {
        return description( fromNull( description ) );
    }

    public PackageParameters description( Option<String> description )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName, excludeDirs);
    }

    public PackageParameters contact( String contact )
    {
        return contact(fromNull(contact));
    }

    public PackageParameters contact( Option<String> contact )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName, excludeDirs);
    }

    public PackageParameters size( String size )
    {
        return size(fromNull(size));
    }

    public PackageParameters size( Option<String> size )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName, excludeDirs);
    }

    public PackageParameters contactEmail( String contactEmail )
    {
        return contactEmail(fromNull(contactEmail));
    }

    public PackageParameters contactEmail( Option<String> contactEmail )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName, excludeDirs );
    }

    public PackageParameters license( String license )
    {
        return license(fromNull(license));
    }

    public PackageParameters license( Option<String> license )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName, excludeDirs);
    }

    public PackageParameters architecture( String architecture )
    {
        return architecture(fromNull(architecture));
    }

    public PackageParameters architecture( Option<String> architecture )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                                      defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                                      license, architecture, outputFileName , excludeDirs);
    }

    public PackageParameters excludeDirs( List<String> excludeDirs )
    {
        return new PackageParameters( groupId, artifactId, version, id, name, defaultFileAttributes,
                defaultDirectoryAttributes, classifier, description, contact,size, contactEmail,
                license, architecture, outputFileName , excludeDirs);
    }
}
