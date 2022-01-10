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
import org.jivesoftware.openfire.plugin.rest.controller.MsgArchiveController;
import org.jivesoftware.openfire.plugin.rest.entity.MsgArchiveEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.xmpp.packet.JID;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("restapi/v1/archive/messages/unread/{jid}")
@Tag(name = "Message Archive", description = "Server-sided storage of chat messages.")
public class MsgArchiveService {

    private MsgArchiveController archive;

    @PostConstruct
    public void init() {
        archive = MsgArchiveController.getInstance();
    }

    @GET
    @Operation( summary = "Unread message count",
        description = "Gets a count of messages that haven't been delivered to the user yet.",
        responses = {
            @ApiResponse(responseCode = "200", description = "A message count", content = @Content(schema = @Schema(implementation = MsgArchiveEntity.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public MsgArchiveEntity getUnReadMessagesCount(@Parameter(description = "The (bare) JID of the user for which the unread message count needs to be fetched.", example = "john@example.org", required = true) @PathParam("jid") String jidStr)
        throws ServiceException
    {
        JID jid = new JID(jidStr);
        int msgCount = archive.getUnReadMessagesCount(jid);
        return new MsgArchiveEntity(jidStr, msgCount);
    }
}
