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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The Class GroupEntities.
 */
@XmlRootElement(name = "groups")
public class GroupEntities {
    
    /** The groups. */
    List<GroupEntity> groups;

    /**
     * Instantiates a new group entities.
     */
    public GroupEntities() {
    }

    /**
     * Instantiates a new group entities.
     *
     * @param groups the groups
     */
    public GroupEntities(List<GroupEntity> groups) {
        this.groups = groups;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    @XmlElement(name = "group")
    @JsonProperty(value = "groups")
    public List<GroupEntity> getGroups() {
        return groups;
    }

    /**
     * Sets the groups.
     *
     * @param groups the new groups
     */
    public void setGroups(List<GroupEntity> groups) {
        this.groups = groups;
    }

}
