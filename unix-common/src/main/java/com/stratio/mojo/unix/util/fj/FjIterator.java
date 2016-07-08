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
 package com.stratio.mojo.unix.util.fj;

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
import fj.data.*;
import static fj.data.Option.*;

import java.util.*;

public class FjIterator<A>
    implements java.util.Iterator<A>
{
    private final java.util.Iterator<A> iterator;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static <A> FjIterator<A> iterator( java.util.Iterator<A> iterator )
    {
        return new FjIterator<A>( iterator );
    }

    public static <A> FjIterator<A> iterator( Iterable<A> iterable )
    {
        return new FjIterator<A>( iterable.iterator() );
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private FjIterator( java.util.Iterator<A> iterator )
    {
        this.iterator = iterator;
    }

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public A next()
    {
        return iterator.next();
    }

    public void remove()
    {
        iterator.remove();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public <B> FjIterator<B> map( F<A, B> f )
    {
        return new FjIterator<B>( new MappingIterator<A, B>( this, f ) );
    }

    public FjIterator<A> filter( F<A, Boolean> f )
    {
        return new FjIterator<A>( new FilteringIterator<A>( this, f ) );
    }

    public Iterable<A> toIterable()
    {
        return new Iterable<A>()
        {
            public Iterator<A> iterator()
            {
                return FjIterator.this;
            }
        };
    }

    // -----------------------------------------------------------------------
    // Private Inner Classes
    // -----------------------------------------------------------------------

    private static class MappingIterator<A, B>
        implements Iterator<B>
    {

        private final Iterator<A> iterator;

        private final F<A, B> f;

        private MappingIterator( Iterator<A> iterator, F<A, B> f )
        {
            this.iterator = iterator;
            this.f = f;
        }

        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        public B next()
        {
            return f.f( iterator.next() );
        }

        public void remove()
        {
            iterator.remove();
        }
    }

    private static class FilteringIterator<A>
        implements Iterator<A>
    {
        private final Iterator<A> iterator;

        private final F<A, Boolean> f;

        private Option<A> next;

        private FilteringIterator( Iterator<A> iterator, F<A, Boolean> f )
        {
            this.iterator = iterator;
            this.f = f;
            next = none();
        }

        public boolean hasNext()
        {
            if ( next.isSome() )
            {
                return true;
            }

            while ( iterator.hasNext() )
            {
                A a = iterator.next();

                if ( f.f( a ) )
                {
                    next = some( a );
                    break;
                }
            }

            return next.isSome();
        }

        public A next()
        {
            if ( hasNext() )
            {
                A n = next.some();
                next = none();
                return n;
            }

            throw new NoSuchElementException();
        }

        public void remove()
        {
            throw new RuntimeException( "Not supported" );
        }
    }
}
