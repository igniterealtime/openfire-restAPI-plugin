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
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.OwnerEntities;
import org.jivesoftware.openfire.plugin.rest.exceptions.ErrorResponse;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.xmpp.packet.JID;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.stream.Collectors;

@Path("restapi/v1/chatrooms/{roomName}/owners")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomOwnersService {

    @GET
    @Path("/")
    @Operation( summary = "All room owners",
        description = "Retrieves a list of JIDs for all owners of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Owner list retrieved."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response getOwners(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room for which to return owners.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        final List<String> results = MUCRoomController.getInstance().getByAffiliation(serviceName, roomName, MUCRole.Affiliation.owner).stream()
            .map(JID::toBareJID)
            .collect(Collectors.toList());
        return Response.ok(new OwnerEntities(results)).build();
    }

    @PUT
    @Path("/")
    @Operation( summary = "Replace room owners",
        description = "Replaces the room owners in a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room owner, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Owners of the room have been replaced."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to modify a room owners.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response replaceMUCRoomOwners(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room of which owners are to be replaced.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "Whether to send invitations to newly affiliated users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
        @RequestBody(description = "The new list of room owners.", required = true) OwnerEntities ownerEntities)
        throws ServiceException
    {
        MUCRoomController.getInstance().replaceAffiliatedUsers(serviceName, roomName, MUCRole.Affiliation.owner, ownerEntities.getOwners(), sendInvitations);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/")
    @Operation( summary = "Add room owners",
        description = "Add multiple room owners in a multi-user chat room (without removing existing owners). Note that a user can only have one type of affiliation with a room. By adding a user as a room owner, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Owners of the room have been added."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to modify a room owners.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addMUCRoomOwners(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which owners are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "Whether to send invitations to new owners.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
        @RequestBody(description = "The list of room owners to add to the room.", required = true) OwnerEntities ownerEntities)
        throws ServiceException
    {
        MUCRoomController.getInstance().addAffiliatedUsers(serviceName, roomName, MUCRole.Affiliation.owner, ownerEntities.getOwners(), sendInvitations);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room owner",
        description = "Adds an owner to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room owner, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Owner added to the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to add a room owner.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomOwner(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The (bare) JID of the entity that is to be added as an owner.", example = "john@example.org", required = true) @PathParam("jid") String jid,
        @Parameter(description = "The name of the MUC room to which an owner is to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "Whether to send invitation to new owner.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations)
        throws ServiceException
    {
        MUCRoomController.getInstance().addOwner(serviceName, roomName, jid, sendInvitations);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/group/{groupname}")
    @Operation( summary = "Add room owners",
        description = "Adds all members of an Openfire user group as owners to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room owner, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Owners added to the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to add a room owner.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomOwnerGroup(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the user group from which all members will be owners of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC room to which owners are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "Whether to send invitations to new owners.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations)
        throws ServiceException
    {
        MUCRoomController.getInstance().addOwner(serviceName, roomName, groupname, sendInvitations);
        return Response.status(Status.CREATED).build();
    }

    @DELETE
    @Path("/{jid}")
    @Operation( summary = "Remove room owner",
        description = "Removes a user as an owner of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Owner removed from the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "When removal of this owner would leave the room without any owners (which is not allowed: a room must always have an owner).", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomOwner(
        @Parameter(description = "The (bare) JID of the entity that is to be removed as an owner of the room.", example = "john@example.org", required = true) @PathParam("jid") String jid,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room from which an owner is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room owners",
        description = "Removes all members of an Openfire user group as owner of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Owners removed from the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "When removal of these owners would leave the room without any owners (which is not allowed: a room must always have an owner).", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomOwnerGroup(
        @Parameter(description = "The name of the user group from which all members will be removed as owners of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which owners are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
        return Response.status(Status.OK).build();
    }
}
