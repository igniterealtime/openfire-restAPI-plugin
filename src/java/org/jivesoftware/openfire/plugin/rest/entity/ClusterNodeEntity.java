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
package org.jivesoftware.openfire.plugin.rest.entity;

import org.jivesoftware.openfire.cluster.ClusterNodeInfo;
import org.jivesoftware.openfire.cluster.NodeID;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "clusterNode")
public class ClusterNodeEntity {

    private String hostName;

    private String nodeID;

    private Date joinedTime;

    private boolean seniorMember;

    @Nonnull
    public static ClusterNodeEntity from(@Nonnull final ClusterNodeInfo clusterNodeInfo) {
        return new ClusterNodeEntity(clusterNodeInfo.getHostName(), clusterNodeInfo.getNodeID(), clusterNodeInfo.getJoinedTime(), clusterNodeInfo.isSeniorMember());
    }

    public ClusterNodeEntity() {}

    public ClusterNodeEntity(String hostName, NodeID nodeID, long joinedTime, boolean seniorMember) {
        this.hostName = hostName;
        this.nodeID = nodeID == null ? null : nodeID.toString();
        this.joinedTime = joinedTime < 0 ? null : new Date(joinedTime);
        this.seniorMember = seniorMember;
    }

    @XmlElement
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @XmlElement
    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    @XmlElement
    public Date getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(Date joinedTime) {
        this.joinedTime = joinedTime;
    }

    @XmlElement
    public boolean isSeniorMember() {
        return seniorMember;
    }

    public void setSeniorMember(boolean seniorMember) {
        this.seniorMember = seniorMember;
    }
}
