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
import org.jivesoftware.openfire.plugin.rest.controller.GroupController;
import org.jivesoftware.openfire.plugin.rest.entity.GroupEntities;
import org.jivesoftware.openfire.plugin.rest.entity.GroupEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/groups")
@Tag(name="User Group", description = "Managing Openfire user groupings.")
public class GroupService {

    private GroupController groupController;

    @PostConstruct
    public void init() {
        groupController = GroupController.getInstance();
    }

    @GET
    @Operation( summary = "Get groups",
                description = "Get a list of all user groups.",
                responses = {
                    @ApiResponse(responseCode = "200", description = "All groups", content = @Content(schema = @Schema(implementation = GroupEntities.class)))
                })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public GroupEntities getGroups() throws ServiceException
    {
        return new GroupEntities(groupController.getGroups());
    }

    @POST
    @Operation( summary = "Create group",
        description = "Create a new user group.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Group created."),
            @ApiResponse(responseCode = "400", description = "Group or group name missing, or invalid syntax for a property."),
            @ApiResponse(responseCode = "409", description = "Group already exists.")
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createGroup(
            @RequestBody(description = "The group that needs to be created.", required = true) GroupEntity groupEntity)
        throws ServiceException
    {
        groupController.createGroup(groupEntity);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{groupName}")
    @Operation( summary = "Get group",
        description = "Get one specific user group by name.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The group.", content = @Content(schema = @Schema(implementation = GroupEntity.class))),
            @ApiResponse(responseCode = "404", description = "Group with this name not found.")
        })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public GroupEntity getGroup(@Parameter(description = "The name of the group that needs to be fetched.", example = "Colleagues", required = true) @PathParam("groupName") String groupName)
        throws ServiceException
    {
        return groupController.getGroup(groupName);
    }

    @PUT
    @Path("/{groupName}")
    @Operation( summary = "Update group",
        description = "Updates / overwrites an existing user group. Note that the name of the group cannot be changed.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Group updated."),
            @ApiResponse(responseCode = "400", description = "Group or group name missing, or name does not match existing group, or invalid syntax for a property."),
            @ApiResponse(responseCode = "404", description = "Group with this name not found."),
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateGroup(@Parameter(description = "The name of the group that needs to be fetched.", example = "Colleagues", required = true) @PathParam("groupName") String groupName,
                                @RequestBody(description = "The new group definition that needs to overwrite the old definition.", required = true) GroupEntity groupEntity )
        throws ServiceException
    {
        groupController.updateGroup(groupName, groupEntity);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{groupName}")
    @Operation( summary = "Delete group",
        description = "Removes an existing user group.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Group deleted."),
            @ApiResponse(responseCode = "400", description = "Group not found.")
        })
    public Response deleteGroup(@Parameter(description = "The name of the group that needs to be removed.", example = "Colleagues", required = true) @PathParam("groupName") String groupName)
        throws ServiceException
    {
        groupController.deleteGroup(groupName);
        return Response.status(Response.Status.OK).build();
    }
}
