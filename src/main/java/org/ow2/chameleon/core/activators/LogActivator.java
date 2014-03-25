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

import org.osgi.framework.*;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts the logging support.
 *
 * @author The OW2 Chameleon Team
 * @version $Id: 1.0.4 $Id
 */
public class LogActivator implements LogListener,
        BundleActivator, ServiceListener {

    /**
     * The Chameleon Logger.
     * The logger instance comes from the Chameleon launcher.
     */
    private final Logger logger; //NOSONAR
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
    synchronized ServiceReference getLogServiceReference() {
        return logService;
    }

    /**
     * Sets the log service reference.
     * For testing purpose only.
     *
     * @param ref the service reference
     */
    public synchronized void setLogServiceReference(ServiceReference ref) {
        logService = ref;
    }

    /**
     * A message were logged in the log service.
     * The log entry is dispatched to the Chameleon logger backend.
     *
     * @param le the log entry
     * @see org.osgi.service.log.LogListener#logged(org.osgi.service.log.LogEntry)
     */
    public void logged(LogEntry le) {
        String message = le.getMessage();

        // Create a new logger based on the symbolic name of the bundle logging the message.
        Logger log = this.logger;
        if (le.getBundle() != null) {
            log = LoggerFactory.getLogger(le.getBundle().getSymbolicName());
        }

        message = enhanceMessage(le, message);

        switch (le.getLevel()) {
            case LogService.LOG_DEBUG:
                debug(le, message, log);
                break;
            case LogService.LOG_INFO:
                info(le, message, log);
                break;
            case LogService.LOG_WARNING:
                warn(le, message, log);
                break;
            case LogService.LOG_ERROR:
                error(le, message, log);
                break;
            default:
                // Cannot happen
                break;
        }

    }

    private void error(LogEntry le, String message, Logger logger) {
        if (le.getException() != null) {
            logger.error(message, le.getException());
        } else {
            logger.error(message);
        }
    }

    private void warn(LogEntry le, String message, Logger logger) {
        if (le.getException() != null) {
            logger.warn(message, le.getException());
        } else {
            logger.warn(message);
        }
    }

    private void info(LogEntry le, String message, Logger logger) {
        if (le.getException() != null) {
            logger.info(message, le.getException());
        } else {
            logger.info(message);
        }
    }

    private void debug(LogEntry le, String message, Logger logger) {
        if (le.getException() != null) {
            logger.debug(message, le.getException());
        } else {
            logger.debug(message);
        }
    }

    private String enhanceMessage(LogEntry le, String message) {
        if (le.getServiceReference() != null) {
            if (le.getServiceReference().getProperty(
                    Constants.SERVICE_PID) != null) {
                return message
                        + " [ServicePID="
                        + le.getServiceReference().getProperty(
                        Constants.SERVICE_PID) + "]";
            } else {
                return message
                        + " [ServiceID="
                        + le.getServiceReference().getProperty(
                        Constants.SERVICE_ID) + "]";
            }
        } else {
            return message;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Initializes LogReaderService tracking.
     *
     * @param bc the bundle context.
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        synchronized (this) {
            context = bc;
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
     * @param bc the bundle context.
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception {
        if (bc != null) {
            bc.removeServiceListener(this);
            synchronized (this) {
                if (logService != null) {
                    LogReaderService reader = (LogReaderService)
                            bc.getService(logService);
                    reader.removeLogListener(this);
                    logService = null;
                }
            }
        }
    }

    /**
     * The Service Listener method.
     *
     * @param ev the event
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
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
