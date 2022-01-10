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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class RosterItemEntity.
 */
@XmlRootElement(name = "rosterItem")
@XmlType(propOrder = { "jid", "nickname", "subscriptionType", "groups" })
public class RosterItemEntity {

    /** The jid. */
    private String jid;

    /** The nickname. */
    private String nickname;

    /** The subscription type. */
    private int subscriptionType;

    /** The groups. */
    private List<String> groups;

    /**
     * Instantiates a new roster item entity.
     */
    public RosterItemEntity() {

    }

    /**
     * Instantiates a new roster item entity.
     *
     * @param jid
     *            the jid
     * @param nickname
     *            the nickname
     * @param subscriptionType
     *            the subscription type
     */
    public RosterItemEntity(String jid, String nickname, int subscriptionType) {
        this.jid = jid;
        this.nickname = nickname;
        this.subscriptionType = subscriptionType;
    }

    /**
     * Gets the jid.
     *
     * @return the jid
     */
    @XmlElement
    public String getJid() {
        return jid;
    }

    /**
     * Sets the jid.
     *
     * @param jid
     *            the new jid
     */
    public void setJid(String jid) {
        this.jid = jid;
    }

    /**
     * Gets the nickname.
     *
     * @return the nickname
     */
    @XmlElement
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname.
     *
     * @param nickname
     *            the new nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gets the subscription type.
     *
     * @return the subscription type
     */
    @XmlElement
    public int getSubscriptionType() {
        return subscriptionType;
    }

    /**
     * Sets the subscription type.
     *
     * @param subscriptionType
     *            the new subscription type
     */
    public void setSubscriptionType(int subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    @XmlElement(name = "group")
    @XmlElementWrapper(name = "groups")
    @JsonProperty(value = "groups")
    public List<String> getGroups() {
        return groups;
    }

    /**
     * Sets the groups.
     *
     * @param groups
     *            the new groups
     */
    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
