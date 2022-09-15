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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.MUCServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.MUCServiceEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCServiceEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ErrorResponse;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("restapi/v1/chatservices")
@Tag(name = "Chat service", description = "Managing Multi-User chat services.")
public class MUCServiceService {

    @GET
    @Operation( summary = "Get chat services",
        description = "Get a list of all multi-user chat services.",
        responses = {
            @ApiResponse(responseCode = "200", description = "All chat services", content = @Content(schema = @Schema(implementation = MUCServiceEntities.class))),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MUCServiceEntities getMUCServices()
    {
        return MUCServiceController.getInstance().getChatServices();
    }

    @POST
    @Operation( summary = "Create new multi-user chat service",
        description = "Create a new multi-user chat service.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Service created."),
            @ApiResponse(responseCode = "401", description = "Web service authentication failed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Service creation is not permitted.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Service already exists, or another conflict occurred while creating the service.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected, generic error condition.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response createMUCService(
            @RequestBody(description = "The MUC service that needs to be created.", required = true) MUCServiceEntity mucServiceEntity)
        throws ServiceException
    {
        MUCServiceController.getInstance().createChatService(mucServiceEntity);
        return Response.status(Status.CREATED).build();
    }
}
