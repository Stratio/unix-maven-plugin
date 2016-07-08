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
import static fj.data.Option.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@SuppressWarnings( {"UnusedDeclaration"} )
public class DebSpecificSettings
{
    public Option<String> priority = none();

    public Option<String> section = none();

    public Option<String> depends = none();

    public Option<String> recommends = none();

    public Option<String> suggests = none();

    public Option<String> preDepends = none();

    public Option<String> provides = none();

    public Option<String> replaces = none();

    public boolean useFakeroot = true;

    public Option<String> dpkgDeb = none();

    public void setPriority( String priority )
    {
        this.priority = fromNull( priority );
    }

    public void setSection( String section )
    {
        this.section = fromNull( section );
    }

    public void setDepends( String depends )
    {
        this.depends = fromNull( depends );
    }

    public void setRecommends( String recommends )
    {
        this.recommends = fromNull( recommends );
    }

    public void setSuggests( String suggests )
    {
        this.suggests = fromNull( suggests );
    }

    public void setPreDepends( String preDepends )
    {
        this.preDepends = fromNull( preDepends );
    }

    public void setProvides( String provides )
    {
        this.provides = fromNull( provides );
    }

    public void setReplaces( String replaces )
    {
        this.replaces = fromNull( replaces );
    }

    public void setUseFakeroot( boolean useFakeroot )
    {
        this.useFakeroot = useFakeroot;
    }

    public void setDpkgDeb( String dpkgDeb )
    {
        this.dpkgDeb = fromNull( dpkgDeb );
    }

}
