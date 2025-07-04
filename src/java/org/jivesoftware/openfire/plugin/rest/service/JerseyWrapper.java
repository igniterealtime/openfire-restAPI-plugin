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

package org.jivesoftware.openfire.plugin.rest.service;

import org.glassfish.jersey.server.ResourceConfig;
import org.jivesoftware.openfire.plugin.rest.AuthFilter;
import org.jivesoftware.openfire.plugin.rest.CORSFilter;
import org.jivesoftware.openfire.plugin.rest.CustomJacksonMapperProvider;
import org.jivesoftware.openfire.plugin.rest.StatisticsFilter;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.util.JiveGlobals;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class JerseyWrapper.
 */
public class JerseyWrapper extends ResourceConfig {

    /** The Constant CUSTOM_AUTH_PROPERTY_NAME */
    private static final String CUSTOM_AUTH_PROPERTY_NAME = "plugin.restapi.customAuthFilter";
    
    /** The Constant REST_AUTH_TYPE */
    private static final String REST_AUTH_TYPE  = "plugin.restapi.httpAuth";

    /** The Constant SERVLET_URL. */
    public static final String SERVLET_URL = "restapi/*";
    
    /** The Constant JERSEY_LOGGER. */
    private final static Logger JERSEY_LOGGER = Logger.getLogger("org.glassfish.jersey");
    
    private static String loadingStatusMessage = null;
    
    static {
        JERSEY_LOGGER.setLevel(Level.SEVERE);
    }

    public static String tryLoadingAuthenticationFilter(String customAuthFilterClassName) {
        
        try {
            if(customAuthFilterClassName != null) {
                Class.forName(customAuthFilterClassName, false, JerseyWrapper.class.getClassLoader());
                loadingStatusMessage = null;
            }
        } catch (ClassNotFoundException e) {
            loadingStatusMessage = "No custom auth filter found for restAPI plugin with name " + customAuthFilterClassName;
        }
        
        if(customAuthFilterClassName == null || customAuthFilterClassName.isEmpty())
            loadingStatusMessage = "Classname field can't be empty!";
        return loadingStatusMessage;
    }
    
    public String loadAuthenticationFilter() {
            
        // Check if custom AuthFilter is available
        String customAuthFilterClassName = JiveGlobals.getProperty(CUSTOM_AUTH_PROPERTY_NAME);
        String restAuthType = JiveGlobals.getProperty(REST_AUTH_TYPE);
        Class<?> pickedAuthFilter = AuthFilter.class;
        
        try {
            if(customAuthFilterClassName != null && "custom".equals(restAuthType)) {
                pickedAuthFilter = Class.forName(customAuthFilterClassName, false, JerseyWrapper.class.getClassLoader());
                loadingStatusMessage = null;
            }
        } catch (ClassNotFoundException e) {
            loadingStatusMessage = "No custom auth filter found for restAPI plugin! " + customAuthFilterClassName + " " + restAuthType;
        }
        
        register(pickedAuthFilter);
        return loadingStatusMessage;
    }
    
    /**
     * Instantiates a new jersey wrapper.
     */
    public JerseyWrapper(@Context ServletConfig servletConfig) {

        // Filters
        loadAuthenticationFilter();
        register(CORSFilter.class);
        register(StatisticsFilter.class);

        // Services
        registerClasses(
            ClusteringService.class,
            GroupService.class,
            MessageService.class,
            MsgArchiveService.class,
            MUCRoomAffiliationsService.class,
            MUCRoomService.class,
            MUCServiceService.class,
            SystemService.class,
            SecurityAuditLogService.class,
            SessionService.class,
            StatisticsService.class,
            UserGroupService.class,
            UserLockoutService.class,
            UserRosterService.class,
            UserService.class,
            UserServiceLegacy.class,
            UserVCardService.class
        );

        // Exception mapper
        register(RESTExceptionMapper.class);

        // Jackson's Object Mapper
        register(CustomJacksonMapperProvider.class);

        // Documentation (Swagger)
        register( new CustomOpenApiResource() );
    }
    
    /*
     * Returns the loading status message.
     *
     * @return the loading status message.
     */
    public static String getLoadingStatusMessage() {
        return loadingStatusMessage;
    }
    
}
