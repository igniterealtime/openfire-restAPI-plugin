/*
 * Copyright (C) 2022-2026 Ignite Realtime Foundation. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import org.glassfish.jersey.server.ResourceConfig;
import org.jivesoftware.openfire.plugin.rest.AuthFilter;
import org.jivesoftware.openfire.plugin.rest.CORSFilter;
import org.jivesoftware.openfire.plugin.rest.CustomJacksonMapperProvider;
import org.jivesoftware.openfire.plugin.rest.StatisticsFilter;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.util.JiveGlobals;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.servlet.ServletConfig;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The Class JerseyWrapper.
 */
public class JerseyWrapper extends ResourceConfig {

    private static final org.slf4j.Logger Log = org.slf4j.LoggerFactory.getLogger(JerseyWrapper.class);

    /**
     * A collection of classes that are acceptable for use as a custom REST API authentication implementation.
     */
    public static final Set<Class<?>> CUSTOM_AUTH_ACCEPTABLE_CONTRACT_SHAPES = Set.of(
        ContainerRequestFilter.class, // Implementations are expected to be a ContainerRequestFilter
        Feature.class,                // Theoretically, Jersey can use this too - allowing/keeping this for backwards compatibility.
        DynamicFeature.class          // Theoretically, Jersey can use this too - allowing/keeping this for backwards compatibility.
    );

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

    public static String validateCustomAuthFilterClassName(String customAuthFilterClassName)
    {
        if (customAuthFilterClassName == null || customAuthFilterClassName.isEmpty()) {
            loadingStatusMessage = "Classname field can't be empty!";
        } else {
            try {
                getCustomAuthFilterClassObject(customAuthFilterClassName);
                loadingStatusMessage = null;
            } catch (IllegalArgumentException e) {
                loadingStatusMessage = "Unable to use the custom auth filter class name '" + customAuthFilterClassName + "': " + e.getMessage();
            }
        }

        return loadingStatusMessage;
    }
    
    public String loadAuthenticationFilter()
    {
        // Check if custom AuthFilter is available
        String customAuthFilterClassName = JiveGlobals.getProperty(CUSTOM_AUTH_PROPERTY_NAME);
        String restAuthType = JiveGlobals.getProperty(REST_AUTH_TYPE);
        Class<?> pickedAuthFilter = AuthFilter.class;
        
        try {
            if(customAuthFilterClassName != null && "custom".equals(restAuthType)) {
                pickedAuthFilter = getCustomAuthFilterClassObject(customAuthFilterClassName);
                loadingStatusMessage = null;
            }
        } catch (IllegalArgumentException e) {
            loadingStatusMessage = "Unable to use the custom auth filter class name '" + customAuthFilterClassName + "': " + e.getMessage();
            Log.error("Unable to load custom auth filter: {}", customAuthFilterClassName, e);
        }
        
        register(pickedAuthFilter);
        return loadingStatusMessage;
    }

    /**
     * Resolves the given class name and validates that it is suitable for use as a custom REST API authentication
     * filter.
     *
     * To be accepted, the resolved class must satisfy both of the following:
     * <ul>
     *     <li>It must implement one of {@link ContainerRequestFilter}, {@link Feature}, or {@link DynamicFeature}.
     *     {@link ContainerRequestFilter} is the expected, documented contract; {@link Feature} and
     *     {@link DynamicFeature} are accepted only for backwards compatibility with any implementation that wires
     *     up its filter indirectly through one of these.</li>
     *     <li>It must be annotated with {@code @}{@link Priority}{@code (}{@link Priorities#AUTHENTICATION}{@code )},
     *     signaling that the class is specifically intended to perform authentication. This is a necessary, but not
     *     sufficient, safeguard: it prevents an incidental class (e.g. a logging or metrics filter) from being
     *     mistaken for an authenticator, but it cannot verify that the class actually rejects unauthenticated
     *     requests.</li>
     * </ul>
     * This method does not instantiate or invoke the resolved class; it only inspects its type and annotations.
     *
     * @param className the fully qualified name of the class to resolve and validate. Must not be {@code null}.
     * @return the resolved, validated class. Never {@code null}.
     * @throws IllegalArgumentException if the class cannot be resolved on the classpath, or if it is resolved but
     *         does not satisfy both validation requirements above. The exception message identifies the specific
     *         reason for rejection.
     */
    @VisibleForTesting
    static Class<?> getCustomAuthFilterClassObject(@Nonnull final String className) throws IllegalArgumentException
    {
        final Class<?> candidate;
        try {
            candidate = Class.forName(className, false, JerseyWrapper.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className, e);
        }

        if (CUSTOM_AUTH_ACCEPTABLE_CONTRACT_SHAPES.stream().noneMatch(c -> c.isAssignableFrom(candidate))) {
            throw new IllegalArgumentException("Class " + className + " does not implement an acceptable contract shape (one of " + CUSTOM_AUTH_ACCEPTABLE_CONTRACT_SHAPES.stream().map(Class::getName).collect(Collectors.joining(", ")) + ").");
        }

        final Priority priority = candidate.getAnnotation(Priority.class);
        if (priority == null || priority.value() != Priorities.AUTHENTICATION) {
            throw new IllegalArgumentException("Class " + className + " is not a valid authentication filter (it lacks the @javax.annotation.Priority(javax.ws.rs.Priorities.AUTHENTICATION) annotation).");
        }

        return candidate;
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
