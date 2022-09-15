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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ErrorResponse;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.xmpp.packet.JID;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("restapi/v1/chatrooms")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomService {

    @GET
    @Operation( summary = "Get chat rooms",
        description = "Get a list of all multi-user chat rooms of a particular chat room service.",
        responses = {
            @ApiResponse(responseCode = "200", description = "All chat rooms", content = @Content(schema = @Schema(implementation = MUCRoomEntities.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "MUC service does not exist or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomEntities getMUCRooms(
            @Parameter(description = "The name of the MUC service for which to return all chat rooms.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "Room type-based filter: 'all' or 'public'", examples = { @ExampleObject(value = "public", description = "Only return rooms configured with 'List Room in Directory'"), @ExampleObject(value = "all", description = "Return all rooms")}, required = false) @DefaultValue(MUCChannelType.PUBLIC) @QueryParam("type") String channelType,
            @Parameter(description = "Search/Filter by room name.\nThis act like the wildcard search %String%", example = "conference", required = false) @QueryParam("search") String roomSearch,
            @Parameter(description = "For all groups defined in owners, admins, members and outcasts, list individual members instead of the group name.", required = false) @DefaultValue("false") @QueryParam("expandGroups") Boolean expand)
        throws ServiceException
    {
        return MUCRoomController.getInstance().getChatRooms(serviceName, channelType, roomSearch, expand);
    }

    @GET
    @Path("/{roomName}")
    @Operation( summary = "Get chat room",
        description = "Get information of a specific multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The chat room", content = @Content(schema = @Schema(implementation = MUCRoomEntity.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomEntity getMUCRoomJSON2(
            @Parameter(description = "The name of the MUC room to return.", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The name of the MUC service for which to return a chat room.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "For all groups defined in owners, admins, members and outcasts, list individual members instead of the group name.", required = false) @DefaultValue("false") @QueryParam("expandGroups") Boolean expand)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        return MUCRoomController.getInstance().getChatRoom(roomName, serviceName, expand);
    }

    @Operation( summary = "Delete chat room",
        description = "Removes an existing multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Room deleted."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @DELETE
    @Path("/{roomName}")
    public Response deleteMUCRoom(
            @Parameter(description = "The name of the MUC room to delete.", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The name of the MUC service from which to delete a chat room.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        MUCRoomController.getInstance().deleteChatRoom(roomName, serviceName);
        return Response.status(Status.OK).build();
    }

    @POST
    @Operation( summary = "Create chat room",
        description = "Create a new multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Room created."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Room creation is not permitted.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "MUC Service does not exist or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Room already exists, or another conflict occurred while creating the room.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createMUCRoom(
            @Parameter(description = "The name of the MUC service in which to create a chat room.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "Whether to send invitations to affiliated users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
            @RequestBody(description = "The MUC room that needs to be created.", required = true) MUCRoomEntity mucRoomEntity)
        throws ServiceException
    {
        MUCRoomController.getInstance().createChatRoom(serviceName, mucRoomEntity, sendInvitations);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/bulk")
    @Operation( summary = "Create multiple chat rooms",
        description = "Create a number of new multi-user chat rooms.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Request has been processed. Results are reported in the response.", content = @Content(schema = @Schema(implementation = RoomCreationResultEntities.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "MUC Service does not exist or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RoomCreationResultEntities createMUCRooms(
        @Parameter(description = "The name of the MUC service in which to create a chat room.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "Whether to send invitations to newly affiliated users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
        @RequestBody(description = "The MUC rooms that need to be created.", required = true) MUCRoomEntities mucRoomEntities)
        throws ServiceException
    {
        return MUCRoomController.getInstance().createMultipleChatRooms(serviceName, mucRoomEntities, sendInvitations);
    }

    @PUT
    @Path("/{roomName}")
    @Operation( summary = "Update chat room",
        description = "Updates an existing multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Room updated."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Room update/create is not permitted.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "MUC Service does not exist or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "This update causes a conflict, possibly with another existing room.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateMUCRoom(
            @Parameter(description = "The name of the chat room that needs to be updated", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The name of the MUC service in which to update a chat room.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "Whether to send invitations to newly affiliated users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
            @RequestBody(description = "The new MUC room definition that needs to overwrite the old definition.", required = true) MUCRoomEntity mucRoomEntity)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        MUCRoomController.getInstance().updateChatRoom(roomName, serviceName, mucRoomEntity, sendInvitations);
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("/{roomName}/participants")
    @Operation( summary = "Get room participants",
        description = "Get all participants of a specific multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The chat room participants", content = @Content(schema = @Schema(implementation = ParticipantEntities.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ParticipantEntities getMUCRoomParticipants(
            @Parameter(description = "The name of the chat room for which to return participants", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The name of the chat room's MUC service.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        return MUCRoomController.getInstance().getRoomParticipants(roomName, serviceName);
    }

    @GET
    @Path("/{roomName}/occupants")
    @Operation( summary = "Get room occupants",
        description = "Get all occupants of a specific multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The chat room participants", content = @Content(schema = @Schema(implementation = OccupantEntities.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OccupantEntities getMUCRoomOccupants(
            @Parameter(description = "The name of the chat room for which to return occupants", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The name of the chat room's MUC service.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        return MUCRoomController.getInstance().getRoomOccupants(roomName, serviceName);
    }

    @GET
    @Path("/{roomName}/chathistory")
    @Operation( summary = "Get room history",
        description = "Get messages that have been exchanged in a specific multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The chat room message history", content = @Content(schema = @Schema(implementation = MUCRoomMessageEntities.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCRoomMessageEntities getMUCRoomHistory(
            @Parameter(description = "The name of the chat room for which to return message history", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The name of the chat room's MUC service.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        return MUCRoomController.getInstance().getRoomHistory(roomName, serviceName);
    }

    @POST
    @Path("/{roomName}/invite/{jid}")
    @Operation( summary = "Invite user or group",
        description = "Invites a user or group to join a specific multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Invitation sent"),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to invite a user to this room.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response inviteUserOrGroupToMUCRoom(
            @Parameter(description = "The name of the chat room in which to invite a user or group", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "The JID of the entity to invite into the room", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the chat room's MUC service.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @RequestBody(description = "The invitation message to send and whom to send it to.", required = true) MUCInvitationEntity mucInvitationEntity)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final MUCInvitationsEntity multiple = new MUCInvitationsEntity();
        multiple.setReason(mucInvitationEntity.getReason());
        if (!multiple.getJidsToInvite().contains(jid)) {
            multiple.getJidsToInvite().add(jid);
        }
        MUCRoomController.getInstance().inviteUsersAndOrGroups(serviceName, roomName, multiple);
        return Response.status(Status.OK).build();
    }

    @POST
    @Path("/{roomName}/invite")
    @Operation( summary = "Invite a collection of users and/or groups",
        description = "Invites a collection of users and/or groups to join a specific multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Invitation sent"),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to invite a user or group to this room.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response inviteUsersAndOrGroupsToMUCRoom(
        @Parameter(description = "The name of the chat room in which to invite a user or group", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "The name of the chat room's MUC service.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @RequestBody(description = "The invitation message to send and whom to send it to.", required = true) MUCInvitationsEntity mucInvitationsEntity)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        MUCRoomController.getInstance().inviteUsersAndOrGroups(serviceName, roomName, mucInvitationsEntity);
        return Response.status(Status.OK).build();
    }

}
