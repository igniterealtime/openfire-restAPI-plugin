/*
 * Copyright (C) 2005-2008 Jive Software, 2022-2026 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.rest;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.plugin.rest.service.JerseyWrapper;
import org.jivesoftware.openfire.stats.StatisticsManager;
import org.jivesoftware.util.*;

import java.io.File;
import java.util.*;

/**
 * The Class RESTServicePlugin.
 */
public class RESTServicePlugin implements Plugin, PropertyEventListener {

    /**
     * The value that is used to authenticate requests when using 'shared secret' authentication.
     */
    public static final SystemProperty<String> SECRET = SystemProperty.Builder.ofType(String.class)
        .setPlugin("REST API")
        .setKey("plugin.restapi.secret")
        .setDynamic(true)
        .setEncrypted(true)
        .build();

    /**
     * Enables or disables additional logging of REST API service calls.
     */
    public static final SystemProperty<Boolean> SERVICE_LOGGING_ENABLED = SystemProperty.Builder.ofType(Boolean.class)
        .setPlugin("REST API")
        .setKey("plugin.restapi.serviceLoggingEnabled")
        .setDynamic(true)
        .setDefaultValue(false)
        .build();

    /**
     * The class name of a custom authentication filter implementation.
     */
    public static final SystemProperty<String> CUSTOM_AUTH_FILTER = SystemProperty.Builder.ofType(String.class)
        .setPlugin("REST API")
        .setKey("plugin.restapi.customAuthFilter")
        .setDynamic(true)
        .build();

    /** The allowed i ps. */
    private Collection<String> allowedIPs;
    
    /** The enabled. */
    private boolean enabled;

    /** The http auth. */
    private String httpAuth;
    
    private final Set<String> registeredStatisticKeys = new HashSet<>();

    /* (non-Javadoc)
     * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware.openfire.container.PluginManager, java.io.File)
     */
    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        // If no secret key has been assigned, assign a random one.
        if (SECRET.getValue() == null || SECRET.getValue().isEmpty()) {
            SECRET.setValue(StringUtils.randomString(16));
        }
        
        // Start collecting statistics.
        for (StatisticsFilter.RestResponseFamilyStatistic statistic : StatisticsFilter.generateAllFamilyStatisticInstances()) {
            StatisticsManager.getInstance().addStatistic(statistic.getKeyName(), statistic);
            registeredStatisticKeys.add(statistic.getKeyName());
        }

        // See if the service is enabled or not.
        enabled = JiveGlobals.getBooleanProperty("plugin.restapi.enabled", false);

        // See if the HTTP Basic Auth is enabled or not.
        httpAuth = JiveGlobals.getProperty("plugin.restapi.httpAuth", "basic");

        // Get the list of IP addresses that can use this service. An empty list
        // means that this filter is disabled.
        allowedIPs = StringUtils.stringToCollection(JiveGlobals.getProperty("plugin.restapi.allowedIPs", ""));

        // Listen to system property events
        PropertyEventDispatcher.addListener(this);

        // Exclude this servlet from requering the user to login
        AuthCheckFilter.addExclude(JerseyWrapper.SERVLET_URL);
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
     */
    public void destroyPlugin() {
        // Stop registering statistics.
        final Iterator<String> iter = registeredStatisticKeys.iterator();
        while (iter.hasNext()) {
            StatisticsManager.getInstance().removeStatistic(iter.next());
            iter.remove();
        }

        // Release the excluded URL
        AuthCheckFilter.removeExclude(JerseyWrapper.SERVLET_URL);
        // Stop listening to system property events
        PropertyEventDispatcher.removeListener(this);
    }

    /**
     * Returns the loading status message.
     *
     * @return the loading status message.
     */
    public String getLoadingStatusMessage() {
        return JerseyWrapper.getLoadingStatusMessage();
    }
    
    /**
     * Reloads the Jersey wrapper.
     */
    public String loadAuthenticationFilter(String customAuthFilterClassName) {
        return JerseyWrapper.tryLoadingAuthenticationFilter(customAuthFilterClassName);
    }
    
    /**
     * Gets the allowed i ps.
     *
     * @return the allowed i ps
     */
    public Collection<String> getAllowedIPs() {
        return allowedIPs;
    }

    /**
     * Sets the allowed i ps.
     *
     * @param allowedIPs the new allowed i ps
     */
    public void setAllowedIPs(Collection<String> allowedIPs) {
        JiveGlobals.setProperty("plugin.restapi.allowedIPs", StringUtils.collectionToString(allowedIPs));
        this.allowedIPs = allowedIPs;
    }

    /**
     * Returns true if the user service is enabled. If not enabled, it will not
     * accept requests to create new accounts.
     *
     * @return true if the user service is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables the user service. If not enabled, it will not accept
     * requests to create new accounts.
     *
     * @param enabled
     *            true if the user service should be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        JiveGlobals.setProperty("plugin.restapi.enabled", enabled ? "true" : "false");
    }

    /**
     * Gets the http authentication mechanism.
     *
     * @return the http authentication mechanism
     */
    public String getHttpAuth() {
        return httpAuth;
    }

    /**
     * Sets the http auth.
     *
     * @param httpAuth the new http auth
     */
    public void setHttpAuth(String httpAuth) {
        this.httpAuth = httpAuth;
        JiveGlobals.setProperty("plugin.restapi.httpAuth", httpAuth);
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.util.PropertyEventListener#propertySet(java.lang.String, java.util.Map)
     */
    public void propertySet(String property, Map<String, Object> params) {
        if (property.equals("plugin.restapi.enabled")) {
            this.enabled = Boolean.parseBoolean((String) params.get("value"));
        } else if (property.equals("plugin.restapi.allowedIPs")) {
            this.allowedIPs = StringUtils.stringToCollection((String) params.get("value"));
        } else if (property.equals("plugin.restapi.httpAuth")) {
            this.httpAuth = (String) params.get("value");
        }
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.util.PropertyEventListener#propertyDeleted(java.lang.String, java.util.Map)
     */
    public void propertyDeleted(String property, Map<String, Object> params) {
        if (property.equals("plugin.restapi.enabled")) {
            this.enabled = false;
        } else if (property.equals("plugin.restapi.allowedIPs")) {
            this.allowedIPs = Collections.emptyList();
        } else if (property.equals("plugin.restapi.httpAuth")) {
            this.httpAuth = "basic";
        }
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.util.PropertyEventListener#xmlPropertySet(java.lang.String, java.util.Map)
     */
    public void xmlPropertySet(String property, Map<String, Object> params) {
        // Do nothing
    }

    /* (non-Javadoc)
     * @see org.jivesoftware.util.PropertyEventListener#xmlPropertyDeleted(java.lang.String, java.util.Map)
     */
    public void xmlPropertyDeleted(String property, Map<String, Object> params) {
        // Do nothing
    }
}
