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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.xmpp.packet.JID;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "chatRoom")
@XmlType(propOrder = { "roomName", "naturalName", "description", "password", "subject", "creationDate",
        "modificationDate", "maxUsers", "persistent", "publicRoom", "registrationEnabled", "canAnyoneDiscoverJID",
        "canOccupantsChangeSubject", "canOccupantsInvite", "canChangeNickname", "logEnabled",
        "loginRestrictedToNickname", "membersOnly", "moderated", "broadcastPresenceRoles", "owners", "admins",
        "members", "outcasts", "ownerGroups", "adminGroups", "memberGroups", "outcastGroups", "allowPM" })
@Schema(description = "A multi-user chat room. When a room is created or updated, boolean values that are not provided are treated as 'false'.")
public class MUCRoomEntity {

    private String roomName;
    private String description;
    private String password;
    private String subject;
    private String naturalName;

    private int maxUsers;

    private Date creationDate;
    private Date modificationDate;

    private boolean persistent;
    private boolean publicRoom;
    private boolean registrationEnabled;
    private boolean canAnyoneDiscoverJID;
    private boolean canOccupantsChangeSubject;
    private boolean canOccupantsInvite;
    private boolean canChangeNickname;
    private boolean logEnabled;
    private boolean loginRestrictedToNickname;
    private boolean membersOnly;
    private boolean moderated;
    private String allowPM;

    private List<String> broadcastPresenceRoles;

    private List<String> owners;
    private List<String> ownerGroups;

    private List<String> admins;
    private List<String> adminGroups;

    private List<String> members;
    private List<String> memberGroups;

    private List<String> outcasts;
    private List<String> outcastGroups;

    public MUCRoomEntity() {
    }

    public MUCRoomEntity(String naturalName, String roomName, String description) {
        this.naturalName = naturalName;
        this.roomName = roomName == null ? null : JID.nodeprep(roomName);
        this.description = description;
    }

    @XmlElement
    @Schema(description = "The human-readable name of the room, as shown to users that discover rooms on the chat service.", example = "Global Chat")
    public String getNaturalName() {
        return naturalName;
    }

    public void setNaturalName(String naturalName) {
        this.naturalName = naturalName;
    }

    @XmlElement
    @Schema(description = "The name of the room, which is used as the local part of the room's JID. It is converted to lowercase. When updating a room, this must be equal to the room name in the path of the request.", example = "global", requiredMode = Schema.RequiredMode.REQUIRED)
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName == null ? null : JID.nodeprep(roomName);;
    }

    @XmlElement
    @Schema(description = "The description of the room.", example = "A room for everyone")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    @Schema(description = "The password that users must provide to enter the room.", example = "s3cr3t")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement
    @Schema(description = "The subject (topic) of the room.", example = "Welcome!")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @XmlElement
    @Schema(description = "The maximum number of occupants that can be in the room at the same time. 0 means unlimited.", example = "30")
    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    @XmlElement
    @Schema(description = "The moment at which the room was created. When creating a room without this value, the current time is used.")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @XmlElement
    @Schema(description = "The moment at which the configuration of the room was last modified. When creating or updating a room without this value, the current time is used.")
    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @XmlElement
    @Schema(description = "Whether the room is persistent. Persistent rooms are saved to the database, and are not destroyed when the last occupant leaves.", example = "true")
    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    @XmlElement
    @Schema(description = "Whether the room is public: searchable and visible through service discovery.", example = "true")
    public boolean isPublicRoom() {
        return publicRoom;
    }

    public void setPublicRoom(boolean publicRoom) {
        this.publicRoom = publicRoom;
    }

    @XmlElement
    @Schema(description = "Whether users are allowed to register with the room.", example = "false")
    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    @XmlElement
    @Schema(description = "Whether the real JID of every occupant is visible to every other occupant (a non-anonymous room).", example = "false")
    public boolean isCanAnyoneDiscoverJID() {
        return canAnyoneDiscoverJID;
    }

    public void setCanAnyoneDiscoverJID(boolean canAnyoneDiscoverJID) {
        this.canAnyoneDiscoverJID = canAnyoneDiscoverJID;
    }

    @XmlElement
    @Schema(description = "Whether participants are allowed to change the subject of the room.", example = "false")
    public boolean isCanOccupantsChangeSubject() {
        return canOccupantsChangeSubject;
    }

    public void setCanOccupantsChangeSubject(boolean canOccupantsChangeSubject) {
        this.canOccupantsChangeSubject = canOccupantsChangeSubject;
    }

    @XmlElement
    @Schema(description = "Whether occupants can invite other users to the room. When the room is not members-only, anyone can send invitations regardless of this value. When the room is members-only and this is 'false', only owners and admins can send invitations.", example = "false")
    public boolean isCanOccupantsInvite() {
        return canOccupantsInvite;
    }

    public void setCanOccupantsInvite(boolean canOccupantsInvite) {
        this.canOccupantsInvite = canOccupantsInvite;
    }

    public void setBroadcastPresenceRoles(List<String> broadcastPresenceRoles) {
        this.broadcastPresenceRoles = broadcastPresenceRoles;
    }

    @XmlElement
    @Schema(description = "Whether occupants are allowed to change their nickname in the room.", example = "true")
    public boolean isCanChangeNickname() {
        return canChangeNickname;
    }

    public void setCanChangeNickname(boolean canChangeNickname) {
        this.canChangeNickname = canChangeNickname;
    }

    @XmlElement
    @Schema(description = "Whether the conversation in the room is logged (saved to the database).", example = "true")
    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    @XmlElement
    @Schema(description = "Whether registered users can only join the room using their registered nickname.", example = "false")
    public boolean isLoginRestrictedToNickname() {
        return loginRestrictedToNickname;
    }

    public void setLoginRestrictedToNickname(boolean loginRestrictedToNickname) {
        this.loginRestrictedToNickname = loginRestrictedToNickname;
    }

    @XmlElement
    @Schema(description = "Whether the room is members-only: users need to be a member (or be invited) to enter.", example = "false")
    public boolean isMembersOnly() {
        return membersOnly;
    }

    public void setMembersOnly(boolean membersOnly) {
        this.membersOnly = membersOnly;
    }

    @XmlElement
    @Schema(description = "Whether the room is moderated: only occupants with 'voice' can send messages to all occupants.", example = "false")
    public boolean isModerated() {
        return moderated;
    }

    public void setModerated(boolean moderated) {
        this.moderated = moderated;
    }

    @XmlElement
    @Schema(description = "Defines who is allowed to send private messages to other occupants. Must be one of \"anyone\", \"participants\", \"moderators\" or \"none\".", example = "anyone")
    public String getAllowPM() {
        return allowPM == null ? "anyone" : allowPM;
    }

    public void setAllowPM(String allowPM) {
        this.allowPM = allowPM == null ? "anyone" : allowPM;
    }

    @XmlElement(name = "broadcastPresenceRole")
    @XmlElementWrapper(name = "broadcastPresenceRoles")
    @JsonProperty(value = "broadcastPresenceRoles")
    @ArraySchema(arraySchema = @Schema(description = "The roles of occupants of which presence is broadcast to the other occupants. Each is one of: 'moderator', 'participant', 'visitor'."), schema = @Schema(example = "moderator"))
    public List<String> getBroadcastPresenceRoles() {
        return broadcastPresenceRoles;
    }

    @XmlElementWrapper(name = "owners")
    @XmlElement(name = "owner")
    @JsonProperty(value = "owners")
    @ArraySchema(arraySchema = @Schema(description = "The (bare) JIDs of the users that have an owner affiliation with the room. When creating a room without owners, the 'admin' user is made owner."), schema = @Schema(example = "admin@example.org"))
    public List<String> getOwners() {
        return owners;
    }

    @XmlElementWrapper(name = "ownerGroups")
    @XmlElement(name = "ownerGroup")
    @JsonProperty(value = "ownerGroups")
    @ArraySchema(arraySchema = @Schema(description = "The names of the user groups that have an owner affiliation with the room."), schema = @Schema(example = "Management"))
    public List<String> getOwnerGroups() {
        return ownerGroups;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public void setOwnerGroups(List<String> ownerGroups) {
        this.ownerGroups = ownerGroups;
    }

    @XmlElementWrapper(name = "members")
    @XmlElement(name = "member")
    @JsonProperty(value = "members")
    @ArraySchema(arraySchema = @Schema(description = "The (bare) JIDs of the users that have a member affiliation with the room."), schema = @Schema(example = "john@example.org"))
    public List<String> getMembers() {
        return members;
    }

    @XmlElementWrapper(name = "memberGroups")
    @XmlElement(name = "memberGroup")
    @JsonProperty(value = "memberGroups")
    @ArraySchema(arraySchema = @Schema(description = "The names of the user groups that have a member affiliation with the room."), schema = @Schema(example = "Sales"))
    public List<String> getMemberGroups() {
        return memberGroups;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void setMemberGroups(List<String> memberGroups) {
        this.memberGroups = memberGroups;
    }

    @XmlElementWrapper(name = "outcasts")
    @XmlElement(name = "outcast")
    @JsonProperty(value = "outcasts")
    @ArraySchema(arraySchema = @Schema(description = "The (bare) JIDs of the users that have an outcast affiliation with the room: users that are banned from the room."), schema = @Schema(example = "spammer@example.org"))
    public List<String> getOutcasts() {
        return outcasts;
    }

    @XmlElementWrapper(name = "outcastGroups")
    @XmlElement(name = "outcastGroup")
    @JsonProperty(value = "outcastGroups")
    @ArraySchema(arraySchema = @Schema(description = "The names of the user groups that have an outcast affiliation with the room."), schema = @Schema(example = "Banned"))
    public List<String> getOutcastGroups() {
        return outcastGroups;
    }

    public void setOutcasts(List<String> outcasts) {
        this.outcasts = outcasts;
    }

    public void setOutcastGroups(List<String> outcastGroups) {
        this.outcastGroups = outcastGroups;
    }

    @XmlElementWrapper(name = "admins")
    @XmlElement(name = "admin")
    @JsonProperty(value = "admins")
    @ArraySchema(arraySchema = @Schema(description = "The (bare) JIDs of the users that have an admin affiliation with the room."), schema = @Schema(example = "jane@example.org"))
    public List<String> getAdmins() {
        return admins;
    }

    @XmlElementWrapper(name = "adminGroups")
    @XmlElement(name = "adminGroup")
    @JsonProperty(value = "adminGroups")
    @ArraySchema(arraySchema = @Schema(description = "The names of the user groups that have an admin affiliation with the room."), schema = @Schema(example = "Moderators"))
    public List<String> getAdminGroups() {
        return adminGroups;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public void setAdminGroups(List<String> adminGroups) {
        this.adminGroups = adminGroups;
    }

}
