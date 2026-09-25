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

/**
 * The Class MsgArchiveEntity.
 */
@XmlRootElement(name = "archive")
@XmlType(propOrder = { "jid", "count" })
@Schema(description = "The number of unread messages of a user.")
public class MsgArchiveEntity {

    String jid;

    /**
     * unread messages count
     */
    int count;

    public MsgArchiveEntity() {
    }

    public MsgArchiveEntity(String jid, int count) {
        this.jid = jid;
        this.count = count;
    }

    @XmlElement
    @Schema(description = "The JID of the user.", example = "john@example.org")
    public String getJid() {
        return jid;
    }

    @XmlElement
    @Schema(description = "The number of unread messages.", example = "3")
    public int getCount() {
        return count;
    }
}
