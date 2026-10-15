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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


//xmlns=&quot;jabber:x:event&quot;&gt;&lt;composing/&gt;&lt;/x&gt;&lt;/message&gt
@XmlRootElement(name = "message")
@XmlType(propOrder = { "to", "from", "type", "body", "delayStamp", "delayFrom"})
@Schema(description = "A message from the history of a multi-user chat room.")
public class MUCRoomMessageEntity {
    String to;
    String from;
    String type;
    String body;
    String delayStamp;
    String delayFrom;

    @XmlElement
    @Schema(description = "The JID of the addressee of the message.", example = "global@conference.example.org")
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }

    @XmlElement
    @Schema(description = "The JID of the sender of the message: the room JID, followed by the nickname of the occupant.", example = "global@conference.example.org/john")
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    @XmlElement
    @Schema(description = "The XMPP message type.", example = "groupchat")
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name="delay_stamp")
    @JsonProperty(value = "delay_stamp")
    @Schema(description = "The moment at which the message was originally sent (XEP-0203 delayed delivery timestamp).", example = "2026-01-31T12:34:56.789Z")
    public String getDelayStamp() { return delayStamp; }
    public void setDelayStamp(String delayStamp) {
        this.delayStamp = delayStamp;
    }

    @XmlElement
    @Schema(description = "The text of the message.", example = "Hello, everyone!")
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    @XmlElement(name="delay_from")
    @JsonProperty(value = "delay_from")
    @Schema(description = "The JID of the entity that delayed the delivery of the message (XEP-0203).", example = "global@conference.example.org")
    public String getDelayFrom() { return delayFrom; }
    public void setDelayFrom(String delayFrom) { this.delayFrom = delayFrom; }

}
