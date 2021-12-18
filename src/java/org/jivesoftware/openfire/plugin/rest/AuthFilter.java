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
    private RESTServicePlugin plugin = (RESTServicePlugin) XMPPServer.getInstance().getPluginManager()
            .getPlugin("restapi");

    @Override
    public void filter(ContainerRequestContext containerRequest) throws IOException {
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
                LOG.warn("Wrong HTTP Basic Auth authorization", e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } catch (ConnectionException e) {
                LOG.error("Authentication went wrong", e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            } catch (InternalUnauthenticatedException e) {
                LOG.error("Authentication went wrong", e);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        } else {
            if (!auth.equals(plugin.getSecret())) {
                LOG.warn("Wrong secret key authorization. Provided key: " + auth);
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
        }
    }
}
