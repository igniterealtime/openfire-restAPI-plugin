/*
 * Copyright (c) 2022.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.spi.ConnectionListener;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.openfire.spi.ConnectionType;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.SystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;

public class SystemController {
    private static final Logger LOG = LoggerFactory.getLogger(SystemController.class);

    private static SystemController INSTANCE = null;

    /**
     * Gets the single instance of SystemController.
     *
     * @return single instance of SystemController
     */
    public static SystemController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SystemController();
        }
        return INSTANCE;
    }

    /**
     * @param instance the mock/stub/spy controller to use.
     * @deprecated - for test use only
     */
    @Deprecated
    public static void setInstance(final SystemController instance) {
        SystemController.INSTANCE = instance;
    }

    public static void log(String logMessage) {
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            LOG.info(logMessage);
        }
    }

    /**
     * Gets the system properties.
     *
     * @return the system properties
     */
    public SystemProperties getSystemProperties() {
        final Collection<SystemProperty> systemProperties = org.jivesoftware.util.SystemProperty.getProperties();
        final Set<String> systemPropertyKeys = systemProperties.stream().map(org.jivesoftware.util.SystemProperty::getKey).collect(Collectors.toSet());
        // Get all the SystemProperties
        final List<org.jivesoftware.openfire.plugin.rest.entity.SystemProperty> compoundProperties = systemProperties.stream().map(p -> new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(p.getKey(), p.getValueAsSaved())).collect(Collectors.toList());
        // Now add any missing JiveGlobals properties
        JiveGlobals.getPropertyNames().stream().filter(key -> !systemPropertyKeys.contains(key)).forEach(key -> compoundProperties.add(new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, JiveGlobals.getProperty(key))));
        // And sort by key
        compoundProperties.sort(Comparator.comparing(org.jivesoftware.openfire.plugin.rest.entity.SystemProperty::getKey));

        SystemProperties result = new SystemProperties();
        result.setProperties(compoundProperties);
        return result;
    }

    /**
     * Gets the system property.
     *
     * @param propertyKey the property key
     * @return the system property
     * @throws ServiceException the service exception
     */
    public org.jivesoftware.openfire.plugin.rest.entity.SystemProperty getSystemProperty(String propertyKey) throws ServiceException {
        String propertyValue = JiveGlobals.getProperty(propertyKey);
        if(propertyValue != null) {
            return new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(propertyKey, propertyValue);
        } else {
            throw new ServiceException("Could not find property", propertyKey, ExceptionType.PROPERTY_NOT_FOUND,
                Response.Status.NOT_FOUND);
        }
    }

    /**
     * Creates the system property.
     *
     * @param systemProperty the system property
     */
    public void createSystemProperty(org.jivesoftware.openfire.plugin.rest.entity.SystemProperty systemProperty) {
        JiveGlobals.setProperty(systemProperty.getKey(), systemProperty.getValue());
    }

    /**
     * Delete system property.
     *
     * @param propertyKey the property key
     * @throws ServiceException the service exception
     */
    public void deleteSystemProperty(String propertyKey) throws ServiceException {
        if(JiveGlobals.getProperty(propertyKey) != null) {
            JiveGlobals.deleteProperty(propertyKey);
        } else {
            throw new ServiceException("Could not find property", propertyKey, ExceptionType.PROPERTY_NOT_FOUND,
                Response.Status.NOT_FOUND);
        }
    }

    /**
     * Update system property.
     *
     * @param propertyKey the property key
     * @param systemProperty the system property
     * @throws ServiceException the service exception
     */
    public void updateSystemProperty(String propertyKey, org.jivesoftware.openfire.plugin.rest.entity.SystemProperty systemProperty) throws ServiceException {
        if(JiveGlobals.getProperty(propertyKey) != null) {
            if(systemProperty.getKey().equals(propertyKey)) {
                JiveGlobals.setProperty(propertyKey, systemProperty.getValue());
            } else {
                throw new ServiceException("Path property name and entity property name doesn't match", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                    Response.Status.BAD_REQUEST);
            }
        } else {
            throw new ServiceException("Could not find property for update", systemProperty.getKey(), ExceptionType.PROPERTY_NOT_FOUND,
                Response.Status.NOT_FOUND);
        }
    }

    /**
     * Determines if there are threads that are deadlocked.
     *
     * @return true if deadlocked threads are found, otherwise false.
     */
    public boolean hasDeadlock() {
        try {
            return ManagementFactory.getThreadMXBean().findDeadlockedThreads() != null;
        } catch (Throwable t) {
            LOG.warn("Unable to determine if there is a deadlock.", t);
            return false;
        }
    }

    /**
     * Determines if there are any system properties that require a restart.
     *
     * @return true if at least one system property requires a restart, otherwise false.
     */
    public boolean hasSystemPropertyRequiringRestart() {
        try {
            SystemProperty.getProperties().stream().filter(SystemProperty::isRestartRequired).forEach(p -> LOG.info("Requires restart: {}. Current value: {}. Initial value: {}", p.getKey(), p.getValue(), p.getDisplayValue()));
            return SystemProperty.getProperties().stream()
                .filter(systemProperty -> !systemProperty.getKey().equals("xmpp.domain")) // xmpp.domain can report a false positive. See OF-2399
                .anyMatch(SystemProperty::isRestartRequired);
        } catch (Throwable t) {
            LOG.warn("Unable to determine if there are any system properties that require a restart.", t);
            return false;
        }
    }

    /**
     * Determine if the core Openfire service is started.
     *
     * @return true if Openfire has started, otherwise false.
     */
    public boolean isStarted() {
        return XMPPServer.getInstance().isStarted();
    }

    /**
     * Determine if clustering has been fully started, if clustering is enabled.
     *
     * When clustering is not enabled, this method will always return 'true'.
     *
     * @return true if Openfire has started, otherwise false.
     */
    public boolean hasClusteringStartedWhenEnabled() {
        if (!ClusterManager.isClusteringEnabled()) {
            return true;
        }
        return ClusterManager.isClusteringStarted();
    }

    /**
     * Checks if the plugin manager has finished starting all plugins that were available at boot-time.
     *
     * Note that the return value does not indicate that plugins succeeded or failed to start: a 'true' return value
     * indicates only that the plugin manager finished its initial attempt to start all plugins.
     *
     * @return True when all (initial) plugins have been started.
     */
    public boolean hasPluginManagerExecuted() {
        return XMPPServer.getInstance().getPluginManager().isExecuted();
    }

    /**
     * Verifies that a connection listener for the provided type and encryption level is disabled, or ready to accept connections.
     *
     * @param connectionType The type of connection for which to check state
     * @param encrypted true when the direct-TLS encrypted variant of the listeners is to be checked, otherwise false.
     * @return True when the connection listener is ready to accept connections, or is disabled.
     */
    public boolean isConnectionListenerStartedWhenEnabled(@Nonnull final ConnectionType connectionType, final boolean encrypted) {
        switch (connectionType) {
            case BOSH_C2S:
                if (encrypted) {
                    return !HttpBindManager.HTTP_BIND_ENABLED.getValue() || HttpBindManager.HTTP_BIND_SECURE_PORT.getValue() <= 0 || HttpBindManager.getInstance().isHttpsBindActive();
                } else {
                    return !HttpBindManager.HTTP_BIND_ENABLED.getValue() || HttpBindManager.HTTP_BIND_PORT.getValue() <= 0 || HttpBindManager.getInstance().isHttpBindActive();
                }

            case WEBADMIN:
                // FIXME. See OF-2400
                return true;

            case SOCKET_S2S:
                // FIXME. See OF-2400
                return true;

            default:
                final ConnectionManagerImpl connectionManager = (ConnectionManagerImpl) XMPPServer.getInstance().getConnectionManager();
                if (connectionManager == null) {
                    return false;
                }
                final ConnectionListener listener = connectionManager.getListener(connectionType, encrypted);
                if (listener == null) {
                    return false;
                }
                // When disabled, the listener immediately is in the state that we expect it to be in.
                // If the listener is disabled, the check to see if it is 'ready' should pass.
                // If the check does not pass when a listener is disabled, then this test will always indicate that the
                // server isn't ready to be used.
                return !listener.isEnabled() || listener.getConnectionAcceptor() != null;
        }
    }
    /**
     * Verifies that all connection listeners that are enabled are ready to accept connections.
     *
     * @return True when all enabled connection listeners are ready to accept connections.
     */
    public boolean areConnectionListenersStarted() {
        for (final ConnectionType connectionType : ConnectionType.values()) {
            if (!isConnectionListenerStartedWhenEnabled(connectionType, true)) {
                return false;
            }
            if (!isConnectionListenerStartedWhenEnabled(connectionType, false)) {
                return false;
            }
        }

        return true;
    }
}
