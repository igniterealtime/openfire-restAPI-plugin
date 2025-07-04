/*
 * Copyright (c) 2025 Ignite Realtime Foundation
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
import org.dom4j.Element;
import org.jivesoftware.openfire.SharedGroupException;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.RosterItemEntity;
import org.jivesoftware.openfire.plugin.rest.entity.UserGroupsEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/users/{username}/vcard")
@Tag(name = "Users", description = "Managing vCards of Openfire users.")
public class UserVCardService
{
    private UserServiceController plugin;

    @PostConstruct
    public void init() {
        plugin = UserServiceController.getInstance();
    }

    @GET
    @Operation( summary = "Get user's vCard",
        description = "Retrieves the vCard for a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The vCard of the user"),
            @ApiResponse(responseCode = "204", description = "No vCard found.")
        })
    @Produces({MediaType.APPLICATION_XML})
    public String getUserVcard(
            @Parameter(description = "The username for user for which to return the vcard.", required = true) @PathParam("username") String username)
        throws ServiceException
    {
        final Element el = plugin.getUserVCard(username);
        if (el == null) {
            return null;
        }
        return el.asXML();
    }

    @PUT
    @Operation( summary = "Update vCard",
        description = "Creates or changes a vCard of a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The vCard was updated/created."),
            @ApiResponse(responseCode = "400", description = "Provided data could not be parsed."),
            @ApiResponse(responseCode = "409", description = "Cannot change vCard, as Openfire is configured to have read-only vCards.")
        })
    @Consumes({MediaType.APPLICATION_XML})
    public Response setUserVcard(
        @Parameter(description = "The username of the user for which the update a roster entry.", required = true) @PathParam("username") String username,
        @RequestBody(description = "The updated definition of the vCard.", required = true) String vCard)
        throws ServiceException
    {
        plugin.setUserVCard(username, vCard);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Operation( summary = "Delete vCard",
        description = "Removes a vCard of a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The vCard was deleted."),
            @ApiResponse(responseCode = "409", description = "Cannot delete vCard, as Openfire is configured to have read-only vCards.")
        })
    public Response deleteUserVcard(
        @Parameter(description = "The username of the user for which the update a roster entry.", required = true) @PathParam("username") String username)
        throws ServiceException
    {
        plugin.deleteUserVCard(username);
        return Response.status(Response.Status.OK).build();
    }
}
