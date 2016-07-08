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

import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import static com.stratio.mojo.unix.util.Validate.*;

/**
 * A utility class to contain all configuration settings for a packaging mojo.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class PackagingMojoParameters
{
    public final Option<String> name;
    public final Option<String> revision;
    public final Option<String> description;
    public final Option<String> contact;
    public final Option<String> size;
    public final Option<String> contactEmail;
    public final Option<String> architecture;
    public final Defaults defaults;
    public final List<AssemblyOp> assembly;
    public final List<Package> packages;
    public final Option<String> outputFileName;
    public final List<String> excludeDirs;

    public PackagingMojoParameters( String name,
                                    String revision,
                                    String description,
            						String contact,
            						String size,
                                    String contactEmail,
                                    String architecture,
                                    Defaults defaults,
                                    AssemblyOp[] assembly,
                                    Package[] packages,
                                    String outputFileName,
                                    String[] excludeDirs)
    {
        validateNotNull( defaults );
        this.name = fromNull( name );
        this.revision = fromNull( revision );
        this.description = fromNull( description );
        this.contact = fromNull( contact );
        this.size= fromNull( size );
        this.contactEmail = fromNull( contactEmail );
        this.architecture = fromNull( architecture );
        this.defaults = defaults;
        this.assembly = assembly == null ? List.<AssemblyOp>nil() : list( assembly );
        this.packages = packages == null ? List.<Package>nil() : list( packages );
        this.outputFileName= fromNull( outputFileName );
        this.excludeDirs = excludeDirs == null ? List.<String>nil() : list( excludeDirs );
    }
}
