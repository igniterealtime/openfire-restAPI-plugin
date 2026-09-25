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

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "session")
@XmlType(propOrder = { "sessionId", "username", "resource", "node", "sessionStatus", "presenceStatus", "presenceMessage", "priority",
        "hostAddress", "hostName", "creationDate", "lastActionDate", "secure" })
@Schema(description = "A client session.")
public class SessionEntity {

    private String sessionId;
    private String username;
    private String resource;
    private String node;
    private String sessionStatus;
    private String presenceStatus;
    private String presenceMessage;
    private int priority;
    private String hostAddress;
    private String hostName;

    private Date creationDate;
    private Date lastActionDate;

    private boolean secure;

    public SessionEntity() {
    }

    @XmlElement
    @Schema(description = "The (full) JID of the session.", example = "john@example.org/laptop")
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @XmlElement
    @Schema(description = "The username of the user of the session, or 'Anonymous' for anonymous sessions.", example = "john")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement
    @Schema(description = "The resource part of the JID of the session.", example = "laptop")
    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @XmlElement
    @Schema(description = "Whether the session is connected to the cluster node that processes the request ('Local'), or to another cluster node ('Remote').", example = "Local")
    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @XmlElement
    @Schema(description = "The status of the session. One of: 'Closed', 'Connected', 'Authenticated', 'Unknown'.", example = "Authenticated")
    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    @XmlElement
    @Schema(description = "The availability of the user of the session. One of: 'Online', 'Away', 'Available to Chat', 'Do Not Disturb', 'Extended Away', 'Unknown/Not Recognized'.", example = "Online")
    public String getPresenceStatus() {
        return presenceStatus;
    }

    public void setPresenceStatus(String presenceStatus) {
        this.presenceStatus = presenceStatus;
    }

    @Schema(description = "The (optional) natural-language description of the availability of the user of the session.", example = "In a meeting")
    public String getPresenceMessage() {
        return presenceMessage;
    }

    public void setPresenceMessage(String presenceMessage) {
        this.presenceMessage = presenceMessage;
    }

    @XmlElement
    @Schema(description = "The presence priority of the session, from -128 to 127.", example = "0")
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @XmlElement
    @Schema(description = "The IP address of the client.", example = "192.168.0.20")
    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    @XmlElement
    @Schema(description = "The host name of the client.", example = "laptop.example.org")
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @XmlElement
    @Schema(description = "The moment at which the session was created.")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @XmlElement
    @Schema(description = "The moment at which the session last had activity.")
    public Date getLastActionDate() {
        return lastActionDate;
    }

    public void setLastActionDate(Date lastActionDate) {
        this.lastActionDate = lastActionDate;
    }

    @XmlElement
    @Schema(description = "Whether the connection of the session is encrypted.", example = "true")
    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

}
