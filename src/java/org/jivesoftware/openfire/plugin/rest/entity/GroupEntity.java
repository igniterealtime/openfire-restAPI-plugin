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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class GroupEntity.
 */
@XmlRootElement(name = "group")
@XmlType(propOrder = { "name", "description", "admins", "members", "shared" })
public class GroupEntity {

    /** The name. */
    private String name;

    /** The description. */
    private String description;

    /** The admins. */
    private List<String> admins;

    /** The members. */
    private List<String> members;

    /** The visibility, false unless set */
    private Boolean shared = false;

    /**
     * Instantiates a new group entity.
     */
    public GroupEntity() {
    }

    /**
     * Instantiates a new group entity.
     *
     * @param name
     *            the name
     * @param description
     *            the description
     */
    public GroupEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @XmlElement
    @Schema(description = "Name of the group", example = "UserGroup1")
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    @XmlElement
    @Schema(description = "Description of the group", example = "My group of users")
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the admins.
     *
     * @return the admins
     */
    @XmlElementWrapper(name = "admins")
    @XmlElement(name = "admin")
    @JsonProperty(value = "admins")
    @ArraySchema(schema = @Schema(example = "jane.smith"), arraySchema = @Schema(description = "List of admins of the group"))
    public List<String> getAdmins() {
        return admins;
    }

    /**
     * Gets the members.
     *
     * @return the members
     */
    @XmlElementWrapper(name = "members")
    @XmlElement(name = "member")
    @JsonProperty(value = "members")
    @ArraySchema(schema = @Schema(example = "john.jones"), arraySchema = @Schema(description = "List of members of the group"))
    public List<String> getMembers() {
        return members;
    }

    /**
     * Sets the admins.
     *
     * @param admins the new admins
     */
    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    /**
     * Sets the members.
     *
     * @param members the new members
     */
    public void setMembers(List<String> members) {
        this.members = members;
    }


    /**
     * Gets whether this is a shared group
     *
     * @return whether it's a shared group
     */
    @XmlElement(name = "shared")
    @Schema(description = "Whether the group should automatically appear in the rosters of the users", example = "false")
    public Boolean getShared(){ return shared; }

    /**
     * Sets whether this is a shared group
     *
     * @param shared whether this is a shared group
     */
    public void setShared(Boolean shared) { this.shared = shared; }

}
