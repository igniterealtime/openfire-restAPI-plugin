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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.SessionController;
import org.jivesoftware.openfire.plugin.rest.entity.SessionEntities;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/sessions")
@Tag(name = "Client Sessions", description = "Managing live client sessions.")
public class SessionService {

    private SessionController sessionController;

    @PostConstruct
    public void init() {
        sessionController = SessionController.getInstance();
    }

    @GET
    @Operation( summary = "Get all sessions",
        description = "Retrieve all live client sessions.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The client sessions currently active in Openfire.", content = @Content(schema = @Schema(implementation = SessionEntities.class))),
        })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SessionEntities getAllSessions() throws ServiceException {
        return sessionController.getAllSessions();
    }
    
    @GET
    @Path("/{username}")
    @Operation( summary = "Get user sessions",
        description = "Retrieve all live client sessions for a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The client sessions for one particular user that are currently active in Openfire.", content = @Content(schema = @Schema(implementation = SessionEntities.class))),
        })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SessionEntities getUserSessions(
            @Parameter(description = "The name of a user for which to return client sessions.", required = true, example = "johndoe") @PathParam("username") String username)
        throws ServiceException
    {
        return sessionController.getUserSessions(username);
    }
    
    @DELETE
    @Operation( summary = "Kick user sessions",
        description = "Close/disconnect all live client sessions for a particular user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The client sessions for one particular user have been closed."),
        })
    @Path("/{username}")
    public Response kickSession(
            @Parameter(description = "The name of a user for which to drop all client sessions.", required = true, example = "johndoe") @PathParam("username") String username)
        throws ServiceException
    {
        sessionController.removeUserSessions(username);
        return Response.status(Response.Status.OK).build();
    }
    
}
