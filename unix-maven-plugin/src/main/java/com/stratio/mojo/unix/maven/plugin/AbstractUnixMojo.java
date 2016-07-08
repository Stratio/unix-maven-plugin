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
 package com.stratio.mojo.unix.maven.plugin;

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

import org.apache.maven.execution.*;
import org.apache.maven.plugin.*;
import org.apache.maven.project.*;

import java.util.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractUnixMojo
    extends AbstractMojo
{
    /**
     * The assembly operations shared between all packages.
     *
     * @parameter
     */
    protected AssemblyOp[] assembly;

    /**
     * Default values for files and directories.
     *
     * @parameter
     */
    protected Defaults defaults = new Defaults();

    /**
     * Optional parameter to specify the name of the package. If not set it will use the artifact id of the Maven
     * project.
     *
     * @parameter
     */
    protected String name;

    /**
     * Optional parameter to specify the revision of the package. If not set it will be parsed from the
     * version information in the Maven project.
     *
     * @parameter
     */
    protected String revision;

    /**
     * One-line description of the package.
     *
     * @parameter
     */
    protected String description;

    /**
     * The name of the contact person for the package.
     *
     * @parameter
     */
    protected String contact;

    /**
     * The size of the package.
     *
     * @parameter
     */
    protected String size;

    /**
     * The email address of the contact person for the package.
     *
     * @parameter
     */
    protected String contactEmail;

    // TODO: Is this really common? Or just common enough?
    /**
     * The architecture the package is valid for.
     *
     * @parameter
     */
    protected String architecture;

    /**
     * Comma separated package requires
     */
    protected String requires;

    /**
     * Enabled extra debugging informasjon.
     *
     * @parameter property="maven.unix.debug" default="false"
     */
    protected boolean debug;

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    /**
     * @parameter default-value="${project}"
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter default-value="${session}"
     * @readonly
     */
    protected ThreadLocal<MavenSession> session = new ThreadLocal<MavenSession>();

    /**
     * @component role="com.stratio.mojo.unix.core.UnixPlatform"
     */
    protected Map platforms;

    /**
     * @component
     */
    protected MavenProjectHelper mavenProjectHelper;

    /**
     * The size of the package.
     *
     * @parameter
     */
    protected String outputFileName;


    /**
     * Exclude this directories from RPM SPECT
     * @parameter
     */
    protected String[] excludeDirectoryAttributes;

}
