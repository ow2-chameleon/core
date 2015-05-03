/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.core.utils.jul;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class JulLogManagerTest {

    private static LoggerContext loggerContext;

    @BeforeClass
    public static void init() {
        System.setProperty("java.util.logging.manager", JulLogManager.class.getName());

        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        if ( factory instanceof LoggerContext ) {
            loggerContext = (LoggerContext)factory;
        } else {
            loggerContext = new LoggerContext();
            loggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        }
    }

    @AfterClass
    public static void cleanup() {
        System.clearProperty("java.util.logging.manager");
    }

    @Test
    public void isLogManagerConfigured() {
        java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
        assertThat(logManager).isInstanceOf(JulLogManager.class);
    }

    @Test
    public void loggerAreWrapped() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(getClass().getName());
        assertThat(logger).isInstanceOf(JulWrapper.class);
    }

    @Test
    public void JDKToSlf4jLevels() {
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        Logger logger = loggerContext.getLogger(getClass().getName());

        julLogger.setLevel(java.util.logging.Level.SEVERE);
        assertEquals(Level.ERROR, logger.getLevel());

        julLogger.setLevel(java.util.logging.Level.WARNING);
        assertEquals(Level.WARN, logger.getLevel());

        julLogger.setLevel(java.util.logging.Level.INFO);
        assertEquals(Level.INFO, logger.getLevel());

        julLogger.setLevel(java.util.logging.Level.CONFIG);
        assertEquals(Level.DEBUG, logger.getLevel());

        julLogger.setLevel(java.util.logging.Level.FINE);
        assertEquals(Level.DEBUG, logger.getLevel());

        julLogger.setLevel(java.util.logging.Level.FINER);
        assertEquals(Level.TRACE, logger.getLevel());

        julLogger.setLevel(java.util.logging.Level.FINEST);
        assertEquals(Level.TRACE, logger.getLevel());
    }

    @Test
    public void slf4jToJDKLevels() {

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        Logger logger = loggerContext.getLogger(getClass().getName());

        logger.setLevel(Level.ERROR);
        assertEquals(java.util.logging.Level.SEVERE, julLogger.getLevel());

        logger.setLevel(Level.WARN);
        assertEquals(java.util.logging.Level.WARNING, julLogger.getLevel());

        logger.setLevel(Level.INFO);
        assertEquals(java.util.logging.Level.INFO, julLogger.getLevel());

        logger.setLevel(Level.DEBUG);
        assertEquals(java.util.logging.Level.FINE, julLogger.getLevel());

        logger.setLevel(Level.TRACE);
        assertEquals(java.util.logging.Level.FINEST, julLogger.getLevel());
    }

    static class TestFilter extends Filter<ILoggingEvent> {

        String msg;
        String thrownMsg;

        @Override
        public FilterReply decide(ILoggingEvent event) {
            msg = event.getFormattedMessage();
            thrownMsg = event.getThrowableProxy() != null ? event.getThrowableProxy().getMessage() : null;
            return FilterReply.NEUTRAL;
        }
    }

    @Test
    public void testlog() throws UnsupportedEncodingException {

        LogConfigurator conf = LogConfigurator.configure();
        TestFilter filter = new TestFilter();
        conf.addFilter(filter);

        final String testMsg = "Hello Chameleon";

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        julLogger.severe(testMsg);
        assertEquals(filter.msg, testMsg);
    }

    @Test
    public void testNoParam() throws UnsupportedEncodingException {

        LogConfigurator conf = LogConfigurator.configure();
        TestFilter filter = new TestFilter();
        conf.addFilter(filter);

        final String testMsg = "Hello Chameleon";

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        julLogger.log(java.util.logging.Level.WARNING, testMsg);

        assertEquals(filter.msg, testMsg);
    }

    @Test
    public void testParam1() throws UnsupportedEncodingException {

        LogConfigurator conf = LogConfigurator.configure();
        TestFilter filter = new TestFilter();
        conf.addFilter(filter);

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        julLogger.log(java.util.logging.Level.WARNING, "test {0}", "1234");

        assertEquals(filter.msg, "test 1234");
    }

    @Test
    public void testFmtMsg() throws UnsupportedEncodingException {

        LogConfigurator conf = LogConfigurator.configure();
        TestFilter filter = new TestFilter();
        conf.addFilter(filter);

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        julLogger.log(java.util.logging.Level.WARNING, "test {1}, {0}", new Object[] { "1", "2" });

        assertEquals(filter.msg, "test 2, 1");
    }

    @Test
    public void testThrown() throws UnsupportedEncodingException {

        LogConfigurator conf = LogConfigurator.configure();
        TestFilter filter = new TestFilter();
        conf.addFilter(filter);

        final String testMsg = "Hello Chameleon";

        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(getClass().getName());
        RuntimeException e = new RuntimeException(testMsg);
        julLogger.log(java.util.logging.Level.WARNING, testMsg, e);

        assertEquals(filter.thrownMsg, testMsg);
    }

}