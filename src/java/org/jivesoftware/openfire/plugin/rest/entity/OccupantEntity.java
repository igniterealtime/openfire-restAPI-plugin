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

@XmlRootElement(name = "occupant")
@Schema(description = "An occupant of a multi-user chat room.")
public class OccupantEntity {

    private String jid;
    private String userAddress;
    private String role;
    private String affiliation;

    public OccupantEntity() {
    }

    @XmlElement
    @Schema(description = "The occupant JID: the room JID, followed by the nickname of the occupant.", example = "global@conference.example.org/john")
    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    @XmlElement
    @Schema(description = "The role of the occupant in the room. One of: 'moderator', 'participant', 'visitor', 'none'.", example = "participant")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @XmlElement
    @Schema(description = "The affiliation of the occupant with the room. One of: 'owner', 'admin', 'member', 'outcast', 'none'.", example = "member")
    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @XmlElement
    @Schema(description = "The real (full) JID of the user.", example = "john@example.org/laptop")
    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
}
