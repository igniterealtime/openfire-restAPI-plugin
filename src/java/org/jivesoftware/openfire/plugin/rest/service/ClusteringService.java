package org.jivesoftware.openfire.plugin.rest.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.ClusteringController;
import org.jivesoftware.openfire.plugin.rest.entity.ClusteringEntity;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("restapi/v1/clustering")
@Tag(name="Clustering", description = "Reporting the status of Openfire clustering")
public class ClusteringService {

    private ClusteringController clusteringController;

    @PostConstruct
    public void init() {
        clusteringController = ClusteringController.getInstance();
    }

    @GET
    @Operation( summary = "Get clustering status",
        description = "Describes the point-in-time state of Openfire's clustering with other servers",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status returned", content = @Content(schema = @Schema(implementation = ClusteringEntity.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/status")
    public ClusteringEntity getClusteringStatus(){
        return new ClusteringEntity(clusteringController.getClusterStatus());
    }
}
