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
import org.jivesoftware.openfire.plugin.rest.entity.OutcastEntities;
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

@Path("restapi/v1/chatrooms/{roomName}/outcasts")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomOutcastsService {

    @GET
    @Path("/")
    @Operation( summary = "All room outcasts",
        description = "Retrieves a list of JIDs for all outcasts of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Outcast list retrieved."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response getOutcasts(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room for which to return outcasts.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        final List<String> results = MUCRoomController.getInstance().getByAffiliation(serviceName, roomName, MUCRole.Affiliation.outcast).stream()
            .map(JID::toBareJID)
            .collect(Collectors.toList());
        return Response.ok(new OutcastEntities(results)).build();
    }

    @PUT
    @Path("/")
    @Operation( summary = "Replace room outcasts",
        description = "Replaces the room outcasts in a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room outcast, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Outcasts of the room have been replaced."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to modify a room outcasts.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response replaceMUCRoomOutcasts(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room of which outcasts are to be replaced.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @RequestBody(description = "The new list of room outcasts.", required = true) OutcastEntities outcastEntities)
        throws ServiceException
    {
        MUCRoomController.getInstance().replaceAffiliatedUsers(serviceName, roomName, MUCRole.Affiliation.outcast, outcastEntities.getOutcasts(), false);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/")
    @Operation( summary = "Add room outcasts",
        description = "Add multiple room outcasts in a multi-user chat room (without removing existing outcasts). Note that a user can only have one type of affiliation with a room. By adding a user as a room outcast, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Outcasts of the room have been added."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to modify a room outcasts.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addMUCRoomOutcasts(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which outcasts are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @RequestBody(description = "The list of room outcasts to add to the room.", required = true) OutcastEntities outcastEntities)
        throws ServiceException
    {
        MUCRoomController.getInstance().addAffiliatedUsers(serviceName, roomName, MUCRole.Affiliation.outcast, outcastEntities.getOutcasts(), false);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room outcast",
        description = "Marks a JID as outcast of a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room outcast, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "JID marked as outcast."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to add a room outcast.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Adding this JID as a room outcast would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomOutcast(
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The (bare) JID of the entity that is to be marked as an outcast.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC room in which the JID is outcast.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().addOutcast(serviceName, roomName, jid);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/group/{groupname}")
    @Operation( summary = "Add room outcasts",
        description = "Marks all members of an Openfire user group as outcasts of a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room outcast, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Group members marked as outcast."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to add a room outcast.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Adding this JID as a room outcast would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomOutcastGroup(
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the user group from which all members will be marked as outcast of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
            @Parameter(description = "The name of the MUC room in which the group members are outcast.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().addOutcast(serviceName, roomName, groupname);
        return Response.status(Status.CREATED).build();
    }

    @DELETE
    @Path("/{jid}")
    @Operation( summary = "Remove room outcast",
        description = "Unmarks a JID as outcast of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "JID no longer marked as outcast."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Applying this affiliation change would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomOutcast(
            @Parameter(description = "The (bare) JID of the entity that is to be removed as an outcast of the room.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the MUC room from which an outcast is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room outcasts",
        description = "Removes all members of an Openfire user group as outcast of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Group members no longer marked as outcast."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Applying this affiliation change would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomOutcastGroup(
            @Parameter(description = "The name of the user group from which all members will be removed as outcast of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the MUC room to which outcast are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
        return Response.status(Status.OK).build();
    }
}
