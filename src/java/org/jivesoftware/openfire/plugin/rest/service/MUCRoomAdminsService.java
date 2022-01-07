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

@Path("restapi/v1/chatrooms/{roomName}/admins")
@Tag(name = "Chat room", description = "Managing Multi-User chat rooms.")
public class MUCRoomAdminsService {

    @POST
    @Path("/{jid}")
    @Operation( summary = "Add room admin",
        description = "Adds an administrator to a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Administrator added to the room.")
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
        description = "Adds all members of an Openfire user group as administrator to a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Administrators added to the room.")
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
            @ApiResponse(responseCode = "200", description = "Administrator removed from the room.")
        })
    public Response deleteMUCRoomAdmin(
            @Parameter(description = "The (bare) JID of the entity that is to be removed as an administrators of the room.", example = "john@example.org", required = true) @PathParam("jid") String jid,
            @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
            @Parameter(description = "The name of the MUC room from which an administrator is to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        // FIXME: check if this removes _all_ affiliations, which probably would be more than we're bargaining for.
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, jid);
        return Response.status(Status.OK).build();
    }

    @DELETE
    @Path("/group/{groupname}")
    @Operation( summary = "Remove room admins",
        description = "Removes all members of an Openfire user group as administrator of a multi-user chat room.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Administrators removed from the room.")
        })
    public Response deleteMUCRoomAdminGroup(
        @Parameter(description = "The name of the user group from which all members will be removed as administrators of the room.", example = "Operators", required = true) @PathParam("groupname") String groupname,
        @Parameter(description = "The name of the MUC service that the MUC room is part of.", example = "conference", required = false) @DefaultValue("conference") @QueryParam("servicename") String serviceName,
        @Parameter(description = "The name of the MUC room to which administrators are to be removed.", example = "lobby", required = true) @PathParam("roomName") String roomName)
        throws ServiceException
    {
        // FIXME: check if this removes _all_ affiliations, which probably would be more than we're bargaining for.
        MUCRoomController.getInstance().deleteAffiliation(serviceName, roomName, groupname);
        return Response.status(Status.OK).build();
    }
}
