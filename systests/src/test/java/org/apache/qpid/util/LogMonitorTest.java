/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.util;

import junit.framework.TestCase;
import org.apache.qpid.test.utils.QpidTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LogMonitorTest extends QpidTestCase
{

    private LogMonitor _monitor;

    @Override
    public void setUp() throws Exception
    {
        _monitor = new LogMonitor();
        _monitor.getMonitoredFile().deleteOnExit(); // Make sure we clean up
    }
    
    /**
     * Test that a new file is created when attempting to set up a monitor with
     * the default constructor.
     */
    public void testMonitor()
    {
        //Validate that the monitor is now running on a new file
        assertTrue("New file does not have correct name:" + _monitor.
                getMonitoredFile().getName(),
                _monitor.getMonitoredFile().getName().contains("LogMonitor"));
    }

    /**
     * Test that creation of a monitor on an existing file is possible
     *
     * This also tests taht getMonitoredFile works
     *
     * @throws IOException if there is a problem creating the temporary file
     */
    public void testMonitorNormalFile() throws IOException
    {
        File testFile = File.createTempFile("testMonitorFile", ".log");
        testFile.deleteOnExit();

        //Ensure that we can create a monitor on a file
        try
        {
            _monitor = new LogMonitor(testFile);
            assertEquals(testFile, _monitor.getMonitoredFile());
        }
        catch (IOException ioe)
        {
            fail("IOE thrown:" + ioe);
        }

    }

    /**
     * Test that a new file is created when attempting to set up a monitor on
     * a null input value.
     */
    public void testMonitorNullFile()
    {
        // Validate that a NPE is thrown with null input
        try
        {
            LogMonitor montior = new LogMonitor(null);
            //Validte that the monitor is now running on a new file
            assertTrue("New file does not have correct name:" + montior.
                    getMonitoredFile().getName(),
                       montior.getMonitoredFile().getName().contains("LogMonitor"));
        }
        catch (IOException ioe)
        {
            fail("IOE thrown:" + ioe);
        }
    }

    /**
     * Test that a new file is created when attempting to set up a monitor on
     * a non existing file.
     *
     * @throws IOException if there is a problem setting up the nonexistent file
     */
    public void testMonitorNonExistentFile() throws IOException
    {
        //Validate that we get a FileNotFound if the file does not exist

        File nonexist = File.createTempFile("nonexist", ".out");

        assertTrue("Unable to delete file for our test", nonexist.delete());

        assertFalse("Unable to test as our test file exists.", nonexist.exists());

        try
        {
            LogMonitor montior = new LogMonitor(nonexist);
            //Validte that the monitor is now running on a new file
            assertTrue("New file does not have correct name:" + montior.
                    getMonitoredFile().getName(),
                       montior.getMonitoredFile().getName().contains("LogMonitor"));
        }
        catch (IOException ioe)
        {
            fail("IOE thrown:" + ioe);
        }
    }

    /**
     * Test that Log file matches logged messages.
     *
     * @throws java.io.IOException if there is a problem creating LogMontior
     */
    public void testFindMatches_Match() throws IOException
    {

        String message = getName() + ": Test Message";

        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(message);

        validateLogContainsMessage(_monitor, message);
    }

    /**
     * Test that Log file does not match a message not logged.
     *
     * @throws java.io.IOException if there is a problem creating LogMontior
     */
    public void testFindMatches_NoMatch() throws IOException
    {
        String message = getName() + ": Test Message";

        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(message);

        String notLogged = "This text was not logged";

        validateLogDoesNotContainMessage(_monitor, notLogged);
    }

    public void testWaitForMessage_Timeout() throws IOException
    {
        String message = getName() + ": Test Message";

        long TIME_OUT = 2000;

        logMessageWithDelay(message, TIME_OUT);

        // Verify that we can time out waiting for a message
        assertFalse("Message was logged ",
                    _monitor.waitForMessage(message, TIME_OUT / 2));

        // Verify that the message did eventually get logged.
        assertTrue("Message was never logged.",
                    _monitor.waitForMessage(message, TIME_OUT));
    }

    public void testDiscardPoint() throws IOException
    {
        String firstMessage = getName() + ": Test Message1";
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(firstMessage);

        validateLogContainsMessage(_monitor, firstMessage);

        _monitor.markDiscardPoint();

        validateLogDoesNotContainMessage(_monitor, firstMessage);

        String secondMessage = getName() + ": Test Message2";
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(secondMessage);
        validateLogContainsMessage(_monitor, secondMessage);
    }

    public void testRead() throws IOException
    {
        String message = getName() + ": Test Message";

        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(message);

        String fileContents = _monitor.readFile();

        assertTrue("Logged message not found when reading file.",
                   fileContents.contains(message));
    }

    /****************** Helpers ******************/

    /**
     * Validate that the LogMonitor does not match the given string in the log
     *
     * @param log     The LogMonitor to check
     * @param message The message to check for
     *
     * @throws IOException if a problems occurs
     */
    protected void validateLogDoesNotContainMessage(LogMonitor log, String message)
            throws IOException
    {
        List<String> results = log.findMatches(message);

        assertNotNull("Null results returned.", results);

        assertEquals("Incorrect result set size", 0, results.size());
    }

    /**                                                                                                                                                 
     * Validate that the LogMonitor can match the given string in the log
     *
     * @param log     The LogMonitor to check
     * @param message The message to check for
     *
     * @throws IOException if a problems occurs
     */
    protected void validateLogContainsMessage(LogMonitor log, String message)
            throws IOException
    {
        List<String> results = log.findMatches(message);

        assertNotNull("Null results returned.", results);

        assertEquals("Incorrect result set size", 1, results.size());

        assertTrue("Logged Message'" + message + "' not present in results:"
                   + results.get(0), results.get(0).contains(message));
    }

    /**
     * Create a new thread to log the given message after the set delay
     *
     * @param message the messasge to log
     * @param delay   the delay (ms) to wait before logging
     */
    private void logMessageWithDelay(final String message, final long delay)
    {
        new Thread(new Runnable()
        {

            public void run()
            {
                try
                {
                    Thread.sleep(delay);
                }
                catch (InterruptedException e)
                {
                    //ignore
                }

                LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).warn(message);
            }
        }).start();
    }

}
