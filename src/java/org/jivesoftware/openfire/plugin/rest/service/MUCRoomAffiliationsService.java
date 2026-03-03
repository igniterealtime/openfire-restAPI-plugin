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
import org.jivesoftware.openfire.muc.Affiliation;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ErrorResponse;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.utils.MUCRoomUtils;
import org.xmpp.packet.JID;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.stream.Collectors;

@Path("restapi/v1/chatrooms/{roomName}/{affiliation: (admins|members|outcasts|owners)}")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomAffiliationsService
{

    @GET
    @Path("/")
    @Operation( summary = "All room affiliations",
        description = "Retrieves a list of JIDs for all affiliated users of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Affiliated user list retrieved."),
            @ApiResponse(responseCode = "400", description = "Provided 'affiliations' value is invalid."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response getAffiliations(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room for which to return affiliations.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        final List<String> results = MUCRoomController.getInstance().getByAffiliation(serviceName, roomName, affiliation).stream()
            .map(JID::toBareJID)
            .collect(Collectors.toList());

        final AffiliatedEntities result;
        switch (affiliation) {
            case admin:
                result = new AdminEntities(results);
                break;
            case member:
                result = new MemberEntities(results);
                break;
            case owner:
                result = new OwnerEntities(results);
                break;
            case outcast:
                result = new OutcastEntities(results);
                break;
            default:
                throw new ServiceException("Unrecognized affiliation: " + affiliation, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok(result).build();
    }

    @PUT
    @Path("/")
    @Operation( summary = "Replace room affiliations",
        description = "Replaces the list of users in a multi-user chat room with a specific affiliation with a new list of users. Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Affiliations of the room have been replaced."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs, or provided 'affiliations' value is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to perform this affiliation change.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response replaceMUCRoomAffiliation(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room of which affiliations are to be replaced.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations,
        @Parameter(description = "Whether to send invitations to new admin users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
        @RequestBody(description = "The new list of users with this particular affiliation.", required = true) AffiliatedEntities affiliatedEntities)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        MUCRoomController.getInstance().replaceAffiliatedUsers(serviceName, roomName, affiliation, sendInvitations, affiliatedEntities.asUserReferences());
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/")
    @Operation( summary = "Add room affiliations",
        description = "Affiliatione multiple users to a particular multi-user chat room (without removing existing affiliated users of that type). Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Users have been affiliated to the room."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs, or provided 'affiliations' value is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to perform this affiliation change.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addMUCRoomAffiliations(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which users are to be affiliated.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations,
        @Parameter(description = "Whether to send invitations to new admin users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations,
        @RequestBody(description = "The list of users to affiliate to the room.", required = true) AffiliatedEntities affiliatedEntities)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        MUCRoomController.getInstance().addAffiliatedUsers(serviceName, roomName, affiliation, sendInvitations, affiliatedEntities.asUserReferences());
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room affiliation",
        description = "Affiliates a single use to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "User to affiliate to the room."),
            @ApiResponse(responseCode = "400", description = "Provided 'affiliations' value is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to perform this affiliation change.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomAffiliation(
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The (bare) JID of the entity that is to be affiliated.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations,
            @Parameter(description = "The name of the MUC room to which an affiliation is to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "Whether to send invitations to new admin users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations)
    throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        MUCRoomController.getInstance().addAffiliatedUsers(serviceName, roomName, affiliation, sendInvitations, jid);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/group/{groupname}")
    @Operation( summary = "Add room affiliations",
        description = "Affiliate all members of an Openfire user group to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By affiliating a user to a room, any other pre-existing affiliation for that user is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Affiliations added to the room."),
            @ApiResponse(responseCode = "400", description = "Provided 'affiliations' value is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to perform this affiliation change.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomAffiliationGroup(
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the user group from which all members will be affiliated to the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
            @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations,
            @Parameter(description = "The name of the MUC room to which affiliations are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
            @Parameter(description = "Whether to send invitations to new admin users.", example = "true", required = false) @DefaultValue("false") @QueryParam("sendInvitations") boolean sendInvitations)
    throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        MUCRoomController.getInstance().addAffiliatedUsers(serviceName, roomName, affiliation, sendInvitations, groupname);
        return Response.status(Status.CREATED).build();
    }

    @DELETE
    @Path("/{jid}")
    @Operation( summary = "Remove room affiliation",
        description = "Removes an affiliation of a user to a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Affiliation removed from the room."),
            @ApiResponse(responseCode = "400", description = "Provided 'affiliations' value is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Applying this affiliation change would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomAffiliation(
            @Parameter(description = "The (bare) JID of the entity for which the room affiliation is to be removed.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations,
            @Parameter(description = "The name of the MUC room from which an affiliation is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, affiliation, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room affiliations",
        description = "Removes affiliation for all members of an Openfire user group from a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Affiliations removed from the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Provided 'affiliations' value is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Applying this affiliation change would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomAffiliationGroup(
        @Parameter(description = "The name of the user group from which all members will get their room affiliation removed.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The type of affiliation. One of: 'admins', 'members', 'outcasts', 'owners' .", example = "members", required = true) @PathParam("affiliation") String affiliations,
        @Parameter(description = "The name of the MUC room from which affiliations are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        roomName = JID.nodeprep(roomName);
        final Affiliation affiliation;
        try {
            affiliation = MUCRoomUtils.convertPluralStringToAffiliation(affiliations);
        } catch (RuntimeException e) {
            throw new ServiceException("Invalid 'affiliations' value: " + affiliations, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Status.BAD_REQUEST);
        }
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, affiliation, groupname);
        return Response.status(Status.OK).build();
    }
}
