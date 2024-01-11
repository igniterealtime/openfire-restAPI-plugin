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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.MessageController;
import org.jivesoftware.openfire.plugin.rest.entity.MessageEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/messages")
@Tag(name = "Message", description = "Sending (chat) messages to users.")
public class MessageService {

    private MessageController messageController;

    @PostConstruct
    public void init() {
        messageController = MessageController.getInstance();
    }

    @POST
    @Path("/users")
    @Operation(
        summary = "Broadcast",
        description = "Sends a message to all users that are currently online.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Message is sent."),
            @ApiResponse(responseCode = "400", description = "The message content is empty or missing."),
        }
    )
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response sendBroadcastMessage(@RequestBody(description = "The message that is to be broadcast.", required = true) MessageEntity messageEntity)
        throws ServiceException {
        messageController.sendBroadcastMessage(messageEntity);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/user/{address}")
    @Operation(
        summary = "Messaging",
        description = "Send a message to a single user.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Message is sent."),
            @ApiResponse(responseCode = "400", description = "The message content is empty or missing."),
            @ApiResponse(responseCode = "400", description = "The message recipient is empty or invalid."),
        }
    )
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response sendMessage(
        @RequestBody(description = "The message that is to be broadcast.", required = true) MessageEntity messageEntity,
        @Parameter(description =   "The (bare) JID of the message recipient.", example = "john@example.org", required = true)  @PathParam("address") String address
    ) throws ServiceException {
        messageController.sendMessageToUser(messageEntity, address, null);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/user/{address}/{resource}")
    @Operation(
        summary = "Messaging",
        description = "Send a message to a single user.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Message is sent."),
            @ApiResponse(responseCode = "400", description = "The message content is empty or missing."),
            @ApiResponse(responseCode = "400", description = "The message recipient is empty or invalid."),
        }
    )
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response sendMessage(
        @RequestBody(description = "The message that is to be broadcast.", required = true) MessageEntity messageEntity,
        @Parameter(description =   "The (bare) JID of the message recipient.", example = "john@example.org", required = true) @PathParam("address") String address,
        @Parameter(description =   "The resource of the message recipient.", example = "123", required = true) @PathParam("resource") String resource
    ) throws ServiceException {
        messageController.sendMessageToUser(messageEntity, address, resource);
        return Response.status(Response.Status.CREATED).build();
    }
}
