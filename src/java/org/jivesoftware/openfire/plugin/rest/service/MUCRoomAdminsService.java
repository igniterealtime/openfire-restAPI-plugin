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
import org.jivesoftware.openfire.plugin.rest.entity.AdminEntities;
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

@Path("restapi/v1/chatrooms/{roomName}/admins")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomAdminsService {

    @GET
    @Path("/")
    @Operation( summary = "All room admins",
        description = "Retrieves a list of JIDs for all administrators of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Admin list retrieved."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response getAdmins(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room for which to return admins.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        final List<String> results = MUCRoomController.getInstance().getByAffiliation(serviceName, roomName, MUCRole.Affiliation.admin).stream()
            .map(JID::toBareJID)
            .collect(Collectors.toList());
        return Response.ok(new AdminEntities(results)).build();
    }

    @PUT
    @Path("/")
    @Operation( summary = "Replace room admins",
        description = "Replaces the room admins in a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room admin, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Admins of the room have been replaced."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to modify a room admins.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response replaceMUCRoomAdmins(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room of which admins are to be replaced.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @RequestBody(description = "The new list of room admins.", required = true) AdminEntities adminEntities)
        throws ServiceException
    {
        MUCRoomController.getInstance().replaceAffiliatedUsers(serviceName, roomName, MUCRole.Affiliation.admin, adminEntities.getAdmins());
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/")
    @Operation( summary = "Add room admins",
        description = "Add multiple room admins in a multi-user chat room (without removing existing admins). Note that a user can only have one type of affiliation with a room. By adding a user as a room admin, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Admins of the room have been added."),
            @ApiResponse(responseCode = "400", description = "Provided values cannot be parsed as JIDs.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to modify a room admins.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response addMUCRoomAdmins(
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which admins are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName,
        @RequestBody(description = "The list of room admins to add to the room.", required = true) AdminEntities adminEntities)
        throws ServiceException
    {
        MUCRoomController.getInstance().addAffiliatedUsers(serviceName, roomName, MUCRole.Affiliation.admin, adminEntities.getAdmins());
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room admin",
        description = "Adds an administrator to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room admin, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Administrator added to the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to add a room admin, or adding it would cause a conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomAdmin(
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The (bare) JID of the entity that is to be added as an admin.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC room to which an administrator is to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().addAdmin(serviceName, roomName, jid);
        return Response.status(Status.CREATED).build();
    }

    @POST
    @Path("/group/{groupname}")
    @Operation( summary = "Add room admins",
        description = "Adds all members of an Openfire user group as administrator to a multi-user chat room. Note that a user can only have one type of affiliation with a room. By adding a user as a room admin, any other pre-existing affiliation is removed.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Administrators added to the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to add a room admin, or adding it would cause a conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response addMUCRoomAdminGroup(
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the user group from which all members will be administrators of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
            @Parameter(description = "The name of the MUC room to which administrators are to be added.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().addAdmin(serviceName, roomName, groupname);
        return Response.status(Status.CREATED).build();
    }

    @DELETE
    @Path("/{jid}")
    @Operation( summary = "Remove room admin",
        description = "Removes a user as an administrator of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Administrator removed from the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Applying this affiliation change would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomAdmin(
            @Parameter(description = "The (bare) JID of the entity that is to be removed as an administrators of the room.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the MUC room from which an administrator is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room admins",
        description = "Removes all members of an Openfire user group as administrator of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Administrators removed from the room."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not allowed to remove this affiliation.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "The chat room (or its service) can not be found or is not accessible.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Applying this affiliation change would cause a room conflict.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    public Response deleteMUCRoomAdminGroup(
        @Parameter(description = "The name of the user group from which all members will be removed as administrators of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which administrators are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
        return Response.status(Status.OK).build();
    }
}
