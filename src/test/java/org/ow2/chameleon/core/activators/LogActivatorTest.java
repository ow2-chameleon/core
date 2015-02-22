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
package org.ow2.chameleon.core.activators;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.ow2.chameleon.core.Chameleon;
import org.ow2.chameleon.core.ChameleonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.fail;

/**
 * Checks the Log Activator.
 */
public class LogActivatorTest {

    /**
     * Reference Id.
     */
    private static final long REFERENCE_ID = 12l;
    /**
     * A log activator.
     */
    private LogActivator logger;

    /**
     * Prepares the environment.
     * Create a log activator.
     *
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() {
        ChameleonConfiguration configuration = new ChameleonConfiguration(new File(""));
        Logger log;
        log = Chameleon.initializeLoggingSystem(configuration);
        logger = new LogActivator(log);
    }

    /**
     * Cleans the environment.
     * Delete log files.
     *
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteQuietly(new File("logs"));
    }

    /**
     * Tries to log a couple of message using different level.
     */
    @Test
    public void testSLF4J() {
        Logger log = LoggerFactory.getLogger("foo");
        log.debug("debug");
        log.info("info");
        log.warn("warning");
        log.error("error");
    }

    /**
     * Checks the propagation of OSGi log entries.
     */
    @Test
    public void testLogging() {
        LogEntry le1 = getLogEntry(null, "1", LogService.LOG_INFO, null, null,
                null);
        logger.logged(le1);

        LogEntry le2 = getLogEntry(null, "2", LogService.LOG_DEBUG, null, null,
                null);
        logger.logged(le2);

        LogEntry le3 = getLogEntry("bundle-3", "3", LogService.LOG_WARNING,
                null, null, null);
        logger.logged(le3);

        LogEntry le4 = getLogEntry("bundle-4", "4", LogService.LOG_ERROR,
                "Bad news", null, null);
        logger.logged(le4);

        LogEntry le5 = getLogEntry("bundle-5", "5", LogService.LOG_INFO, null,
                REFERENCE_ID, null);
        logger.logged(le5);

        LogEntry le6 = getLogEntry("bundle-6", "6", LogService.LOG_INFO, null,
                REFERENCE_ID, "pid");
        logger.logged(le6);

        LogEntry le7 = getLogEntry("bundle-7", "7", LogService.LOG_INFO, "foo",
                REFERENCE_ID, "pid");
        logger.logged(le7);

        LogEntry le8 = getLogEntry(null, "8", LogService.LOG_DEBUG,
                "debug exception", null, null);
        logger.logged(le8);

        LogEntry le9 = getLogEntry(null, "9", LogService.LOG_WARNING,
                "debug exception", null, null);
        logger.logged(le9);

        LogEntry le10 = getLogEntry(null, "10", LogService.LOG_ERROR, null,
                null, null);
        logger.logged(le10);
    }

    /**
     * Creates a log entry.
     *
     * @param name      the name
     * @param message   the message
     * @param level     the level
     * @param exception the exception
     * @param refid     the service id.
     * @param refpid    the service pid.
     * @return a mock log entry.
     */
    private LogEntry getLogEntry(String name, String message, int level,
                                 String exception, Long refid, String refpid) {
        LogEntry le = Mockito.mock(LogEntry.class);
        Mockito.when(le.getMessage()).thenReturn(message);
        if (exception == null) {
            Mockito.when(le.getException()).thenReturn(null);
        } else {
            Mockito.when(le.getException())
                    .thenReturn(new Exception(exception));
        }
        Mockito.when(le.getLevel()).thenReturn(level);

        if (name == null) {
            Mockito.when(le.getBundle()).thenReturn(null);
        } else {
            Bundle bundle = Mockito.mock(Bundle.class);
            Mockito.when(bundle.getSymbolicName()).thenReturn(name);
            Mockito.when(le.getBundle()).thenReturn(bundle);
        }

        if (refid == null) {
            Mockito.when(le.getServiceReference()).thenReturn(null);
        } else {
            ServiceReference sr = Mockito
                    .mock(ServiceReference.class);
            Mockito.when(sr.getProperty("service.pid")).thenReturn(refpid);
            Mockito.when(sr.getProperty("service.id")).thenReturn(refid);
            Mockito.when(le.getServiceReference()).thenReturn(sr);
        }

        return le;
    }

    /**
     * Simulates the availability of the log service.
     *
     * @throws Exception if something wrong happens.
     */
    @Test
    public void testStartWithServiceAvailable() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito
                .mock(ServiceReference.class);

        LogReaderService reader = Mockito
                .mock(LogReaderService.class);

        Mockito.when(bc.getServiceReference(LogReaderService.class.getName())).thenReturn(ref);
        Mockito.when(bc.getService(ref)).thenReturn(reader);

        // Start the test
        logger.start(bc);

        // Verify:
        // addServiceListener called once with logger
        Mockito.verify(reader, Mockito.times(1))
                .addLogListener(logger);

        // Service Listener registered
        Mockito.verify(bc, Mockito.times(1))
                .addServiceListener(logger, "(" + org.osgi.framework.Constants.OBJECTCLASS + "="
                        + LogReaderService.class.getName() + ")");

        Mockito.verify(bc, Mockito.times(1))
                .getServiceReference(LogReaderService.class.getName());
    }

    /**
     * Simulates the unavailability of the log service.
     *
     * @throws Exception if something wrong happens.
     */
    @Test
    public void testStartWithServiceUnavailable() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito.mock(ServiceReference.class);

        LogReaderService reader = Mockito.mock(LogReaderService.class);

        Mockito.when(bc.getServiceReference(LogReaderService.class.getName()))
                .thenReturn(null);
        Mockito.when(bc.getService(ref)).thenReturn(reader);

        // Start the test
        logger.start(bc);

        // Verify:
        Mockito.verify(reader, Mockito.times(0))
                .addLogListener(logger);

        // Service Listener registered
        Mockito.verify(bc, Mockito.times(1))
                .addServiceListener(logger, "(" + org.osgi.framework.Constants.OBJECTCLASS + "="
                        + LogReaderService.class.getName() + ")");

        Mockito.verify(bc, Mockito.times(1))
                .getServiceReference(LogReaderService.class.getName());
    }

    /**
     * Simulates the availability of the log service during the
     * stop phase.
     *
     * @throws Exception if something wrong happens.
     */
    @Test
    public void testStopMethodWithService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        LogReaderService reader = Mockito.mock(LogReaderService.class);

        logger.setLogServiceReference(ref);

        Mockito.when(bc.getServiceReference(LogReaderService.class.getName())).thenReturn(ref);
        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.stop(bc);

        // Verify
        Mockito.verify(reader, Mockito.times(0)).addLogListener(logger);
        Mockito.verify(reader, Mockito.times(1)).removeLogListener(logger);
        Mockito.verify(bc, Mockito.times(1)).removeServiceListener(logger);

    }

    /**
     * Simulates the unavailability of the log service during
     * the stop phase.
     *
     * @throws Exception if something wrong happens.
     */
    @Test
    public void testStopMethodWithoutService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito
                .mock(ServiceReference.class);
        LogReaderService reader = Mockito
                .mock(LogReaderService.class);

        logger.setLogServiceReference(null);

        Mockito.when(bc.getServiceReference(LogReaderService.class.getName()))
                .thenReturn(null);
        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.stop(bc);

        // Verify
        Mockito.verify(reader, Mockito.times(0))
                .addLogListener(logger);
        Mockito.verify(reader, Mockito.times(0))
                .removeLogListener(logger);
        Mockito.verify(bc, Mockito.times(1))
                .removeServiceListener(logger);
    }

    /**
     * Simulates the arrival of a log service.
     */
    @Test
    public void testServiceChangedArrivalNoService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        LogReaderService reader = Mockito.mock(LogReaderService.class);

        // ServiceEvent
        ServiceEvent ev = Mockito.mock(ServiceEvent.class);
        Mockito.when(ev.getServiceReference()).thenReturn(ref);
        Mockito.when(ev.getType()).thenReturn(ServiceEvent.REGISTERED);

        logger.start(bc);
        logger.setLogServiceReference(null);

        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.serviceChanged(ev);

        // Verify
        Mockito.verify(reader, Mockito.times(1)).addLogListener(logger);

    }

    /**
     * Simulates the arrival of a log service despite already one is there.
     */
    @Test
    public void testServiceChangedArrivalWithAlreadyAService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito
                .mock(ServiceReference.class);
        ServiceReference ref2 = Mockito
                .mock(ServiceReference.class);

        LogReaderService reader = Mockito
                .mock(LogReaderService.class);

        // ServiceEvent
        ServiceEvent ev = Mockito.mock(ServiceEvent.class);
        Mockito.when(ev.getServiceReference()).thenReturn(ref2);
        Mockito.when(ev.getType()).thenReturn(
                ServiceEvent.REGISTERED);

        logger.start(bc);
        logger.setLogServiceReference(ref);

        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.serviceChanged(ev);

        // Verify
        Mockito.verify(reader, Mockito.times(0))
                .addLogListener(logger);

    }

    /**
     * Simulates the departure of the used log service.
     */
    @Test
    public void testServiceChangedDepartureWithTheUsedService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito.mock(ServiceReference.class);

        LogReaderService reader = Mockito.mock(LogReaderService.class);

        // ServiceEvent
        ServiceEvent ev = Mockito.mock(ServiceEvent.class);
        Mockito.when(ev.getServiceReference()).thenReturn(ref);
        Mockito.when(ev.getType()).thenReturn(ServiceEvent.UNREGISTERING);

        logger.start(bc);
        logger.setLogServiceReference(ref);

        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.serviceChanged(ev);

        // Verify
        Mockito.verify(reader, Mockito.times(0))
                .addLogListener(logger);
        Mockito.verify(reader, Mockito.times(1))
                .removeLogListener(logger);
    }

    /**
     * Simulates the departure of a not used log service.
     */
    @Test
    public void testServiceChangedDepartureWithAnotherService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        ServiceReference ref2 = Mockito.mock(ServiceReference.class);

        LogReaderService reader = Mockito.mock(LogReaderService.class);

        // ServiceEvent
        ServiceEvent ev = Mockito.mock(ServiceEvent.class);
        Mockito.when(ev.getServiceReference()).thenReturn(ref2);
        Mockito.when(ev.getType()).thenReturn(ServiceEvent.UNREGISTERING);

        logger.start(bc);
        logger.setLogServiceReference(ref);

        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.serviceChanged(ev);

        // Verify
        Mockito.verify(reader, Mockito.times(0))
                .addLogListener(logger);
        Mockito.verify(reader, Mockito.times(0))
                .removeLogListener(logger);
    }

    /**
     * Simulates the departure of a log service despite no are used.
     */
    @Test
    public void testServiceChangedDepartureWithNoService() throws Exception {
        BundleContext bc = Mockito.mock(BundleContext.class);
        ServiceReference ref = Mockito
                .mock(ServiceReference.class);

        LogReaderService reader = Mockito
                .mock(LogReaderService.class);

        // ServiceEvent
        ServiceEvent ev = Mockito.mock(ServiceEvent.class);
        Mockito.when(ev.getServiceReference()).thenReturn(ref);
        Mockito.when(ev.getType()).thenReturn(
                ServiceEvent.UNREGISTERING);

        logger.start(bc);
        logger.setLogServiceReference(null);

        Mockito.when(bc.getService(ref)).thenReturn(reader);

        logger.serviceChanged(ev);

        // Verify
        Mockito.verify(reader, Mockito.times(0))
                .addLogListener(logger);
        Mockito.verify(reader, Mockito.times(0))
                .removeLogListener(logger);
    }
}
