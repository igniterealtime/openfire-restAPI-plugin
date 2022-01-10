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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("restapi/v1/lockouts")
@Tag(name = "Users", description = "Managing Openfire users.")
public class UserLockoutService {

    private UserServiceController plugin;

    @PostConstruct
    public void init() {
        plugin = UserServiceController.getInstance();
    }

    @POST
    @Path("/{username}")
    @Operation( summary = "Lock user out",
        description = "Lockout / ban the user from the chat server. The user will be kicked if the user is online.",
        responses = {
            @ApiResponse(responseCode = "201", description = "The user was locked out."),
            @ApiResponse(responseCode = "404", description = "No user of with this username exists.")
        })
    public Response disableUser(
            @Parameter(description = "The username of the user that is to be locked out.", required = true) @PathParam("username") String username)
        throws ServiceException
    {
        plugin.disableUser(username);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{username}")
    @Operation( summary = "Unlock user",
        description = "Removes a previously applied lockout / ban of a user.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User is unlocked."),
            @ApiResponse(responseCode = "404", description = "No user of with this username exists.")
        })
    public Response enableUser(
           @Parameter(description = "The username of the user for which the lockout is to be undone.", required = true) @PathParam("username") String username)
        throws ServiceException
    {
        plugin.enableUser(username);
        return Response.status(Response.Status.OK).build();
    }
}
