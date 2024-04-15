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
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntities;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/users")
@Tag(name = "Users", description = "Managing Openfire users.")
public class UserService {

    private UserServiceController plugin;

    @PostConstruct
    public void init() {
        plugin = UserServiceController.getInstance();
    }

    @GET
    @Operation( summary = "Get users",
        description = "Retrieve all users defined in Openfire (with optional filtering).",
        responses = {
            @ApiResponse(responseCode = "200", description = "A list of Openfire users.", content = @Content(schema = @Schema(implementation = UserEntities.class))),
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserEntities getUsers(
            @Parameter(description = "Search/Filter by username. This act like the wildcard search %String%", required = false) @QueryParam("search") String userSearch,
            @Parameter(description = "Filter by a user property name.", required = false) @QueryParam("propertyKey") String propertyKey,
            @Parameter(description = "Filter by user property value. Note: This can only be used in combination with a property name parameter", required = false) @QueryParam("propertyValue") String propertyValue)
        throws ServiceException
    {
        return plugin.getUserEntities(userSearch, propertyKey, propertyValue);
    }

    @POST
    @Operation( summary = "Create user",
        description = "Add a new user to Openfire.",
        responses = {
            @ApiResponse(responseCode = "201", description = "The user was created."),
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createUser(
            @RequestBody(description = "The definition of the user to create.", required = true) UserEntity userEntity)
        throws ServiceException
    {
        plugin.createUser(userEntity);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{username}")
    @Operation( summary = "Get user",
        description = "Retrieve a user that is defined in Openfire.",
        responses = {
            @ApiResponse(responseCode = "200", description = "A list of Openfire users.", content = @Content(schema = @Schema(implementation = UserEntity.class))),
            @ApiResponse(responseCode = "404", description = "No user with that username was found."),
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserEntity getUser(
            @Parameter(description = "The username of the user to return.", required = true) @PathParam("username") String username)
        throws ServiceException
    {
        return plugin.getUserEntity(username);
    }

    @PUT
    @Path("/{username}")
    @Operation( summary = "Update user",
        description = "Update an existing user in Openfire.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The user was updated."),
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateUser(
            @Parameter(description = "The username of the user to update.", required = true) @PathParam("username") String username,
            @RequestBody(description = "The definition update of the user.", required = true) UserEntity userEntity)
        throws ServiceException
    {
        plugin.updateUser(username, userEntity);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{username}")
    @Operation( summary = "Delete user",
        description = "Remove an existing user from Openfire.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The user was removed."),
            @ApiResponse(responseCode = "404", description = "No user with that username was found."),
            })
    public Response deleteUser(@Parameter(description = "The username of the user to remove.", required = true) @PathParam("username") String username) throws ServiceException {
        plugin.deleteUser(username);
        return Response.status(Response.Status.OK).build();
    }
}
