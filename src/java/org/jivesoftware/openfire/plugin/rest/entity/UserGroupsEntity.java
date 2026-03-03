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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class UserGroupsEntity.
 */
@XmlRootElement(name = "groups")
public class UserGroupsEntity {

    /** The group names. */
    private List<String> groupNames;

    /**
     * Instantiates a new user groups entity.
     */
    public UserGroupsEntity() {

    }

    /**
     * Instantiates a new user groups entity.
     *
     * @param groupNames
     *            the group names
     */
    public UserGroupsEntity(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    /**
     * Gets the group names.
     *
     * @return the group names
     */
    @XmlElement(name = "groupname")
    @JsonProperty(value = "groupnames")
    public List<String> getGroupNames() {
        return groupNames;
    }

    /**
     * Sets the group names.
     *
     * @param groupNames
     *            the new group names
     */
    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

}
