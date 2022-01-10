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

/**
 * The Class MsgArchiveEntity.
 */
@XmlRootElement(name = "archive")
public class MsgArchiveEntity {

    @XmlElement
    String jid;

    /**
     * unread messages count
     */
    @XmlElement
    int count;

    public MsgArchiveEntity() {
    }

    public MsgArchiveEntity(String jid, int count) {
        this.jid = jid;
        this.count = count;
    }

}
