/*
 * Copyright 2013 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ow2.chameleon.core.activators;

import org.osgi.framework.*;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the logging support.
 */
public class LogActivator implements LogListener,
        BundleActivator, ServiceListener {

    /**
     * The Chameleon Logger.
     */
    private final Logger logger;
    /**
     * The Log Service Service Reference.
     */
    private ServiceReference logService;
    /**
     * The bundle context.
     */
    private BundleContext context;

    /**
     * Creates a log activator.
     *
     * @param logger the chameleon logger
     */
    public LogActivator(Logger logger) {
        this.logger = logger;
    }

    /**
     * The Log Service Reference.
     *
     * @return the log service reference
     */
    ServiceReference getLogServiceReference() {
        return logService;
    }

    /**
     * Sets the log service reference.
     * For testing purpose only.
     *
     * @param ref the service reference
     */
    public void setLogServiceReference(ServiceReference ref) {
        logService = ref;
    }

    /**
     * A message were logged in the log service.
     * The log entry is dispatched to the Chameleon logger backend.
     *
     * @param le the log entry
     * @see org.osgi.service.log.LogListener#
     *      logged(org.osgi.service.log.LogEntry)
     */
    public void logged(LogEntry le) {
        String message = le.getMessage();

        // Create a new logger based on the symbolic name of the bundle logging the message.
        Logger thelogger = logger;
        if (le.getBundle() != null) {
            thelogger = LoggerFactory.getLogger(le.getBundle().getSymbolicName());
        }

        if (le.getServiceReference() != null) {
            if (le.getServiceReference().getProperty(
                    Constants.SERVICE_PID) != null) {
                message = message
                        + " [ServicePID="
                        + le.getServiceReference().getProperty(
                        Constants.SERVICE_PID) + "]";
            } else {
                message = message
                        + " [ServiceID="
                        + le.getServiceReference().getProperty(
                        Constants.SERVICE_ID) + "]";
            }
        }

        switch (le.getLevel()) {
            case LogService.LOG_DEBUG:
                if (le.getException() != null) {
                    thelogger.debug(message, le.getException());
                } else {
                    thelogger.debug(message);
                }
                break;
            case LogService.LOG_INFO:
                if (le.getException() != null) {
                    thelogger.info(message, le.getException());
                } else {
                    thelogger.info(message);
                }
                break;
            case LogService.LOG_WARNING:
                if (le.getException() != null) {
                    thelogger.warn(message, le.getException());
                } else {
                    thelogger.warn(message);
                }
                break;
            case LogService.LOG_ERROR:
                if (le.getException() != null) {
                    thelogger.error(message, le.getException());
                } else {
                    thelogger.error(message);
                }
                break;
            default:
                break; // Cannot happen
        }

    }

    /**
     * Initializes LogReaderService tracking.
     *
     * @param bc the bundle context
     * @throws Exception if the log activator cannot be started correctly.
     * @see org.osgi.framework.BundleActivator#
     *      start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        context = bc;
        synchronized (this) {
            // Try to get a log reader service
            context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "="
                    + LogReaderService.class.getName() + ")");
            logService = context.getServiceReference(LogReaderService.class
                    .getName());
            if (logService != null) {
                LogReaderService reader = (LogReaderService) context
                        .getService(logService);
                reader.addLogListener(this);
            }
        }
    }

    /**
     * Stops Log tracking.
     *
     * @param bc the bundle context
     * @throws Exception if the log activator cannot be started correctly.
     * @see org.osgi.framework.BundleActivator#
     *      stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        bc.removeServiceListener(this);
        if (logService != null) {
            LogReaderService reader = (LogReaderService)
                    bc.getService(logService);
            reader.removeLogListener(this);
            logService = null;
        }
    }

    /**
     * The Service Listener method.
     *
     * @param ev the Service Event
     * @see org.osgi.framework.ServiceListener#
     *      serviceChanged(org.osgi.framework.ServiceEvent)
     */
    public synchronized void serviceChanged(ServiceEvent ev) {
        if (logService == null && ev.getType() == ServiceEvent.REGISTERED) {
            logService = ev.getServiceReference();
            LogReaderService reader = (LogReaderService) context
                    .getService(logService);
            reader.addLogListener(this);
            return;
        }

        if (logService != null && ev.getType() == ServiceEvent.UNREGISTERING
                && logService == ev.getServiceReference()) {
            LogReaderService reader = (LogReaderService) context
                    .getService(logService);
            reader.removeLogListener(this);
            logService = null;
        }

    }

}
