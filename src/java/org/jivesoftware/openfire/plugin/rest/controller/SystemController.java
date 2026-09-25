/*
 * Copyright (c) 2022-2026 Ignite Realtime Foundation
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SystemController {
    private static final Logger LOG = LoggerFactory.getLogger(SystemController.class);

    /**
     * Key prefix used by all system properties owned by this plugin.
     */
    private static final String RESTRICTED_PROPERTY_KEY_PREFIX = "plugin.restapi.";

    /**
     * The keys of system properties that can be created or deleted via the REST API: one or more dot-separated parts,
     * each consisting of ASCII letters, digits, underscores, apostrophes and hyphens.
     *
     * This excludes characters that Openfire or its database would interpret, rather than store or match literally
     * (such as whitespace, most SQL LIKE wildcards, and characters that some databases consider equal to others).
     */
    private static final Pattern VALID_PROPERTY_KEY = Pattern.compile("[A-Za-z0-9_'-]+(\\.[A-Za-z0-9_'-]+)*");

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
        if (RESTServicePlugin.SERVICE_LOGGING_ENABLED.getValue()) {
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

        // Ensure that we're not exposing any 'forbidden' properties.
        final Set<String> forbiddenPropertyKeys = getForbiddenPropertyKeys();
        compoundProperties.removeIf(systemProperty -> isForbiddenPropertyKey(systemProperty.getKey(), forbiddenPropertyKeys));

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
        // Ensure that we're not exposing any 'forbidden' properties.
        final Set<String> forbiddenPropertyKeys = getForbiddenPropertyKeys();

        final Optional<SystemProperty> systemProperty = SystemProperty.getProperty(propertyKey);
        if (systemProperty.isPresent()) {
            if (isForbiddenPropertyKey(systemProperty.get().getKey(), forbiddenPropertyKeys)) {
                // Ensure that we're not exposing any 'forbidden' properties.
                throw new ServiceException("Access to property is forbidden", propertyKey, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN);
            }
            // There's guaranteed to be a system property - return a value (even null), no matter what.
            return new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(propertyKey, systemProperty.get().getValueAsSaved());
        }

        // No system property found. Check JiveGlobals. This cannot distinguish between a property that is not set and a property that is set to null.
        if (isForbiddenPropertyKey(propertyKey, forbiddenPropertyKeys)) {
            // Ensure that we're not exposing any 'forbidden' properties.
            throw new ServiceException("Access to property is forbidden", propertyKey, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN);
        }

        final String propertyValue = JiveGlobals.getProperty(propertyKey);
        if (propertyValue != null) {
            return new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(propertyKey, propertyValue);
        } else {
            throw new ServiceException("Could not find property", propertyKey, ExceptionType.PROPERTY_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    /**
     * Creates the system property, or overwrites the value of an existing property that has the same key.
     *
     * @param systemProperty the system property
     * @throws ServiceException when no property is provided, when the key is not valid, or when the property has no
     * value (400), when the property is forbidden (403), or when the key differs only in case from the key of an
     * existing property (409).
     */
    public void createSystemProperty(org.jivesoftware.openfire.plugin.rest.entity.SystemProperty systemProperty) throws ServiceException
    {
        if (systemProperty == null) {
            throw new ServiceException("Could not create property, as the request does not contain a property definition.", null, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        final String propertyKey = systemProperty.getKey();
        validatePropertyKey(propertyKey);

        // Ensure that we're not exposing any 'forbidden' properties.
        if (isForbiddenPropertyKey(propertyKey, getForbiddenPropertyKeys())) {
            throw new ServiceException("Could not create property", propertyKey, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN);
        }

        if (hasCaseInsensitiveKeyCollision(propertyKey)) {
            throw new ServiceException("Could not create property, as its key differs only in case from the key of an existing property.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.CONFLICT);
        }

        if (systemProperty.getValue() == null) {
            // Setting null values in JiveGlobals will cause a property to be deleted!
            throw new ServiceException("Could not create property, as the value is missing.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        JiveGlobals.setProperty(propertyKey, systemProperty.getValue());
    }

    /**
     * Deletes a system property, together with all of its child properties (properties of which the key starts with
     * the key of this property, followed by a dot).
     *
     * The deletion is refused when the property or any of its child properties is forbidden (see
     * {@link #isForbiddenPropertyKey(String, Set)}), and when the database could delete any other property as well
     * (see {@link #getKeyDeletionPattern(String)}).
     *
     * @param propertyKey the property key
     * @throws ServiceException when the key is not valid (400), when the property or one of its children is forbidden
     * (403), when the property does not exist (404), or when deleting it could delete other properties (409).
     */
    public void deleteSystemProperty(String propertyKey) throws ServiceException {
        validatePropertyKey(propertyKey);

        // Ensure that we're not exposing any 'forbidden' properties. Openfire deletes a property together with all of
        // its child properties, so none of those can be forbidden either.
        final Set<String> forbiddenPropertyKeys = getForbiddenPropertyKeys();
        final Collection<String> existingPropertyKeys = JiveGlobals.getPropertyNames();
        if (isForbiddenPropertyKey(propertyKey, forbiddenPropertyKeys)
            || existingPropertyKeys.stream().anyMatch(key -> key.startsWith(propertyKey + ".") && isForbiddenPropertyKey(key, forbiddenPropertyKeys))) {
            throw new ServiceException("Could not delete property", propertyKey, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN);
        }

        if (JiveGlobals.getProperty(propertyKey) == null) {
            throw new ServiceException("Could not find property", propertyKey, ExceptionType.PROPERTY_NOT_FOUND,
                Response.Status.NOT_FOUND);
        }

        // The database can match more properties than the property and its children (see getKeyDeletionPattern).
        // Refuse to delete when that would delete any other existing property.
        final Pattern deletionPattern = getKeyDeletionPattern(propertyKey);
        if (existingPropertyKeys.stream().anyMatch(key -> !key.equals(propertyKey) && !key.startsWith(propertyKey + ".") && deletionPattern.matcher(key).matches())) {
            throw new ServiceException("Could not delete property, as that could also delete unintended properties (other than this property and its child properties). This can happen, for example, when the key contains an underscore, which can match any character.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.CONFLICT);
        }

        JiveGlobals.deleteProperty(propertyKey);
    }

    /**
     * Update system property.
     *
     * @param propertyKey the property key
     * @param systemProperty the system property
     * @throws ServiceException when the property is forbidden (403), when no property is provided, when the property
     * has no value, or when the key in the path and the entity do not match (400), when the property does not exist
     * (404), or when the key differs only in case from the key of another existing property (409).
     */
    public void updateSystemProperty(String propertyKey, org.jivesoftware.openfire.plugin.rest.entity.SystemProperty systemProperty) throws ServiceException {
        // Ensure that we're not exposing any 'forbidden' properties.
        if (isForbiddenPropertyKey(propertyKey, getForbiddenPropertyKeys())) {
            throw new ServiceException("Could not update property", propertyKey, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN);
        }
        if (systemProperty == null) {
            throw new ServiceException("Could not update property, as the request does not contain a property definition.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        if (systemProperty.getValue() == null) {
            // Setting null values in JiveGlobals will cause a property to be deleted!
            throw new ServiceException("Could not update property, as the new value is missing.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        if(JiveGlobals.getProperty(propertyKey) != null) {
            if(systemProperty.getKey().equals(propertyKey)) {
                if (hasCaseInsensitiveKeyCollision(propertyKey)) {
                    throw new ServiceException("Could not update property, as its key differs only in case from the key of another existing property.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.CONFLICT);
                }
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

    /**
     * Returns a set of keys of registered system properties that must not be exposed or modified via the REST API.
     *
     * This covers properties that are used to configure this plugin itself, and properties that are flagged as being
     * encrypted.
     *
     * The returned set is based on {@link SystemProperty} registrations only. It is therefore incomplete: callers
     * should use {@code isForbiddenPropertyKey(String, Set)} to determine if a particular property key is forbidden,
     * as that also applies checks that do not depend on a property being registered.
     *
     * @return a set of system property keys (never null, possibly empty).
     */
    public static Set<String> getForbiddenPropertyKeys()
    {
        final String pluginName = RESTServicePlugin.ENABLED.getPlugin();
        return org.jivesoftware.util.SystemProperty.getProperties().stream()

            // Do not expose or allow modifications of the configuration of this plugin itself (see https://github.com/igniterealtime/openfire-restAPI-plugin/issues/244)
            // or of properties that are encrypted (see https://github.com/igniterealtime/openfire-restAPI-plugin/issues/248). Checking the
            // registration (rather than only the stored value, as isForbiddenPropertyKey does) also catches properties that are defined to be
            // encrypted, for which no encrypted value has been stored yet.
            .filter(p -> p.isEncrypted()
                || pluginName.equals(p.getPlugin()) // This works only because all properties used by the plugin are SystemProperty instances (as opposed to using JiveGlobals directly).
            )
            .map(org.jivesoftware.util.SystemProperty::getKey)
            .collect(Collectors.toSet());
    }

    /**
     * Determines whether a property key is one that this plugin should not expose or allow modification of.
     *
     * A property key is forbidden when:
     * <ul>
     *     <li>it is part of the provided set (see {@link #getForbiddenPropertyKeys()});</li>
     *     <li>it has the key prefix used by this plugin's own properties. The set can only reflect properties whose
     *     owning class has already been loaded by the JVM (SystemProperty registration is a side effect of static
     *     initialization, which for some of this plugin's properties - e.g. those declared by MUCRoomController -
     *     isn't guaranteed to have happened yet);</li>
     *     <li>its value is stored encrypted (which applies also to properties that are not registered as a
     *     {@link SystemProperty}, such as LDAP or SMTP credentials);</li>
     *     <li>it is considered sensitive by its name (e.g. it contains 'password'), which is a convention that the
     *     Openfire admin console also uses to hide the value of a property.</li>
     * </ul>
     *
     * @param propertyKey the property key to check.
     * @param forbiddenPropertyKeys the result of {@link #getForbiddenPropertyKeys()}, provided by the caller to avoid recomputing it.
     * @return true if the property key is forbidden, otherwise false.
     */
    private static boolean isForbiddenPropertyKey(final String propertyKey, final Set<String> forbiddenPropertyKeys)
    {
        return propertyKey != null && (forbiddenPropertyKeys.contains(propertyKey)
            || propertyKey.startsWith(RESTRICTED_PROPERTY_KEY_PREFIX)
            || JiveGlobals.isPropertyEncrypted(propertyKey)
            || JiveGlobals.isPropertySensitive(propertyKey));
    }

    /**
     * Determines whether an existing property has a key that differs from the provided key only in case.
     *
     * Depending on the database, keys that differ only in case are considered equal. Writing a property of which the
     * key differs only in case from that of an existing property can then change the value of that existing property.
     *
     * @param propertyKey the property key to check.
     * @return true if another property exists of which the key differs only in case, otherwise false.
     */
    private static boolean hasCaseInsensitiveKeyCollision(final String propertyKey)
    {
        return JiveGlobals.getPropertyNames().stream().anyMatch(key -> !key.equals(propertyKey) && key.equalsIgnoreCase(propertyKey));
    }

    /**
     * Verifies that a property key is one that can be created or deleted via the REST API (see
     * {@link #VALID_PROPERTY_KEY}).
     *
     * @param propertyKey the property key to check.
     * @throws ServiceException when the property key is not valid.
     */
    private static void validatePropertyKey(final String propertyKey) throws ServiceException
    {
        if (propertyKey == null || !VALID_PROPERTY_KEY.matcher(propertyKey).matches()) {
            throw new ServiceException("Invalid property key. Keys consist of one or more dot-separated parts, each consisting of ASCII letters, digits, underscores, apostrophes and hyphens.", propertyKey, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Returns a pattern that matches the keys of all properties that Openfire could delete when the property with
     * the provided key is deleted.
     *
     * Openfire deletes a property together with all of its child properties, using the database statement
     * {@code DELETE FROM ofProperty WHERE name = ? OR name LIKE ?} (with {@code key + ".%"} as the second argument).
     * As the key is not escaped, any {@code _} character in it acts as a wildcard (other wildcards are not allowed in
     * a key, see {@link #VALID_PROPERTY_KEY}). Depending on the database, matching can also be case-insensitive.
     *
     * @param propertyKey the (valid) key of the property that is to be deleted.
     * @return a pattern matching the keys of properties that could be deleted.
     */
    private static Pattern getKeyDeletionPattern(final String propertyKey)
    {
        final StringBuilder regex = new StringBuilder();
        for (final char c : propertyKey.toCharArray()) {
            regex.append(c == '_' ? "." : Pattern.quote(String.valueOf(c))); // SQL LIKE: '_' matches any single character.
        }
        regex.append("(\\..*)?"); // The property itself, or any of its children.
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }
}
