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

package org.jivesoftware.openfire.plugin.rest;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;

/**
 * The Class AuthFilter.
 */
@PreMatching
@Priority(Priorities.AUTHORIZATION)
public class AuthFilter implements ContainerRequestFilter {

    /** The log. */
    private static Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

    /** The http request. */
    @Context
    private HttpServletRequest httpRequest;

    /** The plugin. */
    private RESTServicePlugin plugin = (RESTServicePlugin) XMPPServer.getInstance().getPluginManager()
            .getPlugin("restapi");

    public static final String SHARED_SECRET_AUTHENTICATION_SCHEME = "SharedSecret";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (!plugin.isEnabled()) {
            LOG.debug("REST API Plugin is not enabled");
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        if (!authRequired(requestContext)){
            return;
        }

        if (!plugin.getAllowedIPs().isEmpty()) {
            // Get client's IP address
            String ipAddress = getClientIPAddressForRequest(httpRequest);
            if (!plugin.getAllowedIPs().contains(ipAddress)) {
                LOG.warn("REST API rejected service for IP address: " + ipAddress);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        }
        
        // Get the authentication passed in HTTP headers parameters
        String auth = requestContext.getHeaderString("authorization");

        if (auth == null) {
            LOG.warn("REST API request with no Authorization header rejected. [Request IP: {}, Request URI: {}]",
                    getClientIPAddressForRequest(httpRequest), requestContext.getUriInfo().getRequestUri().getPath());
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        // HTTP Basic Auth or Shared Secret key
        if ("basic".equals(plugin.getHttpAuth())) {
            String[] usernameAndPassword = BasicAuth.decode(auth);

            // If username or password fail
            if (usernameAndPassword == null || usernameAndPassword.length != 2) {
                LOG.warn("Basic authentication failed. Username or password is not set. [Request IP: {}, Request URI: {}]",
                    getClientIPAddressForRequest(httpRequest), requestContext.getUriInfo().getRequestUri().getPath());
                throw new WebApplicationException("Username or password is not set", Status.UNAUTHORIZED);
            }

            boolean userAdmin = AdminManager.getInstance().isUserAdmin(usernameAndPassword[0], true);

            if (!userAdmin) {
                LOG.warn("Provided User is not an admin");
                throw new WebApplicationException("User is not authorised", Status.UNAUTHORIZED);
            }

            try {
                AuthFactory.authenticate(usernameAndPassword[0], usernameAndPassword[1]);
                setSecurityForContext(requestContext, usernameAndPassword[0], SecurityContext.BASIC_AUTH);
                if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
                    LOG.info("Authentication - successfully authenticated user. [Request IP: {}, Request URI: {}, Username: {}]",
                        getClientIPAddressForRequest(httpRequest), requestContext.getUriInfo().getRequestUri().getPath(), usernameAndPassword[0]);
                }
            } catch (UnauthorizedException e) {
                LOG.warn("Basic authentication failed. Username or password is incorrect. [Request IP: {}, Request URI: {}]",
                    getClientIPAddressForRequest(httpRequest), requestContext.getUriInfo().getRequestUri().getPath());
                LOG.warn("Authentication error", e);
                throw new WebApplicationException("Username or password is incorrect", Status.UNAUTHORIZED);
            } catch (ConnectionException | InternalUnauthenticatedException e) {
                LOG.error("Authentication went wrong", e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        } else {
            if (!auth.equals(plugin.getSecret())) {
                LOG.warn("Wrong secret key authorization. [Request IP: {}, Request URI: {}, Request auth: {}]",
                    getClientIPAddressForRequest(httpRequest), requestContext.getUriInfo().getRequestUri().getPath(), auth);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } else {
                //For shared secret, use the authentication scheme to indicate that a username is unknown
                setSecurityForContext(requestContext, SHARED_SECRET_AUTHENTICATION_SCHEME, SHARED_SECRET_AUTHENTICATION_SCHEME);
                if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
                    LOG.info("Authentication - successfully authenticated by secret key. [Request IP: {}, Request URI: {}]",
                        getClientIPAddressForRequest(httpRequest), requestContext.getUriInfo().getRequestUri().getPath());
                }
            }
        }
    }

    private boolean authRequired(ContainerRequestContext requestContext){
        if (requestContext.getUriInfo().getRequestUri().getPath().equals("/plugins/restapi/v1/openapi.yaml")) {
            LOG.debug("Authentication was bypassed for openapi.yaml file (documentation)");
            return false;
        }

        if (isStatusEndpoint(requestContext.getUriInfo().getRequestUri().getPath())) {
            LOG.debug("Authentication was bypassed for a status endpoint");
            return false;
        }

        // Let the preflight request through the authentication
        if ("OPTIONS".equals(requestContext.getMethod())) {
            LOG.debug("Authentication was bypassed because of OPTIONS request");
            return false;
        }

        // To be backwards compatible to userservice 1.*
        if (requestContext.getUriInfo().getRequestUri().getPath().contains("restapi/v1/userservice")) {
            LOG.info("Deprecated 'userservice' endpoint was used. Please switch to the new endpoints");
            return false;
        }

        return true;
    }

    private void setSecurityForContext(ContainerRequestContext requestContext, String username, String authScheme){
        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return () -> username;
            }

            @Override
            public boolean isUserInRole(String role) {
                return true;
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return authScheme;
            }
        });
    }

    private boolean isStatusEndpoint(String path){
        return path.equals("/plugins/restapi/v1/system/liveness") ||
            path.startsWith("/plugins/restapi/v1/system/liveness/") ||
            path.equals("/plugins/restapi/v1/system/readiness") ||
            path.startsWith("/plugins/restapi/v1/system/readiness/");
    }

    private String getClientIPAddressForRequest(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null) {
            ipAddress = request.getHeader("X_FORWARDED_FOR");
            if (ipAddress == null) {
                ipAddress = request.getHeader("X-Forward-For");
                if (ipAddress == null) {
                    ipAddress = request.getRemoteAddr();
                }
            }
        }
        return ipAddress;
    }
}
