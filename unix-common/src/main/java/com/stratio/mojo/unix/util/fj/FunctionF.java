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

public class FunctionF
{
    public static <A, B, C> F<F2<A, B, C>, F<A, F<B, C>>> curry_()
    {
        return new F<F2<A, B, C>, F<A, F<B, C>>>()
        {
            public F<A, F<B, C>> f( F2<A, B, C> f )
            {
                return Function.curry( f );
            }
        };
    }

    public static <A, B, C> F2<F2<A, B, C>, A, F<B, C>> curryA()
    {
        return new F2<F2<A, B, C>, A, F<B, C>>()
        {
            public F<B, C> f( F2<A, B, C> f, A a )
            {
                return Function.curry( f, a );
            }
        };
    }

    public static <A, B, C> F2<B, A, C> flip2( final F2<A, B, C> f )
    {
        return new F2<B, A, C>()
        {
            public C f( B b, A a )
            {
                return f.f( a, b );
            }
        };
    }

    public static <A, B, C, D> F<A, D> compose( final F<C, D> h, final F<B, C> g, final F<A, B> f )
    {
        return new F<A, D>()
        {
            public D f( A a )
            {
                return h.f( g.f( f.f( a ) ) );
            }
        };
    }

    public static <A, B, C, D, E> F<A, E> compose( final F<D, E> i, final F<C, D> h, final F<B, C> g, final F<A, B> f )
    {
        return new F<A, E>()
        {
            public E f( A a )
            {
                return i.f( h.f( g.f( f.f( a ) ) ) );
            }
        };
    }
}
