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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("restapi/v1/chatrooms/{roomName}/owners")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomOwnersService {

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room owner",
        description = "Adds an owner to a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Owner added to the room.")
        })
    public Response addMUCRoomOwner(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The (bare) JID of the entity that is to be added as an owner.", example = "john@example.org", required = true) @PathParam("jid") String jid,
        @Parameter(description = "The name of the MUC room to which an owner is to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().addOwner(serviceName, roomName, jid);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/group/{groupname}")
    @Operation( summary = "Add room owners",
        description = "Adds all members of an Openfire user group as owners to a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Owners added to the room.")
        })
    public Response addMUCRoomOwnerGroup(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the user group from which all members will be owners of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC room to which owners are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().addOwner(serviceName, roomName, groupname);
        return Response.status(Status.CREATED).build();
    }

    @DELETE
    @Path("/{jid}")
    @Operation( summary = "Remove room owner",
        description = "Removes a user as an owner of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Owner removed from the room."),
            @ApiResponse(responseCode = "409", description = "When removal of this owner would leave the room without any owners (which is not allowed: a room must always have an owner).")
        })
    public Response deleteMUCRoomOwner(
        @Parameter(description = "The (bare) JID of the entity that is to be removed as an owner of the room.", example = "john@example.org", required = true) @PathParam("jid") String jid,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room from which an owner is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        // FIXME: check if this removes _all_ affiliations, which probably would be more than we're bargaining for.
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room owners",
        description = "Removes all members of an Openfire user group as owner of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Owners removed from the room."),
            @ApiResponse(responseCode = "409", description = "When removal of these owners would leave the room without any owners (which is not allowed: a room must always have an owner).")
        })
    public Response deleteMUCRoomOwnerGroup(
        @Parameter(description = "The name of the user group from which all members will be removed as owners of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which owners are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        // FIXME: check if this removes _all_ affiliations, which probably would be more than we're bargaining for.
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
        return Response.status(Status.OK).build();
    }
}
