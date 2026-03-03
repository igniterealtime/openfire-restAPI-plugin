package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.controller.PubSubController;
import org.jivesoftware.openfire.plugin.rest.entity.pubsub.PubSubNodeEntities;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("restapi/v1/pubsub")
public class PubSubService {
    private PubSubController plugin;

    @PostConstruct
    public void init() {
        plugin = PubSubController.getInstance();
    }

    @GET
    @Path("/isok")
    public Response isOK(){
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{username}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PubSubNodeEntities getPubSubNodesOfUser(@PathParam("username") String username) throws ServiceException {
        return plugin.getPubSubNodesOfUser(username);
    }
}
