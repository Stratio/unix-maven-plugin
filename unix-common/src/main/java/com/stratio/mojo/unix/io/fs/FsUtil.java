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
 package com.stratio.mojo.unix.io.fs;

import java.io.*;
import java.util.*;

public class FsUtil
{
    private final static String[] zipFileTypes = { "zip", "jar", "war", "ear", "sar", };

    static {
        Arrays.sort( zipFileTypes );
    }

    /**
     * Remember to close() the Fs-es after use.
     */
    public static Fs<?> resolve( File file )
        throws IOException
    {
        if ( file.isDirectory() )
        {
            return new LocalFs( file );
        }

        int i = file.getName().lastIndexOf( '.' );

        if ( i < 1 )
        {
            throw new IOException( "Unable to resolve file type of file: " + file.getAbsolutePath() );
        }

        String ending = file.getName().substring( i + 1 );

        if ( Arrays.binarySearch( zipFileTypes, ending ) >= 0 )
        {
            return new ZipFsRoot( file );
        }
        else
        {
            throw new IOException( "Unable to resolve file type of file: " + file.getAbsolutePath() );
        }
    }
}
