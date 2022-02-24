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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.SystemController;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperty;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.spi.ConnectionType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/system")
@Tag(name = "System", description = "Managing Openfire system configuration")
public class SystemService {

    @GET
    @Path("/properties")
    @Operation( summary = "Get system properties",
        description = "Get all Openfire system properties.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system properties.", content = @Content(schema = @Schema(implementation = SystemProperties.class))),
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SystemProperties getSystemProperties() {
        return SystemController.getInstance().getSystemProperties();
    }

    @GET
    @Path("/properties/{propertyKey}")
    @Operation( summary = "Get system property",
        description = "Get a specific Openfire system property.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The requested system property.", content = @Content(schema = @Schema(implementation = SystemProperty.class))),
            @ApiResponse(responseCode = "404", description = "The system property could not be found.")
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SystemProperty getSystemProperty(
            @Parameter(description = "The name of the system property to return.", example = "foo.bar.xyz", required = true) @PathParam("propertyKey") String propertyKey)
        throws ServiceException
    {
        return SystemController.getInstance().getSystemProperty(propertyKey);
    }

    @POST
    @Path("/properties")
    @Operation( summary = "Create system property",
        description = "Create a new Openfire system property. Will overwrite a pre-existing system property that uses the same name.",
        responses = {
            @ApiResponse(responseCode = "201", description = "The system property is created."),
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createSystemProperty(
            @RequestBody(description = "The system property to create.", required = true) SystemProperty systemProperty)
        throws ServiceException
    {
        SystemController.getInstance().createSystemProperty(systemProperty);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/properties/{propertyKey}")
    @Operation( summary = "Update system property",
        description = "Updates an existing Openfire system property.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system property is updated."),
            @ApiResponse(responseCode = "400", description = "The provided system property does not match the name in the URL."),
            @ApiResponse(responseCode = "404", description = "The system property could not be found.")
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateSystemProperty(
            @Parameter(description = "The name of the system property to update.", example = "foo.bar.xyz", required = true) @PathParam("propertyKey") String propertyKey,
            @RequestBody(description = "The new system property definition that replaced an existing definition.", required = true) SystemProperty systemProperty)
        throws ServiceException
    {
        SystemController.getInstance().updateSystemProperty(propertyKey, systemProperty);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/properties/{propertyKey}")
    @Operation( summary = "Remove system property",
        description = "Removes an existing Openfire system property.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system property is deleted."),
            @ApiResponse(responseCode = "404", description = "The system property could not be found.")
        })
    public Response deleteSystemProperty(
            @Parameter(description = "The name of the system property to delete.", example = "foo.bar.xyz", required = true) @PathParam("propertyKey") String propertyKey)
        throws ServiceException
    {
        SystemController.getInstance().deleteSystemProperty(propertyKey);
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/liveness")
    @Operation( summary = "Perform all liveness checks",
        description = "Detects if Openfire has reached a state that it cannot recover from, except for with a restart, based on every liveness check that it has implemented.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is live."),
            @ApiResponse(responseCode = "503", description = "At least one liveness check failed: the system is determined to not be alive.")
        })
    public Response liveness() {
        // Try to execute these in the order of resource usage.
        if (SystemController.getInstance().hasDeadlock()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        if (SystemController.getInstance().hasSystemPropertyRequiringRestart()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/liveness/deadlock")
    @Operation( summary = "Perform 'deadlock' liveness check.",
        description = "Detects if Openfire has reached a state that it cannot recover from because of a deadlock.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is live."),
            @ApiResponse(responseCode = "503", description = "A deadlock is detected.")
        })
    public Response livenessDeadlock() {
        // Try to execute these in the order of resource usage.
        if (SystemController.getInstance().hasDeadlock()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/liveness/properties")
    @Operation( summary = "Perform 'properties' liveness check.",
        description = "Detects if Openfire has reached a state that it cannot recover from because a system property change requires a restart to take effect.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is live."),
            @ApiResponse(responseCode = "503", description = "One or more system property changes that require a server restart have been detected.")
        })
    public Response livenessSystemProperties() {
        // Try to execute these in the order of resource usage.
        if (SystemController.getInstance().hasSystemPropertyRequiringRestart()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/readiness")
    @Operation( summary = "Perform all readiness checks",
        description = "Detects if Openfire is in a state where it is ready to process traffic, based on every readiness check that it has implemented.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is ready."),
            @ApiResponse(responseCode = "503", description = "At least one readiness check failed: the system is determined to not be able to process traffic.")
        })
    public Response readiness() {
        // Try to execute these in the order of resource usage.
        if (!SystemController.getInstance().isStarted()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        if (!SystemController.getInstance().hasClusteringStartedWhenEnabled()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        if (!SystemController.getInstance().hasPluginManagerExecuted()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        if (!SystemController.getInstance().areConnectionListenersStarted()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/readiness/server")
    @Operation( summary = "Perform 'server started' readiness check",
        description = "Detects if Openfire's core service has been started.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is ready."),
            @ApiResponse(responseCode = "503", description = "The Openfire service has not finished starting up yet.")
        })
    public Response readinessServerStart() {
        // Try to execute these in the order of resource usage.
        if (!SystemController.getInstance().isStarted()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/readiness/cluster")
    @Operation( summary = "Perform 'cluster' readiness check",
        description = "Detects if the cluster functionality has finished starting (or is disabled).",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is ready."),
            @ApiResponse(responseCode = "503", description = "Clustering functionality is enabled, but has not finished starting up yet.")
        })
    public Response readinessCluster() {
        // Try to execute these in the order of resource usage.
        if (!SystemController.getInstance().hasClusteringStartedWhenEnabled()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/readiness/plugins")
    @Operation( summary = "Perform 'plugins' readiness check",
        description = "Detects if Openfire has finished starting its plugins.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is ready."),
            @ApiResponse(responseCode = "503", description = "Plugins have not all been started yet.")
        })
    public Response readinessPlugins() {
        // Try to execute these in the order of resource usage.
        if (!SystemController.getInstance().hasPluginManagerExecuted()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/readiness/connections")
    @Operation( summary = "Perform 'connections' readiness check",
        description = "Detects if Openfire is ready to accept connection requests.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system is ready."),
            @ApiResponse(responseCode = "400", description = "The provided connectionType value is invalid."),
            @ApiResponse(responseCode = "503", description = "Openfire currently does not accept (all) connections.")
        })
    public Response readinessConnections(
        @Parameter(description = "Optional. Use to limit the check to one particular connection type. One of: SOCKET_S2S, SOCKET_C2S, BOSH_C2S, WEBADMIN, COMPONENT, CONNECTION_MANAGER", example = "SOCKET_C2S", required = false) @QueryParam("connectionType") String connectionType,
        @Parameter(description = "Check the encrypted (true) or unencrypted (false) variant of the connection type. Only used in combination with 'connectionType', as without it, all types and both encrypted and unencrypted are checked.", required = false) @QueryParam("encrypted") Boolean encrypted
    ) {
        if (connectionType != null && !connectionType.isEmpty()) {
            // Limit check to one connection listener.
            try {
                ConnectionType limitToType = ConnectionType.valueOf(connectionType);
                if (!SystemController.getInstance().isConnectionListenerStartedWhenEnabled(limitToType, encrypted)) {
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
                }
            } catch (Throwable t) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            // Check all connection listeners, encrypted and unencrypted.
            if (!SystemController.getInstance().areConnectionListenersStarted()) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
        }
        return Response.status(Response.Status.OK).build();
    }
}
