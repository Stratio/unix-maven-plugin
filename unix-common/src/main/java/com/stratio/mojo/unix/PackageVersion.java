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
 * Represents a package version as defined in Maven. Each packaging format
 * might need later conversion to platform-specific versioning.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public final class PackageVersion
{
    public final String version;

    public final String timestamp;

    public final boolean snapshot;

    public final Option<String> revision;

    private PackageVersion( String version, String timestamp, boolean snapshot, Option<String> revision )
    {
        validateNotNull( version, timestamp, snapshot, revision );
        this.version = version;
        this.timestamp = timestamp;
        this.snapshot = snapshot;
        this.revision = revision;
    }

    private static String version( boolean snapshot, String version )
    {
        if ( !snapshot )
        {
            return version;
        }

        // Try to extract the maintainerRevision from the pom
        if ( !version.endsWith( "-SNAPSHOT" ) )
        {
            throw new RuntimeException(
                "Expected the version string to end with '-SNAPSHOT' when the version is a snapshot version." );
        }

        return version.substring( 0, version.length() - 9 );
    }

    public static PackageVersion packageVersion( String mavenVersion, String timestamp, boolean snapshot,
                                                 Option<String> configuredRevision )
    {
        validateNotNull( mavenVersion, timestamp, configuredRevision );

        String version = version( snapshot, mavenVersion );

        // If the configured revision is set, there is not need to transform the version
        if ( configuredRevision.isSome() )
        {
            return new PackageVersion( version( snapshot, mavenVersion ), timestamp, snapshot, configuredRevision );
        }

        if ( snapshot )
        {
            if ( timestamp == null )
            {
                throw new RuntimeException( "The timestamp can't be null when creating a snapshot version" );
            }
        }

        return new PackageVersion( version( snapshot, mavenVersion ), timestamp, snapshot, Option.<String>none() );
    }

    public String getMavenVersion()
    {
        String v = revision.isSome() ? version + "-" + revision.some() : version;

        if ( snapshot )
        {
            return v + "-SNAPSHOT";
        }

        return v;
    }

    public String toString()
    {
        return "PackageVersion{" + "version='" + version + '\'' + ", timestamp='" + timestamp + '\'' + ", snapshot=" +
            snapshot + ", revision=" + revision + '}';
    }
}
