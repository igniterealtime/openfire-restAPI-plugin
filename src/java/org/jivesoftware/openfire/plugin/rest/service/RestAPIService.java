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
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperty;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/system/properties")
@Tag(name = "System", description = "Managing Openfire system configuration")
public class RestAPIService {

    private RESTServicePlugin plugin;

    @PostConstruct
    public void init() {
        plugin = RESTServicePlugin.getInstance();
    }

    @GET
    @Operation( summary = "Get system properties",
        description = "Get all Openfire system properties.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The system properties.", content = @Content(schema = @Schema(implementation = SystemProperties.class))),
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SystemProperties getSystemProperties() {
        return plugin.getSystemProperties();
    }

    @GET
    @Path("/{propertyKey}")
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
        return plugin.getSystemProperty(propertyKey);
    }

    @POST
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
        plugin.createSystemProperty(systemProperty);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{propertyKey}")
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
        plugin.updateSystemProperty(propertyKey, systemProperty);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{propertyKey}")
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
        plugin.deleteSystemProperty(propertyKey);
        return Response.status(Response.Status.OK).build();
    }
}
