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

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    private RESTServicePlugin plugin = (RESTServicePlugin) XMPPServer.getInstance().getPluginManager().getPluginByName("REST API").orElse(null);

    @Override
    public void filter(ContainerRequestContext containerRequest) throws IOException {
        if (containerRequest.getUriInfo().getRequestUri().getPath().equals("/plugins/restapi/v1/openapi.yaml")) {
            LOG.debug("Authentication was bypassed for openapi.yaml file (documentation)");
            return;
        }

        if (isStatusEndpoint(containerRequest.getUriInfo().getRequestUri().getPath())) {
            LOG.debug("Authentication was bypassed for a status endpoint");
            return;
        }

        if (!plugin.isEnabled()) {
            LOG.debug("REST API Plugin is not enabled");
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        
        // Let the preflight request through the authentication
        if ("OPTIONS".equals(containerRequest.getMethod())) {
            LOG.debug("Authentication was bypassed because of OPTIONS request");
            return;
        }
        
        // To be backwards compatible to userservice 1.*
        if (containerRequest.getUriInfo().getRequestUri().getPath().contains("restapi/v1/userservice")) {
            LOG.info("Deprecated 'userservice' endpoint was used. Please switch to the new endpoints");
            return;
        }

        if (!plugin.getAllowedIPs().isEmpty()) {
            // Get client's IP address
            String ipAddress = httpRequest.getHeader("x-forwarded-for");
            if (ipAddress == null) {
                ipAddress = httpRequest.getHeader("X_FORWARDED_FOR");
                if (ipAddress == null) {
                    ipAddress = httpRequest.getHeader("X-Forward-For");
                    if (ipAddress == null) {
                        ipAddress = httpRequest.getRemoteAddr();
                    }
                }
            }
            if (!plugin.getAllowedIPs().contains(ipAddress)) {
                LOG.warn("REST API rejected service for IP address: " + ipAddress);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        }
        
        // Get the authentication passed in HTTP headers parameters
        String auth = containerRequest.getHeaderString("authorization");

        if (auth == null) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }

        // HTTP Basic Auth or Shared Secret key
        if ("basic".equals(plugin.getHttpAuth())) {
            String[] usernameAndPassword = BasicAuth.decode(auth);

            // If username or password fail
            if (usernameAndPassword == null || usernameAndPassword.length != 2) {
                LOG.warn("Username or password is not set");
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }

            boolean userAdmin = AdminManager.getInstance().isUserAdmin(usernameAndPassword[0], true);

            if (!userAdmin) {
                LOG.warn("Provided User is not an admin");
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }

            try {
                AuthFactory.authenticate(usernameAndPassword[0], usernameAndPassword[1]);
            } catch (UnauthorizedException e) {
                LOG.info("Authentication for '{}' failed: incorrect credentials provided.", usernameAndPassword[0], e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } catch (ConnectionException e) {
                LOG.error("Authentication for '{}' failed: Openfire is not able to connect to the back-end users/group system.", usernameAndPassword[0], e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } catch (InternalUnauthenticatedException e) {
                LOG.error("Authentication for '{}' failed: Openfire is not able to authenticate itself to the back-end users/group system.", usernameAndPassword[0], e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        } else {
            if (!auth.equals(plugin.getSecret())) {
                LOG.warn("Wrong secret key authorization. Provided key: " + auth);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        }
    }

    private boolean isStatusEndpoint(String path){
        return path.equals("/plugins/restapi/v1/system/liveness") ||
            path.startsWith("/plugins/restapi/v1/system/liveness/") ||
            path.equals("/plugins/restapi/v1/system/readiness") ||
            path.startsWith("/plugins/restapi/v1/system/readiness/");
    }
}
