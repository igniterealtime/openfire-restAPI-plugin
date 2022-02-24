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

import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.SystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
}
