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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "clusterNodes")
public class ClusterNodeEntities
{
    private List<ClusterNodeEntity> clusterNodeEntities;

    public ClusterNodeEntities() {}

    public ClusterNodeEntities(@Nonnull final List<ClusterNodeEntity> clusterNodeEntities) {
        this.clusterNodeEntities = clusterNodeEntities;
    }

    @XmlElement(name = "clusterNode")
    @JsonProperty(value = "clusterNodes")
    public List<ClusterNodeEntity> getClusterNodeEntities() {
        return clusterNodeEntities;
    }

    public void setClusterNodeEntities(List<ClusterNodeEntity> clusterNodeEntities) {
        this.clusterNodeEntities = clusterNodeEntities;
    }
}
