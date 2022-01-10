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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class RosterEntities.
 */
@XmlRootElement(name = "roster")
public class RosterEntities {

    /** The roster. */
    List<RosterItemEntity> roster;

    /**
     * Instantiates a new roster entities.
     */
    public RosterEntities() {

    }

    /**
     * Instantiates a new roster entities.
     *
     * @param roster
     *            the roster
     */
    public RosterEntities(List<RosterItemEntity> roster) {
        this.roster = roster;
    }

    /**
     * Gets the roster.
     *
     * @return the roster
     */
    @XmlElement(name = "rosterItem")
    public List<RosterItemEntity> getRoster() {
        return roster;
    }

    /**
     * Sets the roster.
     *
     * @param roster
     *            the new roster
     */
    public void setRoster(List<RosterItemEntity> roster) {
        this.roster = roster;
    }

}
