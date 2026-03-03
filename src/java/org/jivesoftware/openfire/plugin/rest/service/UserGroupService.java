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
import org.jivesoftware.openfire.plugin.rest.entity.UserGroupsEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/users/{username}/groups")
@Tag(name = "Users", description = "Managing Openfire users.")
public class UserGroupService {

    private UserServiceController plugin;

    @PostConstruct
    public void init() {
        plugin = UserServiceController.getInstance();
    }

    @GET
    @Operation( summary = "Get user's groups",
        description = "Retrieve names of all groups that a particular user is in.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The names of the groups that the user is in.", content = @Content(schema = @Schema(implementation = UserGroupsEntity.class))),
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public UserGroupsEntity getUserGroups(
            @Parameter(description = "The username for user for which to return group names.", required = true) @PathParam("username") String username)
        throws ServiceException
    {
        return new UserGroupsEntity(plugin.getUserGroups(username));
    }

    @POST
    @Operation( summary = "Add user to groups",
        description = "Add a particular user to a collection of groups. When a group that is provided does not exist, it will be automatically created if possible.",
        responses = {
            @ApiResponse(responseCode = "201", description = "The user was added to all groups."),
            @ApiResponse(responseCode = "400", description = "When the username cannot be parsed into a JID.")
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addUserToGroups(
            @Parameter(description = "The username of the user that is to be added to groups.", required = true) @PathParam("username") String username,
            @RequestBody(description = "A collection of names for groups that the user is to be added to.", required = true) UserGroupsEntity userGroupsEntity)
        throws ServiceException
    {
        plugin.addUserToGroups(username, userGroupsEntity);
        return Response.status(Response.Status.CREATED).build();
    }
    
    @POST
    @Path("/{groupName}")
    @Operation( summary = "Add user to group",
        description = "Add a particular user to a particular group. When the group that does not exist, it will be automatically created if possible.",
        responses = {
            @ApiResponse(responseCode = "201", description = "The user was added to the groups."),
            @ApiResponse(responseCode = "400", description = "When the username cannot be parsed into a JID.")
        })
    public Response addUserToGroup(
            @Parameter(description = "The username of the user that is to be added to a group.", required = true) @PathParam("username") String username,
            @Parameter(description = "The name of the group that the user is to be added to.", required = true) @PathParam("groupName") String groupName)
        throws ServiceException
    {
        plugin.addUserToGroup(username, groupName);
        return Response.status(Response.Status.CREATED).build();
    }
    
    @DELETE
    @Path("/{groupName}")
    @Operation( summary = "Delete user from group",
        description = "Removes a user from a group.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The user was taken out of the group."),
            @ApiResponse(responseCode = "404", description = "The group could not be found."),
        })
    public Response deleteUserFromGroup(
            @Parameter(description = "The username of the user that is to be removed from a group.", required = true) @PathParam("username") String username,
            @Parameter(description = "The name of the group that the user is to be removed from.", required = true) @PathParam("groupName") String groupName)
        throws ServiceException
    {
        plugin.deleteUserFromGroup(username, groupName);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Operation( summary = "Delete user from groups",
        description = "Removes a user from a collection of groups.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The user was taken out of the group."),
            @ApiResponse(responseCode = "404", description = "One or more groups could not be found."),
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteUserFromGroups(
            @Parameter(description = "The username of the user that is to be removed from a group.", required = true) @PathParam("username") String username,
            @RequestBody(description = "A collection of names for groups from which the user is to be removed.", required = true) UserGroupsEntity userGroupsEntity)
        throws ServiceException
    {
        plugin.deleteUserFromGroups(username, userGroupsEntity);
        return Response.status(Response.Status.OK).build();
    }
}
