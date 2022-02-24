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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.cluster.ClusterNodeInfo;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.plugin.rest.controller.ClusteringController;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Path("restapi/v1/clustering")
@Tag(name="Clustering", description = "Reporting the status of Openfire clustering")
public class ClusteringService {

    private ClusteringController clusteringController;

    @PostConstruct
    public void init() {
        clusteringController = ClusteringController.getInstance();
    }

    @GET
    @Path("/status")
    @Operation( summary = "Get clustering status",
        description = "Describes the point-in-time state of Openfire's clustering with other servers",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status returned", content = @Content(schema = @Schema(implementation = ClusteringEntity.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ClusteringEntity getClusteringStatus(){
        return new ClusteringEntity(clusteringController.getClusterStatus());
    }

    @GET
    @Path("/nodes")
    @Operation( summary = "Get all cluster nodes",
        description = "Get a list of all nodes of the cluster. Note that this endpoint can only return data for remote nodes when the instance of Openfire that processes this query has successfully joined the cluster.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Retrieve all cluster nodes", content = @Content(schema = @Schema(implementation = ClusterNodeEntities.class)))
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ClusterNodeEntities getClusterNodes() {
        return clusteringController.getNodeEntities();
    }

    @GET
    @Path("/nodes/{nodeId}")
    @Operation( summary = "Get a specific cluster node",
        description = "Get a specific node of the cluster. Note that this endpoint can only return data for remote nodes when the instance of Openfire that processes this query has successfully joined the cluster.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Retrieve a cluster node", content = @Content(schema = @Schema(implementation = ClusterNodeEntity.class))),
            @ApiResponse(responseCode = "404", description = "The provided NodeID does not identify an existing cluster node.")
        })
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public ClusterNodeEntity getClusterNode(@Parameter(description = "The nodeID value for a particular node.", example = "52a89928-66f7-45fd-9bb8-096de07400ac", required = true) @PathParam("nodeId") final String nodeId) throws ServiceException {
        final Optional<ClusterNodeEntity> entity = clusteringController.getNodeEntity(nodeId);
        if (entity.isPresent()) {
            return entity.get();
        } else {
            throw new ServiceException(
                "Could not find a cluster node with this NodeID.", nodeId,
                ExceptionType.CLUSTER_NODE_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }
}
