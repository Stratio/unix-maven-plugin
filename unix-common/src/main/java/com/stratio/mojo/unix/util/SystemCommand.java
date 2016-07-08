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
 package com.stratio.mojo.unix.util;

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

import static com.stratio.mojo.unix.util.line.LineStreamWriter.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

/**
 * Executes a system command in a similar fashion to back-tick (`) in sh and ruby.
 * <p>
 * Standard output and error will be send to System.out and System.err by default.
 * </p>
 * <p>
 * Typical usage:
 * <pre>
 * new SystemCommand().
 *     setBaseDir(directory).
 *     setCommand("dpkg").
 *     addArgument("-b").
 *     addArgument("deb").
 *     addArgument(debFileName).
 *     execute();
 * </pre>
 * <p/>
 *
 * TODO: Create FileLineConsumer that puts all the output to a file.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class SystemCommand
{
    public static interface LineConsumer
    {
        void onLine( String line )
            throws IOException;
    }

    public static class StringBufferLineConsumer
        implements LineConsumer
    {
        public final StringBuffer buffer;

        public StringBufferLineConsumer()
        {
            this( 1024 );
        }

        public StringBufferLineConsumer( int size )
        {
            buffer = new StringBuffer( size );
        }

        public void onLine( String line )
            throws IOException
        {
            buffer.
                append( line ).
                append( EOL );
        }

        public String toString()
        {
            return buffer.toString();
        }
    }

    public static class StringListLineConsumer
        implements LineConsumer
    {
        public final List<String> strings;

        public StringListLineConsumer()
        {
            this.strings = new LinkedList<String>();
        }

        public StringListLineConsumer( List<String> strings )
        {
            this.strings = strings;
        }

        public void onLine( String line )
            throws IOException
        {
            strings.add( line );
        }

        public List<String> getStrings()
        {
            return strings;
        }
    }

    public static class ExecutionResult
    {
        public final int exitValue;
        public final String command;

        ExecutionResult( int exitValue, String command )
        {
            this.exitValue = exitValue;
            this.command = command;
        }

        public ExecutionResult assertSuccess()
            throws IOException
        {
            if ( exitValue != 0 )
            {
                throw new IOException( "Command '" + command + "' returned a non-null exit code: " + exitValue );
            }

            return this;
        }

        public ExecutionResult assertSuccess( String exceptionMessage )
            throws IOException
        {
            if ( exitValue != 0 )
            {
                throw new IOException( exceptionMessage );
            }

            return this;
        }
    }

    private static final CommandOutputHandler closingOutputHandler = new ClosingCommandOutputHandler();

    private static final CommandOutputHandler DEFAULT_STDERR_OUTPUT_HANDLER = new OutputStreamCommandOutputHandler( System.err );

    private static final CommandOutputHandler DEFAULT_STDOUT_OUTPUT_HANDLER = new OutputStreamCommandOutputHandler( System.out );

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    private File basedir;

    private String command;

    private List<String> arguments;

    private List<String> environment = new ArrayList<String>(  );

    private boolean debug;

    private CommandOutputHandler stderrHandler;

    private CommandOutputHandler stdoutHandler;

    public SystemCommand()
    {
        arguments = new ArrayList<String>();
    }

    public SystemCommand setCommand( String command )
    {
        this.command = command;
        return this;
    }

    public SystemCommand setBasedir( File basedir )
    {
        this.basedir = basedir;
        return this;
    }

    public SystemCommand addArgument( String argument )
    {
        arguments.add( argument );
        return this;
    }

    public SystemCommand addArguments( List<String> strings )
    {
        arguments.addAll( strings );
        return this;
    }

    public SystemCommand addArgumentIf( boolean b, String argument )
    {
        if ( !b )
        {
            return this;
        }

        arguments.add( argument );
        return this;
    }

    public SystemCommand addArgumentIfNotEmpty( String stringToCheck, String argument )
    {
        return addArgumentIf( StringUtils.isEmpty( stringToCheck ), argument );
    }

    public SystemCommand addArgumentIfNotEmpty( String argument )
    {
        if ( argument == null || argument.trim().length() == 0 )
        {
            return this;
        }

        arguments.add( argument );
        return this;
    }

    public SystemCommand addEnvironment( String variable )
    {
        if ( environment == null )
        {
            environment = new ArrayList<String>();
        }

        environment.add( variable );
        return this;
    }

    public SystemCommand dumpCommandIf( boolean debug )
    {
        this.debug = debug;
        return this;
    }

    // -----------------------------------------------------------------------
    // Stderr
    // -----------------------------------------------------------------------

    public SystemCommand withClosedStderrConsumer()
    {
        return setStderrCommandOutputHandler( closingOutputHandler );
    }

    public SystemCommand withClosedStderrIf( boolean flag )
    {
        return flag ? setStderrCommandOutputHandler( closingOutputHandler ) : this;
    }

    public SystemCommand withClosedStderrUnless( boolean flag )
    {
        return withClosedStderrIf( !flag );
    }

    public SystemCommand withIgnoringStderrConsumer()
    {
        return setStderrCommandOutputHandler( new IgnoringCommandOutputHandler() );
    }

    public SystemCommand withIgnoringStderrIf( boolean flag )
    {
        return flag ? setStderrCommandOutputHandler( new IgnoringCommandOutputHandler() ) : this;
    }

    public SystemCommand withIgnoringStderrUnless( boolean flag )
    {
        return withIgnoringStderrIf( !flag );
    }

    public SystemCommand withStderrConsumer( OutputStream consumer )
    {
        return setStderrCommandOutputHandler( new OutputStreamCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStderrConsumerUnless( OutputStream consumer, boolean flag )
    {
        return !flag ? withStderrConsumer( consumer ) : setStderrCommandOutputHandler( closingOutputHandler );
    }

    public SystemCommand withStderrConsumer( LineConsumer consumer )
    {
        return setStderrCommandOutputHandler( new LineConsumerCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStderrConsumerUnless( LineConsumer consumer, boolean flag )
    {
        return !flag ? withStderrConsumer( consumer ) : setStderrCommandOutputHandler( closingOutputHandler );
    }

    // -----------------------------------------------------------------------
    // Stdout
    // -----------------------------------------------------------------------

    public SystemCommand withClosedStdout()
    {
        return setStdoutCommandOutputHandler( closingOutputHandler );
    }

    public SystemCommand withClosedConsumerIf( boolean flag )
    {
        return flag ? setStdoutCommandOutputHandler( closingOutputHandler ) : this;
    }

    public SystemCommand withClosedStdoutUnless( boolean flag )
    {
        return withClosedConsumerIf( !flag );
    }

    public SystemCommand withIgnoringStdoutConsumer()
    {
        return setStdoutCommandOutputHandler( new IgnoringCommandOutputHandler() );
    }

    public SystemCommand withIgnoringStdoutIf( boolean flag )
    {
        return flag ? setStdoutCommandOutputHandler( new IgnoringCommandOutputHandler() ) : this;
    }

    public SystemCommand withIgnoringStdoutUnless( boolean flag )
    {
        return withIgnoringStdoutIf( !flag );
    }

    public SystemCommand withStdoutConsumer( OutputStream consumer )
    {
        return setStdoutCommandOutputHandler( new OutputStreamCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStdoutConsumerUnless( OutputStream consumer, boolean flag )
    {
        return !flag ? withStdoutConsumer( consumer ) : setStdoutCommandOutputHandler( closingOutputHandler );
    }

    public SystemCommand withStdoutConsumer( LineConsumer consumer )
    {
        return setStdoutCommandOutputHandler( new LineConsumerCommandOutputHandler( consumer ) );
    }

    public SystemCommand withStdoutConsumerUnless( LineConsumer consumer, boolean flag )
    {
        return !flag ? withStdoutConsumer( consumer ) : setStdoutCommandOutputHandler( closingOutputHandler );
    }

    private SystemCommand setStderrCommandOutputHandler( CommandOutputHandler outputHandler )
    {
        if ( stderrHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stderrHandler = outputHandler;
        return this;
    }

    private SystemCommand setStdoutCommandOutputHandler( CommandOutputHandler outputHandler )
    {
        if ( stdoutHandler != null )
        {
            throw new RuntimeException( "There can only be one consumer." );
        }

        this.stdoutHandler = outputHandler;
        return this;
    }

    public ExecutionResult execute()
        throws IOException
    {
        if ( basedir == null )
        {
            basedir = new File( "/" ).getAbsoluteFile();
        }

        if ( command == null )
        {
            throw new IOException( "Missing field 'command'" );
        }

        if ( debug )
        {
            System.err.println( "Executing '" + command + "' with arguments (one argument per line):" );
            for ( String argument : arguments )
            {
                System.err.println( argument );
            }
            System.err.println( "Executing command in directory: " + basedir );

            if ( environment.size() == 0 )
            {
                System.err.println( "Using custom environment with " + environment.size() + " entries." );
                for ( String e : environment )
                {
                    System.err.println( "Environment: " + e );
                }
            }
            else
            {
                TreeMap<String, String> env = new TreeMap<String, String>( System.getenv() );

                System.err.println( "Inheriting the environment with " + env.size() + " entries" );

                for ( Map.Entry<String, String> entry : env.entrySet() )
                {
                    System.out.println( entry.getKey() + " = " + entry.getValue() );
                }
            }
        }

        if ( !basedir.isDirectory() )
        {
            throw new IOException( "Basedir must be a directory: '" + basedir + "'." );
        }

        arguments.add( 0, command );

        String[] env = null;
        if ( environment.size() > 0 )
        {
            env = environment.toArray( new String[environment.size()] );
        }

        return new Execution( command, arguments.toArray( new String[arguments.size()] ), env, basedir, debug,
                              stderrHandler != null ? stderrHandler : DEFAULT_STDERR_OUTPUT_HANDLER,
                              stdoutHandler != null ? stdoutHandler : DEFAULT_STDOUT_OUTPUT_HANDLER ).run();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    /**
     * Utility method to check if a command is available.
     * <p/>
     * Executes "which" and asserts that the file exist. It does not check if the file is executable.
     */
    public static boolean available( String command )
    {
        try
        {
            StringBufferLineConsumer stdout = new StringBufferLineConsumer();

            new SystemCommand().setCommand( "which" ).
//                dumpCommandIf( true ).
                addArgument( command ).
                withStdoutConsumer( stdout ).
                execute().
                assertSuccess();

            String out = StringUtils.trim( stdout.toString() );

            return new File( out ).getCanonicalFile().canRead();
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private static class Execution
    {
        private final String command;
        private final String[] arguments;
        private final String[] environment;
        private final File basedir;
        private final boolean debug;
        private final CommandOutputHandler stderrHandler;
        private final CommandOutputHandler stdoutHandler;

        public Process process;

        private Execution( String command, String[] arguments, String[] environment, File basedir,
                           boolean debug, CommandOutputHandler stderrHandler, CommandOutputHandler stdoutHandler )
        {
            this.command = command;
            this.arguments = arguments;
            this.environment = environment;
            this.basedir = basedir;
            this.debug = debug;
            this.stderrHandler = stderrHandler;
            this.stdoutHandler = stdoutHandler;
        }

        public ExecutionResult run()
            throws IOException
        {
            process = Runtime.getRuntime().exec( arguments, environment, basedir );

            process.getOutputStream().close(); // Close stdin

            stderrHandler.setup( command + ": stderr", process.getErrorStream() );

            stdoutHandler.setup( command + ": stdout", process.getInputStream() );

            try
            {
                process.waitFor();
            }
            catch ( InterruptedException e )
            {
                IOException ex = new IOException( "Interrupted while waiting for process" );
                ex.initCause( e );
                throw ex;
            }

            stderrHandler.join();
            stdoutHandler.join();

            int exitValue = process.exitValue();

            if ( debug )
            {
                System.out.println( "Command completed: " + command + ", exit value: " + exitValue );
            }

            return new ExecutionResult( exitValue, command );
        }
    }

    private static abstract interface CommandOutputHandler
    {
        void setup( String threadName, InputStream inputStream );

        void join();
    }

    private static abstract class ThreadCommandOutputHandler
        implements CommandOutputHandler
    {
        private Thread thread;

        abstract void handle( InputStream inputStream )
            throws IOException;

        public void setup( String threadName, final InputStream inputStream )
        {
            thread = new Thread( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        handle( inputStream );
                    }
                    catch ( IOException e )
                    {
                        // ignore and die
                    }
                    finally
                    {
                        IOUtil.close( inputStream );
                    }
                }
            }, threadName );
            thread.start();
        }

        public void join()
        {
            try
            {
                if ( thread == null )
                {
                    return;
                }

                thread.join();
            }
            catch ( InterruptedException e )
            {
                // ignore
            }
        }
    }

    private static class ClosingCommandOutputHandler
        implements CommandOutputHandler
    {
        public void setup( String threadName, InputStream inputStream )
        {
            IOUtil.close( inputStream );
        }

        public void join()
        {
        }
    }

    private static class IgnoringCommandOutputHandler
        extends ThreadCommandOutputHandler
    {
        private IgnoringCommandOutputHandler()
        {
        }

        public void handle( InputStream inputStream )
            throws IOException
        {
            byte[] buf = new byte[128 * 1024];

            while ( true )
            {
                int read = inputStream.read( buf );

                if ( read == -1 )
                {
                    break;
                }
            }
        }
    }

    private static class OutputStreamCommandOutputHandler
        extends ThreadCommandOutputHandler
    {
        private OutputStream outputStream;

        private OutputStreamCommandOutputHandler( OutputStream outputStream )
        {
            this.outputStream = outputStream;
        }

        public void handle( InputStream inputStream )
            throws IOException
        {
            IOUtil.copy( inputStream, outputStream );
            outputStream.flush();
        }
    }

    private static class LineConsumerCommandOutputHandler
        extends ThreadCommandOutputHandler
    {
        private LineConsumer lineConsumer;

        public LineConsumerCommandOutputHandler( LineConsumer lineConsumer )
        {
            this.lineConsumer = lineConsumer;
        }

        public void handle( InputStream inputStream )
            throws IOException
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            String line = reader.readLine();

            while ( line != null )
            {
                lineConsumer.onLine( line );

                line = reader.readLine();
            }
        }
    }
}
