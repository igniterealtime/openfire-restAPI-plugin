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
import org.jivesoftware.openfire.SharedGroupException;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.RosterEntities;
import org.jivesoftware.openfire.plugin.rest.entity.RosterItemEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/users/{username}/roster")
@Tag(name = "Users", description = "Managing Openfire users.")
public class UserRosterService {

    private static final String COULD_NOT_UPDATE_THE_ROSTER = "Could not update the roster";

    private static final String COULD_NOT_CREATE_ROSTER_ITEM = "Could not create roster item";

    private UserServiceController plugin;

    @PostConstruct
    public void init() {
        plugin = UserServiceController.getInstance();
    }

    @GET
    @Operation( summary = "Retrieve user roster",
        description = "Get a list of all roster entries (buddies / contact list) of a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "All roster entries", content = @Content(schema = @Schema(implementation = RosterEntities.class))),
            @ApiResponse(responseCode = "404", description = "No user of with this username exists.")
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public RosterEntities getUserRoster(@Parameter(description = "The username of the user for which the retrieve the roster entries.", required = true) @PathParam("username") String username) throws ServiceException {
        return plugin.getRosterEntities(username);
    }

    @POST
    @Operation( summary = "Create roster entry",
        description = "Add a roster entry to the roster (buddies / contact list) of a particular user.",
        responses = {
            @ApiResponse(responseCode = "201", description = "The entry was added to the roster."),
            @ApiResponse(responseCode = "400", description = "A roster entry cannot be added to a 'shared group' (try removing group names from the roster entry and try again)."),
            @ApiResponse(responseCode = "404", description = "No user of with this username exists."),
            @ApiResponse(responseCode = "409", description = "A roster entry already exists for the provided contact JID.")
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createRoster(
            @Parameter(description = "The username of the user for which the add a roster entry.", required = true) @PathParam("username") String username,
            @RequestBody(description = "The definition of the roster entry that is to be added.", required = true) RosterItemEntity rosterItemEntity)
        throws ServiceException
    {
        try {
            plugin.addRosterItem(username, rosterItemEntity);
        } catch (UserNotFoundException e) {
            throw new ServiceException(COULD_NOT_CREATE_ROSTER_ITEM, "", ExceptionType.USER_NOT_FOUND_EXCEPTION,
                    Response.Status.NOT_FOUND, e);
        } catch (UserAlreadyExistsException e) {
            throw new ServiceException(COULD_NOT_CREATE_ROSTER_ITEM, "", ExceptionType.USER_ALREADY_EXISTS_EXCEPTION,
                    Response.Status.CONFLICT, e);
        } catch (SharedGroupException e) {
            throw new ServiceException(COULD_NOT_CREATE_ROSTER_ITEM, "", ExceptionType.SHARED_GROUP_EXCEPTION,
                    Response.Status.BAD_REQUEST, e);
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{rosterJid}")
    @Operation( summary = "Remove roster entry",
        description = "Removes one of the roster entries (contacts) of a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Entry removed"),
            @ApiResponse(responseCode = "400", description = "A roster entry cannot be removed from a 'shared group'."),
            @ApiResponse(responseCode = "404", description = "No user of with this username exists, or its roster did not contain this entry.")
        })
    public Response deleteRoster(
            @Parameter(description = "The username of the user for which the remove a roster entry.", required = true) @PathParam("username") String username,
            @Parameter(description = "The JID of the entry/contact to remove.", required = true) @PathParam("rosterJid") String rosterJid)
        throws ServiceException
    {
        try {
            plugin.deleteRosterItem(username, rosterJid);
        } catch (SharedGroupException e) {
            throw new ServiceException("Could not delete the roster item", rosterJid,
                    ExceptionType.SHARED_GROUP_EXCEPTION, Response.Status.BAD_REQUEST, e);
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{rosterJid}")
    @Operation( summary = "Update roster entry",
        description = "Changes a roster entry on the roster (buddies / contact list) of a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The roster entry was updated."),
            @ApiResponse(responseCode = "400", description = "A roster entry cannot be added with a 'shared group'."),
            @ApiResponse(responseCode = "404", description = "No user of with this username exists."),
            @ApiResponse(responseCode = "409", description = "A roster entry already exists for the provided contact JID.")
        })
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateRoster(
            @Parameter(description = "The username of the user for which the update a roster entry.", required = true) @PathParam("username") String username,
            @Parameter(description = "The JID of the entry/contact to update.", required = true) @PathParam("rosterJid") String rosterJid,
            @RequestBody(description = "The updated definition of the roster entry.", required = true) RosterItemEntity rosterItemEntity)
        throws ServiceException
    {
        try {
            plugin.updateRosterItem(username, rosterJid, rosterItemEntity);
        } catch (UserNotFoundException e) {
            throw new ServiceException(COULD_NOT_UPDATE_THE_ROSTER, rosterJid, ExceptionType.USER_NOT_FOUND_EXCEPTION,
                    Response.Status.NOT_FOUND, e);
        } catch (SharedGroupException e) {
            throw new ServiceException(COULD_NOT_UPDATE_THE_ROSTER, rosterJid, ExceptionType.SHARED_GROUP_EXCEPTION,
                    Response.Status.BAD_REQUEST, e);
        } catch (UserAlreadyExistsException e) {
            throw new ServiceException(COULD_NOT_UPDATE_THE_ROSTER, rosterJid,
                    ExceptionType.USER_ALREADY_EXISTS_EXCEPTION, Response.Status.CONFLICT, e);
        }
        return Response.status(Response.Status.OK).build();
    }
}
