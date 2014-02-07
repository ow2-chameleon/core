package org.ow2.chameleon.core.activators;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.ow2.chameleon.core.services.Deployer;
import org.ow2.chameleon.core.services.ExtensionBasedDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Bundle deployer.
 */
public class ConfigDeployer extends ExtensionBasedDeployer implements BundleActivator, ServiceListener {

    public static final Logger logger = LoggerFactory.getLogger(ConfigDeployer.class);

    public static final String NOT_MANAGED = "not managed";

    public static final Configuration UNMANAGED_CONFIGURATION = new Configuration() {
        @Override
        public String getPid() {
            return NOT_MANAGED;
        }

        @Override
        public Dictionary getProperties() {
            return null;
        }

        @Override
        public void update(Dictionary dictionary) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getFactoryPid() {
            return getPid();
        }

        @Override
        public void update() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBundleLocation() {
            return null;
        }

        @Override
        public void setBundleLocation(String s) {
        }

        @Override
        public String toString() {
            return NOT_MANAGED;
        }
    };
    Map<File, Configuration> configurations = new HashMap<File, Configuration>();
    private BundleContext context;

    public ConfigDeployer() {
        super("cfg");
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        context.registerService(Deployer.class, this, null);
        context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + ConfigurationAdmin.class.getName() + ")");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        removeAllConfigurations();
    }

    private Properties read(File file) throws IOException {
        InputStream in = null;
        try {
            Properties p = new Properties();
            in = new FileInputStream(file);
            p.load(in);
            return p;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Parses cfg file associated PID. This supports both ManagedService PID and
     * ManagedServiceFactory PID
     *
     * @param path the path
     * @return structure {pid, factory pid} or {pid, <code>null</code> if not a
     * Factory configuration.
     */
    String[] parsePid(String path) {
        String pid = path.substring(0, path.length() - ".cfg".length());
        int n = pid.indexOf('-');
        if (n > 0) {
            String factoryPid = pid.substring(n + 1);
            pid = pid.substring(0, n);
            return new String[]{pid, factoryPid};
        } else {
            return new String[]{pid, null};
        }
    }

    private void readAndApplyConfiguration(File file, ConfigurationAdmin admin) throws Exception {
        synchronized (this) {
            if (admin == null) {
                logger.warn("Cannot apply configuration " + file.getName() + " - no configuration admin");
                configurations.put(file, UNMANAGED_CONFIGURATION);
            } else {
                Properties properties = read(file);
                String[] pid = parsePid(file.getName());
                Dictionary<String, String> ht = new Hashtable<String, String>();
                for (String k : properties.stringPropertyNames()) {
                    ht.put(k, properties.getProperty(k));
                }
                Configuration config = configurations.get(file);
                if (config == null || config == UNMANAGED_CONFIGURATION) {
                    config = getConfiguration(pid[0], pid[1], admin);
                    if (config.getBundleLocation() != null) {
                        config.setBundleLocation(null);
                    }
                }
                logger.info("Updating configuration {} in the configuration admin, configuration: {}",
                        config.getPid(), configurations);
                config.update(ht);

                configurations.put(file, config);
            }
        }
    }

    /**
     * Gets the configuration admin service.
     *
     * @return the Configuration Admin service object
     * @throws Exception if the configuration admin is unavailable.
     */
    private ConfigurationAdmin getConfigurationAdmin()
            throws Exception {
        // Should be there !
        ServiceReference ref = context
                .getServiceReference(ConfigurationAdmin.class.getName());
        if (ref == null) {
            return null;
        } else {
            return (ConfigurationAdmin) context.getService(ref);
        }
    }

    /**
     * Gets a Configuration object.
     *
     * @param pid        the pid
     * @param factoryPid the factory pid
     * @param cm         the config admin service
     * @return the Configuration object (used to update the configuration)
     * @throws Exception if the Configuration object cannot be retrieved
     */
    Configuration getConfiguration(String pid, String factoryPid,
                                   ConfigurationAdmin cm) throws Exception {
        Configuration newConfiguration;
        if (factoryPid != null) {
            newConfiguration = cm.createFactoryConfiguration(pid, "?");
        } else {
            newConfiguration = cm.getConfiguration(pid, "?");
        }
        return newConfiguration;
    }

    @Override
    public void onFileCreate(File file) {
        logger.info("File creation event received for {}", file.getAbsoluteFile());

        synchronized (this) {
            try {
                ConfigurationAdmin admin = getConfigurationAdmin();
                readAndApplyConfiguration(file, admin);
            } catch (Exception e) {
                logger.error("Cannot find the configuration admin service", e);
            }

        }
    }

    @Override
    public void onFileDelete(File file) {
        synchronized (this) {
            Configuration configuration = configurations.remove(file);
            if (configuration != UNMANAGED_CONFIGURATION) {
                try {
                    logger.info("Deleting configuration {}", configuration.getPid());
                    configuration.delete();
                } catch (Exception e) {
                    logger.error("Cannot delete configuration from {}", configuration.getPid(), e);
                }
            }
        }
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            ConfigurationAdmin admin = (ConfigurationAdmin) context.getService(event.getServiceReference());
            processAllConfigurations(admin);
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            removeAllConfigurations();
        }
    }

    private void removeAllConfigurations() {
        synchronized (this) {
            for (Map.Entry<File, Configuration> entry : configurations.entrySet()) {
                if (entry.getValue() != UNMANAGED_CONFIGURATION) {
                    try {
                        logger.info("Deleting configuration {}", entry.getValue().getPid());
                        entry.getValue().delete();
                        entry.setValue(UNMANAGED_CONFIGURATION);
                    } catch (Exception e) {
                        logger.error("Cannot delete configuration from {}", entry.getKey().getAbsoluteFile(), e);
                    }
                }
            }
        }
    }

    private void processAllConfigurations(ConfigurationAdmin admin) {
        synchronized (this) {
            for (Map.Entry<File, Configuration> entry : configurations.entrySet()) {
                if (entry.getValue() == UNMANAGED_CONFIGURATION) {
                    try {
                        readAndApplyConfiguration(entry.getKey(), admin);
                    } catch (Exception e) {
                        logger.error("Cannot apply configuration from {}", entry.getKey().getAbsoluteFile(), e);
                    }
                }
            }
        }
    }
}
