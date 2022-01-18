package org.jivesoftware.openfire.plugin.rest.service;

import org.jivesoftware.openfire.plugin.rest.controller.ClusteringController;
import org.jivesoftware.openfire.plugin.rest.entity.ClusteringEntity;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("restapi/v1/clustering")
public class ClusteringService {

    private ClusteringController clusteringController;

    @PostConstruct
    public void init() {
        clusteringController = ClusteringController.getInstance();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/status")
    public ClusteringEntity getClusteringStatus(){
        return new ClusteringEntity(clusteringController.getClusterStatus());
    }
}
