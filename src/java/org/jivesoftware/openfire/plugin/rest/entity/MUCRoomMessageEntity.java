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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


//xmlns=&quot;jabber:x:event&quot;&gt;&lt;composing/&gt;&lt;/x&gt;&lt;/message&gt
@XmlRootElement(name = "message")
@XmlType(propOrder = { "to", "from", "type", "body", "delayStamp", "delayFrom"})
public class MUCRoomMessageEntity {
    String to;
    String from;
    String type;
    String body;
    String delayStamp;
    String delayFrom;

    @XmlElement
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }

    @XmlElement
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    @XmlElement
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(name="delay_stamp")
    public String getDelayStamp() { return delayStamp; }
    public void setDelayStamp(String delayStamp) {
        this.delayStamp = delayStamp;
    }

    @XmlElement
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    @XmlElement(name="delay_from")
    public String getDelayFrom() { return delayFrom; }
    public void setDelayFrom(String delayFrom) { this.delayFrom = delayFrom; }

}
