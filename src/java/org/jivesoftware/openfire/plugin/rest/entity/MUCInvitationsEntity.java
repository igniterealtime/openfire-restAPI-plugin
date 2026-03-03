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
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "mucInvitations")
public class MUCInvitationsEntity extends MUCInvitationEntity
{
    public MUCInvitationsEntity() {
        super();
    }
    
    private List<String> jidsToInvite;

    @XmlElementWrapper(name = "jidsToInvite")
    @XmlElement(name = "jid")
    @JsonProperty(value = "jidsToInvite")
    @Schema(description = "The JIDs and/or names of the users and groups to invite into the room")
    public List<String> getJidsToInvite() {
        if (jidsToInvite == null) {
            jidsToInvite = new ArrayList<>();
        }
        return jidsToInvite;
    }

    public void setJidsToInvite(List<String> jidsToInvite) {
        this.jidsToInvite = jidsToInvite;
    }

}
