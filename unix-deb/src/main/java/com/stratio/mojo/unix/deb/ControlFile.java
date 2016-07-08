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
 package com.stratio.mojo.unix.deb;

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

import fj.*;
import fj.Function;
import static fj.Function.*;
import fj.data.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import static fj.Ord.*;
import com.stratio.mojo.unix.java.*;
import static com.stratio.mojo.unix.java.StringF.*;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ControlFile
{
    /**
     * Refers to the <code>Package</code> field, but "package" is a reserved keyword in Java.
     */
    public final String packageName;

    public final Option<String> version;

    public final Option<String> description;

    public final Option<String> maintainer;

    public final Option<String> installedSize;

    public final Option<String> architecture;

    public final Option<String> priority;

    public final Option<String> section;

    public final List<String> depends;

    public final List<String> recommends;

    public final List<String> suggests;

    public final List<String> preDepends;

    public final List<String> provides;

    public final List<String> replaces;

    public final List<String> conflicts;

    public final TreeMap<String, String> extraFields;

    public ControlFile( String packageName )
    {
        this( packageName, Option.<String>none(), Option.<String>none(), Option.<String>none(),
                Option.<String>none(), Option.<String>none(),
            Option.<String>none(), Option.<String>none(), List.<String>nil(), List.<String>nil(), List.<String>nil(),
            List.<String>nil(), List.<String>nil(), List.<String>nil(), List.<String>nil(),
            TreeMap.<String, String>empty( stringOrd ) );
    }

    public ControlFile( String packageName, Option<String> version, Option<String> description,
                        Option<String> maintainer,
                        Option<String> installedSize,
                        Option<String> architecture, Option<String> priority,
                        Option<String> section, List<String> depends, List<String> recommends, List<String> suggests,
                        List<String> preDepends, List<String> provides, List<String> replaces, List<String> conflicts,
                        TreeMap<String, String> extraFields )
    {
        this.packageName = packageName;
        this.version = version;
        this.description = description;
        this.maintainer = maintainer;
        this.installedSize= installedSize;
        this.architecture = architecture;
        this.priority = priority;
        this.section = section;
        this.depends = depends;
        this.recommends = recommends;
        this.suggests = suggests;
        this.preDepends = preDepends;
        this.provides = provides;
        this.replaces = replaces;
        this.conflicts = conflicts;
        this.extraFields = extraFields;
    }

    public ControlFile version( Option<String> version )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize,architecture, priority,
                section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile description( Option<String> description )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize, architecture, priority,
                section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile maintainer( Option<String> maintainer )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize, architecture, priority,
                section, depends, recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile installedSize(Option<String> size) {
        return new ControlFile( packageName, version, description, maintainer, size, architecture, priority,
                section, depends, recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile architecture( Option<String> architecture )
    {

        return new ControlFile( packageName, version, description, maintainer,installedSize, architecture, priority,
                section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile priority( Option<String> priority )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize,architecture, priority,
                section,
                depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile section( Option<String> section )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize, architecture, priority,
                section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile depends( List<String> depends )
    {
        return new ControlFile( packageName, version, description, maintainer,installedSize, architecture, priority,
                section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile recommends( List<String> recommends )
    {
        return new ControlFile( packageName, version, description, maintainer,installedSize, architecture, priority,
                section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile suggests( List<String> suggests )
    {
        return new ControlFile( packageName, version, description, maintainer,installedSize,  architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile preDepends( List<String> preDepends )
    {
        return new ControlFile( packageName, version, description, maintainer,installedSize,  architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile provides( List<String> provides )
    {
        return new ControlFile( packageName, version, description, maintainer,installedSize,  architecture, priority, section, depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile replaces( List<String> replaces )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize,  architecture, priority, section,
                depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile conflicts( List<String> conflicts )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize,  architecture, priority, section,
                depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public ControlFile extraFields( TreeMap<String, String> extraFields )
    {
        return new ControlFile( packageName, version, description, maintainer, installedSize,  architecture, priority, section,
                depends,
                                recommends, suggests, preDepends, provides, replaces, conflicts, extraFields );
    }

    public List<String> toList()
    {
        List<Option<String>> optionList = single( some( "Package: " + packageName ) ).
            cons( section.map( curry( concat, "Section: " ) ) ).
            cons(priority.map(curry(concat, "Priority: "))).
            cons(maintainer.map(curry(concat, "Maintainer: "))).
            cons(installedSize.map(curry(concat, "Installed-Size: "))).
            cons( version.map( curry( concat, "Version: " ) ) ).
            cons( architecture.map( curry( concat, "Architecture: " ) ) ).
            cons(description.map(curry(concat, "Description: ")));

        F<String, F<List<String>, List<String>>> f = listToHeader.f( 80 );

        List<String> lines = f.f( "Depends" ).f( this.depends ).
            append( f.f( "Depends" ).f( this.recommends ) ).
            append( f.f( "Recommends" ).f( this.suggests ) ).
            append( f.f( "Pre-Depends" ).f( this.preDepends ) ).
            append( f.f( "Provides" ).f( this.provides ) ).
            append( f.f( "Replaces" ).f( this.replaces ) ).
            append( f.f( "Conflicts" ).f( this.conflicts ) );

        return somes( optionList ).reverse().append( lines.filter( StringF.isNotEmpty ) );
    }

    public static F<Integer, F<String, F<List<String>, List<String>>>> listToHeader = curry( new F3<Integer, String, List<String>, List<String>>()
    {
        public List<String> f( Integer lineLength, String headerName, List<String> values )
        {
            return listToHeader( lineLength, headerName, values );
        }
    } );

    public static List<String> listToHeader( int lineLength, String headerName, List<String> values )
    {
        if ( values.isEmpty() )
        {
            return nil();
        }

        List<String> strings = nil();

        String line = headerName + ": " + values.head();

        values = values.tail();

        for ( String s : values )
        {
            if ( line.length() + s.length() > lineLength )
            {
                strings = strings.cons( line + ", " );
                line = " " + s;
            }
            else
            {
                line += ", " + s;
            }
        }

        strings = strings.cons( line );
        return strings.reverse();
    }

    F2<String, String, String> folder = new F2<String, String, String>()
    {
        public String f( String a, String b )
        {
            return a + ", " + b;
        }
    };

    public static final F<String, List<String>> toList = compose(
        List.<String, String>map_().f( StringF.trim ),
        Function.flip( StringF.split ).f( "," ) );

    public static ControlFile controlFileFromList( List<P2<String, String>> values )
    {
        TreeMap<String, String> map =
            values.foldLeft( new F2<TreeMap<String, String>, P2<String, String>, TreeMap<String, String>>()
            {
                public TreeMap<String, String> f( TreeMap<String, String> map, P2<String, String> p )
                {
                    return map.set( p._1(), p._2() );
                }
            }, TreeMap.<String, String>empty( stringOrd ) );

        Option<String> packageName = map.get( "Package" );

        if ( packageName.isNone() )
        {
            throw Bottom.error( "Could not find required field 'Package'." );
        }

        ControlFile controlFile = new ControlFile( packageName.some() );

        controlFile = controlFile.version( map.get( "Version" ) );
        map = map.delete( "Version" );

        controlFile = controlFile.description( map.get( "Description" ) );
        map = map.delete( "Description" );

        controlFile = controlFile.maintainer( map.get( "Maintainer" ) );
        map = map.delete( "Maintainer" );

        controlFile = controlFile.installedSize( map.get( "Installed-Size" ) );
        map = map.delete( "Installed-Size" );

        controlFile = controlFile.architecture( map.get( "Architecture" ) );
        map = map.delete( "Architecture" );

        controlFile = controlFile.priority( map.get( "Priority" ) );
        map = map.delete( "Priority" );

        controlFile = controlFile.section( map.get( "Section" ) );
        map = map.delete( "Section" );

        List<String> emptyList = List.nil();
        controlFile = controlFile.depends( map.get( "Depends" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Depends" );

        controlFile = controlFile.recommends( map.get( "Recommends" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Recommends" );

        controlFile = controlFile.suggests( map.get( "Suggests" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Suggests" );

        controlFile = controlFile.preDepends( map.get( "PreDepends" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "PreDepends" );

        controlFile = controlFile.provides( map.get( "Provides" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Provides" );

        controlFile = controlFile.replaces( map.get( "replaces" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Replaces" );

        controlFile = controlFile.conflicts( map.get( "conflicts" ).map( toList ).orSome( emptyList ) );
        map = map.delete( "Conflicts" );

        controlFile.extraFields( map );

        return controlFile;
    }

}
