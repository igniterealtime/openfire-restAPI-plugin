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

package org.jivesoftware.openfire.plugin.rest.controller;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.group.ConcurrentGroupList;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.utils.MUCRoomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.UserUtils;
import org.jivesoftware.util.AlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * The Class MUCRoomController.
 */
public class MUCRoomController {
    private static Logger LOG = LoggerFactory.getLogger(MUCRoomController.class);

    /** The Constant INSTANCE. */
    public static final MUCRoomController INSTANCE = new MUCRoomController();

    /**
     * Gets the single instance of MUCRoomController.
     *
     * @return single instance of MUCRoomController
     */
    public static MUCRoomController getInstance() {
        return INSTANCE;
    }
    private static final PluginManager pluginManager = XMPPServer.getInstance().getPluginManager();
    private static final RESTServicePlugin plugin = (RESTServicePlugin) pluginManager.getPlugin("restapi");

    public static void log(String logMessage) {
        if (plugin.isServiceLoggingEnabled())
            LOG.info(logMessage);
    }

    /**
     * Gets the chat rooms.
     *
     * @param serviceName
     *            the service name
     * @param channelType
     *            the channel type
     * @param roomSearch
     *            the room search
     * @return the chat rooms
     */
    public MUCRoomEntities getChatRooms(String serviceName, String channelType, String roomSearch, boolean expand) {
        log("Get the chat rooms");
        final MultiUserChatService service = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName);
        Set<String> roomNames = service.getAllRoomNames();

        List<MUCRoomEntity> mucRoomEntities = new ArrayList<MUCRoomEntity>();

        for (String roomName : roomNames) {
            if (roomSearch != null) {
                if (!roomName.contains(roomSearch)) {
                    continue;
                }
            }

            final MUCRoom chatRoom = service.getChatRoom(roomName);
            if (channelType.equals(MUCChannelType.ALL)) {
                mucRoomEntities.add(convertToMUCRoomEntity(chatRoom, expand));
            } else if (channelType.equals(MUCChannelType.PUBLIC) && chatRoom.isPublicRoom()) {
                mucRoomEntities.add(convertToMUCRoomEntity(chatRoom, expand));
            }
        }

        return new MUCRoomEntities(mucRoomEntities);
    }

    /**
     * Gets the chat room.
     *
     * @param roomName
     *            the room name
     * @param serviceName
     *            the service name
     * @return the chat room
     * @throws ServiceException
     *             the service exception
     */
    public MUCRoomEntity getChatRoom(String roomName, String serviceName, boolean expand) throws ServiceException {
        log("Get the chat room: " + roomName);
        MUCRoom chatRoom = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName);

        if (chatRoom == null) {
            throw new ServiceException("Could not find the chat room", roomName, ExceptionType.ROOM_NOT_FOUND, Response.Status.NOT_FOUND);
        }

        MUCRoomEntity mucRoomEntity = convertToMUCRoomEntity(chatRoom, expand);
        return mucRoomEntity;
    }

    /**
     * Delete chat room.
     *
     * @param roomName
     *            the room name
     * @param serviceName
     *            the service name
     * @throws ServiceException
     *             the service exception
     */
    public void deleteChatRoom(String roomName, String serviceName) throws ServiceException {
        log("Delete the chat room: " + roomName);
        MUCRoom chatRoom = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());

        if (chatRoom != null) {
            chatRoom.destroyRoom(null, null);
        } else {
            throw new ServiceException("Could not remove the channel", roomName, ExceptionType.ROOM_NOT_FOUND, Response.Status.NOT_FOUND);
        }
    }

    /**
     * Creates the chat room.
     *
     * @param serviceName
     *            the service name
     * @param mucRoomEntity
     *            the MUC room entity
     * @throws ServiceException
     *             the service exception
     */
    public void createChatRoom(String serviceName, MUCRoomEntity mucRoomEntity) throws ServiceException {
        log("Create a chat room: " + mucRoomEntity.getRoomName());
        try {
            createRoom(mucRoomEntity, serviceName);
        } catch (NotAllowedException | ForbiddenException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (AlreadyExistsException e) {
            throw new ServiceException("Could not create the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.ALREADY_EXISTS, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Update chat room.
     *
     * @param roomName
     *            the room name
     * @param serviceName
     *            the service name
     * @param mucRoomEntity
     *            the MUC room entity
     * @throws ServiceException
     *             the service exception
     */
    public void updateChatRoom(String roomName, String serviceName, MUCRoomEntity mucRoomEntity)
            throws ServiceException {
        log("Update a chat room: " + mucRoomEntity.getRoomName());
        try {
            // If the room name is different throw exception
            if (!roomName.equals(mucRoomEntity.getRoomName())) {
                throw new ServiceException(
                        "Could not update the channel. The room name is different to the entity room name.", roomName,
                        ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }
            createRoom(mucRoomEntity, serviceName);
        } catch (NotAllowedException | ForbiddenException e) {
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (AlreadyExistsException e) {
            throw new ServiceException("Could not update the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.ALREADY_EXISTS, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Creates the room.
     *
     * @param mucRoomEntity
     *            the MUC room entity
     * @param serviceName
     *            the service name
     * @throws NotAllowedException
     *             the not allowed exception
     * @throws ForbiddenException
     *             the forbidden exception
     * @throws ConflictException
     *             the conflict exception
     * @throws AlreadyExistsException
     */
    private void createRoom(MUCRoomEntity mucRoomEntity, String serviceName) throws NotAllowedException,
            ForbiddenException, ConflictException, AlreadyExistsException {
        log("Create a chat room: " + mucRoomEntity.getRoomName());
        // Set owner
        JID owner = XMPPServer.getInstance().createJID("admin", null);
        if (mucRoomEntity.getOwners() != null && mucRoomEntity.getOwners().size() > 0) {
            owner = new JID(mucRoomEntity.getOwners().get(0));
        } else {
            List<String> owners = new ArrayList<String>();
            owners.add(owner.toBareJID());
            mucRoomEntity.setOwners(owners);
        }

        //	Check if chat service is available, if not create a new one
        boolean serviceRegistered = XMPPServer.getInstance().getMultiUserChatManager().isServiceRegistered(serviceName);
        if(!serviceRegistered) {
            XMPPServer.getInstance().getMultiUserChatManager().createMultiUserChatService(serviceName, serviceName, false);
        }

        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(mucRoomEntity.getRoomName().toLowerCase(), owner);

        // Set values
        room.setNaturalLanguageName(mucRoomEntity.getNaturalName());
        room.setSubject(mucRoomEntity.getSubject());
        room.setDescription(mucRoomEntity.getDescription());
        room.setPassword(mucRoomEntity.getPassword());
        room.setPersistent(mucRoomEntity.isPersistent());
        room.setPublicRoom(mucRoomEntity.isPublicRoom());
        room.setRegistrationEnabled(mucRoomEntity.isRegistrationEnabled());
        room.setCanAnyoneDiscoverJID(mucRoomEntity.isCanAnyoneDiscoverJID());
        room.setCanOccupantsChangeSubject(mucRoomEntity.isCanOccupantsChangeSubject());
        room.setCanOccupantsInvite(mucRoomEntity.isCanOccupantsInvite());
        room.setChangeNickname(mucRoomEntity.isCanChangeNickname());
        room.setModificationDate(mucRoomEntity.getModificationDate());
        room.setLogEnabled(mucRoomEntity.isLogEnabled());
        room.setLoginRestrictedToNickname(mucRoomEntity.isLoginRestrictedToNickname());
        room.setMaxUsers(mucRoomEntity.getMaxUsers());
        room.setMembersOnly(mucRoomEntity.isMembersOnly());
        room.setModerated(mucRoomEntity.isModerated());

        // Set broadcast presence roles
        if (mucRoomEntity.getBroadcastPresenceRoles() != null) {
            room.setRolesToBroadcastPresence(MUCRoomUtils.convertStringsToRoles(mucRoomEntity.getBroadcastPresenceRoles()));
        } else {
            room.setRolesToBroadcastPresence(new ArrayList<>());
        }
        // Set all roles
        if (!equalToAffiliations(room, mucRoomEntity)) {
            setRoles(room, mucRoomEntity);
        }

        // Set creation date
        if (mucRoomEntity.getCreationDate() != null) {
            room.setCreationDate(mucRoomEntity.getCreationDate());
        } else {
            room.setCreationDate(new Date());
        }

        // Set modification date
        if (mucRoomEntity.getModificationDate() != null) {
            room.setModificationDate(mucRoomEntity.getModificationDate());
        } else {
            room.setModificationDate(new Date());
        }

        // Unlock the room, because the default configuration lock the room.  		
        room.unlock(room.getRole());

        // Save the room to the DB if the room should be persistent
        if (room.isPersistent()) {
            room.saveToDB();
        }
    }

    private boolean equalToAffiliations(MUCRoom room, MUCRoomEntity mucRoomEntity) {
        if (mucRoomEntity == null || room == null) {
            return false;
        }
        Set<String> admins = mucRoomEntity.getAdmins() != null ? new HashSet<>(mucRoomEntity.getAdmins()) : new HashSet<>();
        Set<String> owners = mucRoomEntity.getOwners() != null ? new HashSet<>(mucRoomEntity.getOwners()) : new HashSet<>();
        Set<String> members = mucRoomEntity.getMembers() != null ? new HashSet<>(mucRoomEntity.getMembers()) : new HashSet<>();
        Set<String> outcasts = mucRoomEntity.getOutcasts() != null ? new HashSet<>(mucRoomEntity.getOutcasts()) : new HashSet<>();

        Set<String> roomAdmins = room.getAdmins() != null ? new HashSet<>(MUCRoomUtils.convertJIDsToStringList(room.getAdmins())) : new HashSet<>();
        Set<String> roomOwners = room.getOwners() != null ? new HashSet<>(MUCRoomUtils.convertJIDsToStringList(room.getOwners())) : new HashSet<>();
        Set<String> roomMembers = room.getMembers() != null ? new HashSet<>(MUCRoomUtils.convertJIDsToStringList(room.getMembers())) : new HashSet<>();
        Set<String> roomOutcasts = room.getOutcasts() != null ? new HashSet<>(MUCRoomUtils.convertJIDsToStringList(room.getOutcasts())) : new HashSet<>();
        return admins.equals(roomAdmins)
            && owners.equals(roomOwners)
            && members.equals(roomMembers)
            && outcasts.equals(roomOutcasts);
    }

    /**
     * Gets the room participants.
     *
     * @param roomName
     *            the room name
     * @param serviceName
     *            the service name
     * @return the room participants
     */
    public ParticipantEntities getRoomParticipants(String roomName, String serviceName) {
        log("Get room participants for room: " + roomName);
        ParticipantEntities participantEntities = new ParticipantEntities();
        List<ParticipantEntity> participants = new ArrayList<ParticipantEntity>();

        Collection<MUCRole> serverParticipants = XMPPServer.getInstance().getMultiUserChatManager()
                .getMultiUserChatService(serviceName).getChatRoom(roomName).getParticipants();

        for (MUCRole role : serverParticipants) {
            ParticipantEntity participantEntity = new ParticipantEntity();
            participantEntity.setJid(role.getRoleAddress().toFullJID());
            participantEntity.setRole(role.getRole().name());
            participantEntity.setAffiliation(role.getAffiliation().name());

            participants.add(participantEntity);
        }

        participantEntities.setParticipants(participants);
        return participantEntities;
    }

    /**
     * Gets the room occupants.
     *
     * @param roomName
     *            the room name
     * @param serviceName
     *            the service name
     * @return the room occupants
     */
    public OccupantEntities getRoomOccupants(String roomName, String serviceName) {
        log("Get room occupants for room: " + roomName);
        OccupantEntities occupantEntities = new OccupantEntities();
        List<OccupantEntity> occupants = new ArrayList<OccupantEntity>();

        Collection<MUCRole> serverOccupants = XMPPServer.getInstance().getMultiUserChatManager()
                .getMultiUserChatService(serviceName).getChatRoom(roomName).getOccupants();

        for (MUCRole role : serverOccupants) {
            OccupantEntity occupantEntity = new OccupantEntity();
            occupantEntity.setJid(role.getRoleAddress().toFullJID());
            occupantEntity.setUserAddress(role.getUserAddress().toFullJID());
            occupantEntity.setRole(role.getRole().name());
            occupantEntity.setAffiliation(role.getAffiliation().name());

            occupants.add(occupantEntity);
        }

        occupantEntities.setOccupants(occupants);
        return occupantEntities;
    }

    /**
     * Gets the room chat history.
     *
     * @param roomName
     *            the room name
     * @param serviceName
     *            the service name
     * @return the room chat history
     */
    public MUCRoomMessageEntities getRoomHistory(String roomName, String serviceName) throws ServiceException {
        log("Get room history for room: " + roomName);
        MUCRoomMessageEntities mucRoomMessageEntities = new MUCRoomMessageEntities();
        List<MUCRoomMessageEntity> listMessages = new ArrayList<>();

        MultiUserChatService mucS =XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName);
        MUCRoom chatRoom = mucS.getChatRoom(roomName);
        if (chatRoom == null) {
            throw new ServiceException("Could not find the chat room", roomName, ExceptionType.ROOM_NOT_FOUND, Response.Status.NOT_FOUND);
        }

        MUCRoomHistory mucRH = chatRoom.getRoomHistory();
        Iterator<Message> messageHistory = mucRH.getMessageHistory();

        while (messageHistory.hasNext()) {
            Message message = messageHistory.next();

            MUCRoomMessageEntity mucMsgEntity = new MUCRoomMessageEntity();
            if (message.getTo()!=null && message.getTo().toString().length()!=0)
                mucMsgEntity.setTo(message.getTo().toString());
            if (message.getFrom()!=null && message.getFrom().toString().length()!=0)
                mucMsgEntity.setFrom(message.getFrom().toFullJID());
            if (message.getType()!=null && message.getType().toString().length()!=0)
                mucMsgEntity.setType(message.getType().name());
            if (message.getBody()!=null && message.getBody().length()!=0)
                mucMsgEntity.setBody(message.getBody());

            Element delay = message.getChildElement("delay","urn:xmpp:delay");
            if (delay!=null) {
                mucMsgEntity.setDelayStamp(delay.attributeValue("stamp"));
                String delayFrom = delay.attributeValue("from");
                if (delayFrom!=null)
                    mucMsgEntity.setDelayFrom(delayFrom);
            }
            listMessages.add(mucMsgEntity);
        }
        mucRoomMessageEntities.setMessages(listMessages);
        return mucRoomMessageEntities;
    }

    /**
     * Invites the user to the MUC room.
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param jid
     *            the jid to invite
     * @throws ServiceException
     *             the service exception
     */
    public void inviteUser(String serviceName, String roomName, String jid, MUCInvitationEntity mucInvitationEntity)
            throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
            .getChatRoom(roomName.toLowerCase());

        try {
            room.sendInvitation(UserUtils.checkAndGetJID(jid), mucInvitationEntity.getReason(), room.getRole(), null);
        } catch (ForbiddenException | CannotBeInvitedException e) {
            throw new ServiceException("Could not invite user", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        }
    }


    /**
     * Convert to MUC room entity.
     *
     * @param room
     *            the room
     * @return the MUC room entity
     */
    public MUCRoomEntity convertToMUCRoomEntity(MUCRoom room, boolean expand) {
        MUCRoomEntity mucRoomEntity = new MUCRoomEntity(room.getNaturalLanguageName(), room.getName(),
                room.getDescription());

        mucRoomEntity.setSubject(room.getSubject());
        mucRoomEntity.setCanAnyoneDiscoverJID(room.canAnyoneDiscoverJID());
        mucRoomEntity.setCanChangeNickname(room.canChangeNickname());
        mucRoomEntity.setCanOccupantsChangeSubject(room.canOccupantsChangeSubject());
        mucRoomEntity.setCanOccupantsInvite(room.canOccupantsInvite());

        mucRoomEntity.setPublicRoom(room.isPublicRoom());
        mucRoomEntity.setPassword(room.getPassword());
        mucRoomEntity.setPersistent(room.isPersistent());
        mucRoomEntity.setRegistrationEnabled(room.isRegistrationEnabled());
        mucRoomEntity.setLogEnabled(room.isLogEnabled());
        mucRoomEntity.setLoginRestrictedToNickname(room.isLoginRestrictedToNickname());
        mucRoomEntity.setMaxUsers(room.getMaxUsers());
        mucRoomEntity.setMembersOnly(room.isMembersOnly());
        mucRoomEntity.setModerated(room.isModerated());

        ConcurrentGroupList<JID> owners = new ConcurrentGroupList<JID>(room.getOwners());
        ConcurrentGroupList<JID> admins = new ConcurrentGroupList<JID>(room.getAdmins());
        ConcurrentGroupList<JID> members = new ConcurrentGroupList<JID>(room.getMembers());
        ConcurrentGroupList<JID> outcasts = new ConcurrentGroupList<JID>(room.getOutcasts());

        if (expand) {
            for(Group ownerGroup : owners.getGroups()) {
                owners.addAllAbsent(ownerGroup.getAll());
            }
            for(Group adminGroup : admins.getGroups()) {
                admins.addAllAbsent(adminGroup.getAll());
            }
            for(Group memberGroup : members.getGroups()) {
                members.addAllAbsent(memberGroup.getAll());
            }
            for(Group outcastGroup : outcasts.getGroups()) {
                outcasts.addAllAbsent(outcastGroup.getAll());
            }
        }

        mucRoomEntity.setOwners(MUCRoomUtils.convertJIDsToStringList(owners));
        mucRoomEntity.setAdmins(MUCRoomUtils.convertJIDsToStringList(admins));
        mucRoomEntity.setMembers(MUCRoomUtils.convertJIDsToStringList(members));
        mucRoomEntity.setOutcasts(MUCRoomUtils.convertJIDsToStringList(outcasts));

        mucRoomEntity.setOwnerGroups(MUCRoomUtils.convertGroupsToStringList(owners.getGroups()));
        mucRoomEntity.setAdminGroups(MUCRoomUtils.convertGroupsToStringList(admins.getGroups()));
        mucRoomEntity.setMemberGroups(MUCRoomUtils.convertGroupsToStringList(members.getGroups()));
        mucRoomEntity.setOutcastGroups(MUCRoomUtils.convertGroupsToStringList(outcasts.getGroups()));

        mucRoomEntity.setBroadcastPresenceRoles(MUCRoomUtils.convertRolesToStringList(room.getRolesToBroadcastPresence()));

        mucRoomEntity.setCreationDate(room.getCreationDate());
        mucRoomEntity.setModificationDate(room.getModificationDate());

        return mucRoomEntity;
    }

    /**
     * Reset roles.
     *
     * @param room
     *            the room
     * @param mucRoomEntity
     *            the muc room entity
     * @throws ForbiddenException
     *             the forbidden exception
     * @throws NotAllowedException
     *             the not allowed exception
     * @throws ConflictException
     *             the conflict exception
     */
    private void setRoles(MUCRoom room, MUCRoomEntity mucRoomEntity) throws ForbiddenException, NotAllowedException,
            ConflictException {
        List<JID> roles = new ArrayList<JID>();
        Collection<JID> existingOwners = new ArrayList<JID>();
        List<JID> mucRoomEntityOwners = new ArrayList<JID>();

        for (String ownerJid : mucRoomEntity.getOwners()) {
            mucRoomEntityOwners.add(UserUtils.checkAndGetJID(ownerJid));
        }
        Collection<JID> owners = new ArrayList<JID>(room.getOwners());

        // Find same owners
        for (JID jid : owners) {
            if (mucRoomEntityOwners.contains(jid)) {
                existingOwners.add(jid);
            }
        }

        // Don't delete the same owners
        owners.removeAll(existingOwners);

        // Collect all roles to reset
        roles.addAll(owners);
        roles.addAll(room.getAdmins());
        roles.addAll(room.getMembers());
        roles.addAll(room.getOutcasts());

        for (JID jid : roles) {
            room.addNone(jid, room.getRole());
        }

        for (String ownerJid : mucRoomEntity.getOwners()) {
            room.addOwner(UserUtils.checkAndGetJID(ownerJid), room.getRole());
        }
        if (mucRoomEntity.getAdmins() != null) {
            for (String adminJid : mucRoomEntity.getAdmins()) {
                room.addAdmin(UserUtils.checkAndGetJID(adminJid), room.getRole());
            }
        }
        if (mucRoomEntity.getMembers() != null) {
            for (String memberJid : mucRoomEntity.getMembers()) {
                room.addMember(UserUtils.checkAndGetJID(memberJid), null, room.getRole());
            }
        }
        if (mucRoomEntity.getOutcasts() != null) {
            for (String outcastJid : mucRoomEntity.getOutcasts()) {
                room.addOutcast(UserUtils.checkAndGetJID(outcastJid), null, room.getRole());
            }
        }
    }

    /**
     * Adds the admin.
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param jid
     *            the jid
     * @throws ServiceException
     *             the service exception
     */
    public void addAdmin(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addAdmin(UserUtils.checkAndGetJID(jid), room.getRole());
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add admin", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add admin", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Adds the owner.
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param jid
     *            the jid
     * @throws ServiceException
     *             the service exception
     */
    public void addOwner(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addOwner(UserUtils.checkAndGetJID(jid), room.getRole());
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not add owner", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        }
    }

    /**
     * Adds the member.
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param jid
     *            the jid
     * @throws ServiceException
     *             the service exception
     */
    public void addMember(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addMember(UserUtils.checkAndGetJID(jid), null, room.getRole());
        } catch (ForbiddenException | ConflictException e) {
            throw new ServiceException("Could not add member", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        }
    }

    /**
     * Adds the outcast.
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param jid
     *            the jid
     * @throws ServiceException
     *             the service exception
     */
    public void addOutcast(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
            room.addOutcast(UserUtils.checkAndGetJID(jid), null, room.getRole());
        } catch (NotAllowedException | ForbiddenException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Delete affiliation.
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param jid
     *            the jid
     * @throws ServiceException
     *             the service exception
     */
    public void deleteAffiliation(String serviceName, String roomName, String jid) throws ServiceException {
        MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(serviceName)
                .getChatRoom(roomName.toLowerCase());
        try {
              JID userJid = UserUtils.checkAndGetJID(jid);

              // Send a presence to other room members
              List<Presence> addNonePresence = room.addNone(userJid, room.getRole());
              for (Presence presence : addNonePresence) {
                  MUCRoomUtils.send(room, presence, room.getRole());
              }
        } catch (ForbiddenException e) {
            throw new ServiceException("Could not delete affiliation", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not delete affiliation", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Completely unknown implementation of MUCRoom::send
            throw new ServiceException("Could not delete affiliation", jid, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }
}
