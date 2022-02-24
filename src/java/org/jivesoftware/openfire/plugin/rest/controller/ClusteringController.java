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
package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.openfire.cluster.ClusterManager;
import org.jivesoftware.openfire.cluster.ClusterNodeInfo;
import org.jivesoftware.openfire.cluster.NodeID;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.ClusterNodeEntities;
import org.jivesoftware.openfire.plugin.rest.entity.ClusterNodeEntity;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClusteringController {
    private static final Logger LOG = LoggerFactory.getLogger(ClusteringController.class);

    private static ClusteringController INSTANCE = null;

    /**
     * Gets the single instance of ClusteringController.
     *
     * @return single instance of ClusteringController
     */
    public static ClusteringController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClusteringController();
        }
        return INSTANCE;
    }

    /**
     * @param instance the mock/stub/spy controller to use.
     * @deprecated - for test use only
     */
    @Deprecated
    public static void setInstance(final ClusteringController instance) {
        ClusteringController.INSTANCE = instance;
    }

    public static void log(String logMessage) {
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            LOG.info(logMessage);
        }
    }

    public String getClusterStatus() {
        if (ClusterManager.isClusteringEnabled()) {
            if (ClusterManager.isClusteringStarted()) {
                if (ClusterManager.isSeniorClusterMember()) {
                    if (ClusterManager.getNodesInfo().size() == 1) {
                        return "SENIOR AND ONLY MEMBER"; //admin.clustering.only
                    } else {
                        return "Senior member"; //admin.clustering.senior
                    }
                } else {
                    return "Junior member"; //admin.clustering.junior
                }
            } else {
                return "Starting up"; //admin.clustering.starting
            }
        } else {
            return "Disabled"; //admin.clustering.disabled
        }
    }

    public Optional<ClusterNodeEntity> getNodeEntity(String nodeId) {
        final Optional<ClusterNodeInfo> nodeInfo = ClusterManager.getNodeInfo(NodeID.getInstance(nodeId.getBytes(StandardCharsets.UTF_8)));
        return nodeInfo.map(ClusterNodeEntity::from);
    }

    public ClusterNodeEntities getNodeEntities() {
        final Collection<ClusterNodeInfo> nodesInfo = ClusterManager.getNodesInfo();
        return new ClusterNodeEntities(nodesInfo.stream().map(ClusterNodeEntity::from).collect(Collectors.toList()));
    }
}
