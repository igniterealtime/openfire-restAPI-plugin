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

@Path("restapi/v1/chatrooms/{roomName}/outcasts")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomOutcastsService {

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room outcast",
        description = "Marks a JID as outcast of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "JID marked as outcast.")
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
        description = "Marks all members of an Openfire user group as outcasts of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Group members marked as outcast.")
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
            @ApiResponse(responseCode = "200", description = "JID no longer marked as outcast.")
        })
    public Response deleteMUCRoomOutcast(
            @Parameter(description = "The (bare) JID of the entity that is to be removed as an outcast of the room.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the MUC room from which an outcast is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        // FIXME: check if this removes _all_ affiliations, which probably would be more than we're bargaining for.
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room outcasts",
        description = "Removes all members of an Openfire user group as outcast of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Group members no longer marked as outcast.")
        })
    public Response deleteMUCRoomOutcastGroup(
            @Parameter(description = "The name of the user group from which all members will be removed as outcast of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the MUC room to which outcast are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        // FIXME: check if this removes _all_ affiliations, which probably would be more than we're bargaining for.
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
        return Response.status(Status.OK).build();
    }
}
