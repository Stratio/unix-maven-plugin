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
 package com.stratio.mojo.unix.io;

import fj.*;
import junit.framework.*;
import org.codehaus.plexus.util.*;

import java.io.*;

public class LineEndingTest
    extends TestCase
{
    public void testDetect()
        throws Exception
    {
        assertResult( new byte[]{ 'a', 'b', '\n'}, LineEnding.unix );
        assertResult( new byte[]{ 'a', 'b', '\n', 'c', 'd'}, LineEnding.unix );
        assertResult( new byte[]{ 'a', 'b', '\r', '\n', 'c', 'd'}, LineEnding.windows );
    }

    private void assertResult( byte[] bytes, LineEnding lineEnding )
        throws IOException
    {
        P2<InputStream, LineEnding> x = LineEnding.detect( new ByteArrayInputStream( bytes ) );

        assertEquals( lineEnding, x._2() );
        byte[] actualBytes = IOUtil.toByteArray( x._1() );
        assertEquals( bytes.length, actualBytes.length );
        for ( int i = 0; i < actualBytes.length; i++ )
        {
            assertEquals( "byte #" + i, bytes[i], actualBytes[i] );
        }
    }
}
