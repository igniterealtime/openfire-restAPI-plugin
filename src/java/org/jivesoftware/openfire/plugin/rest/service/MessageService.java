package org.jivesoftware.openfire.plugin.rest.service;

import javax.annotation.PostConstruct;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.plugin.rest.controller.MessageController;
import org.jivesoftware.openfire.plugin.rest.entity.MessageEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

@Path("restapi/v1/messages")
public class MessageService {

    private MessageController messageController;

    @PostConstruct
    public void init() {
        messageController = MessageController.getInstance();
    }

    @POST
    @Path("/users")
    public Response sendBroadcastMessage(MessageEntity messageEntity) throws ServiceException {
        messageController.sendBroadcastMessage(messageEntity);
        return Response.status(Response.Status.CREATED).build();
    }
    @POST
    @Path("/user/{address}")
    public Response sendMessage(
        MessageEntity messageEntity,
        @PathParam("address") String address
    ) throws ServiceException {
        messageController.sendMessageToUser(messageEntity, address, null);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/user/{address}/{resource}")
    public Response sendMessageWithResouce(
        MessageEntity messageEntity,
        @PathParam("address") String address,
        @PathParam("resource") String resource
    ) throws ServiceException {
        messageController.sendMessageToUser(messageEntity, address, resource);
        return Response.status(Response.Status.CREATED).build();
    }

}
