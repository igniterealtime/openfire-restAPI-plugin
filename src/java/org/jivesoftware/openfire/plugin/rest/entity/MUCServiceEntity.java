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

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "chatService")
@XmlType(propOrder = { "serviceName", "description", "hidden" })
public class MUCServiceEntity {

    private String serviceName;
    private String description;
    private boolean hidden;

    public MUCServiceEntity() {
    }

    public MUCServiceEntity(String serviceName) {
        this.serviceName = serviceName;
    }

    public MUCServiceEntity(String serviceName, String description, boolean hidden) {
        this.serviceName = serviceName;
        this.description = description;
        this.hidden = hidden;
    }

    @XmlElement
    @Schema(description = "The name of the chat service", example = "conference")
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @XmlElement
    @Schema(description = "The description of the chat service", example = "A public service")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    @Schema(description = "Whether the service is hidden", example = "false")
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
