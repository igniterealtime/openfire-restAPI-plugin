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
import org.jivesoftware.openfire.group.ConcurrentGroupList;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.muc.spi.MUCRoomSearchInfo;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.utils.MUCRoomUtils;
import org.jivesoftware.openfire.plugin.rest.utils.UserUtils;
import org.jivesoftware.util.AlreadyExistsException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The Class MUCRoomController.
 */
public class MUCRoomController {
    private static final Logger LOG = LoggerFactory.getLogger(MUCRoomController.class);

    /** The Constant INSTANCE. */
    private static MUCRoomController INSTANCE = null;

    /**
     * Gets the single instance of MUCRoomController.
     *
     * @return single instance of MUCRoomController
     */
    public static MUCRoomController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MUCRoomController();
        }
        return INSTANCE;
    }

    /**
     * @param instance the mock/stub/spy controller to use.
     * @deprecated - for test use only
     */
    @Deprecated
    public static void setInstance(final MUCRoomController instance) {
        MUCRoomController.INSTANCE = instance;
    }

    public static void log(String logMessage) {
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            LOG.info(logMessage);
        }
    }

    /**
     * Returns the chat room instance for the provided name.
     *
     * This method returns any room that matches the provided room name case-insensitively, but prefers a case-sensitive
     * match. It will <em>not</em> evaluate more than one service, in case more than one match the provided service name
     * case-insensitively.
     *
     * @param serviceName The name of the service that contains the chat room.
     * @param roomName The name of the chat room.
     * @return The chat room instance
     * @throws ServiceException When no service for the provided name exists, or when that service does not contain a chat room of the provided name.
     */
    @Nonnull
    protected static MUCRoom getRoom(@Nonnull final String serviceName, @Nonnull final String roomName) throws ServiceException
    {
        final MultiUserChatService service = MUCServiceController.getService(serviceName);

        // Try finding an exact match first (iterating over all chat room names is pretty resource intensive, so do that
        // only as a last resort.
        MUCRoom room = service.getChatRoom(roomName);
        if (room == null && !roomName.equals(roomName.toLowerCase())) {
            // Try avoiding the need for an all-names lookup, by first attempting to get the room by its lowercase name.
            room = service.getChatRoom(roomName.toLowerCase());
        }
        if (room == null) {
            // As a last resort (because it's resource intensive), iterate over all room(name)s for this service.
            final Set<String> nonExactMatches = service.getAllRoomNames().stream().filter(name -> name.equalsIgnoreCase(roomName)).collect(Collectors.toSet());
            for (final String nonExactMatch : nonExactMatches) {
                room = service.getChatRoom(nonExactMatch);
                if (room != null) {
                    LOG.info("Could not find a case-sensitive match for room '{}', but did find a case-insensitive match: '{}'. Case insensitive lookups are resource intensive. Consider modifying your search query.", roomName, room.getName());
                    break;
                }
            }
        }
        if (room == null) {
            throw new ServiceException("Chat room does not exist or is not accessible.", roomName, ExceptionType.ROOM_NOT_FOUND, Response.Status.NOT_FOUND);
        }
        return room;
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
    public MUCRoomEntities getChatRooms(String serviceName, String channelType, String roomSearch, boolean expand) throws ServiceException
    {
        log("Get the chat rooms");
        final MultiUserChatService service = MUCServiceController.getService(serviceName);
        Collection<MUCRoomSearchInfo> roomsInfo = service.getAllRoomSearchInfo();

        List<MUCRoomEntity> mucRoomEntities = new ArrayList<>();

        for (MUCRoomSearchInfo roomInfo : roomsInfo) {
            String roomName = roomInfo.getName();
            if (roomSearch != null) {
                if (!StringUtils.containsIgnoringCase(roomInfo.getName(), roomSearch) &&
                    !StringUtils.containsIgnoringCase(roomInfo.getNaturalLanguageName(), roomSearch)) {
                    continue;
                }
            }

            final MUCRoom chatRoom = service.getChatRoom(roomName);
            if (chatRoom == null) {
                LOG.warn("Cannot get room '{}' from service '{}' even though service's 'getAllRoomNames()' returns this name.", roomName, serviceName);
                continue;
            }

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
        final MUCRoom chatRoom = getRoom(serviceName, roomName);
        return convertToMUCRoomEntity(chatRoom, expand);
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
        final MUCRoom chatRoom = getRoom(serviceName, roomName);
        chatRoom.destroyRoom(null, null);
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
     * Creates multiple chat rooms.
     *
     * @param serviceName
     *              the service name
     * @param mucRoomEntities
     *              the chat rooms to create
     * @return
     *              a report detailing which creates were successful and which weren't
     * @throws ServiceException
     *              the service exception
     */
    public RoomCreationResultEntities createMultipleChatRooms(String serviceName, MUCRoomEntities mucRoomEntities) throws ServiceException {
        List<MUCRoomEntity> roomsToCreate = mucRoomEntities.getMucRooms();
        log("Create " + roomsToCreate.size() + " chat rooms");
        List<RoomCreationResultEntity> results = new ArrayList<>();
        for (MUCRoomEntity roomToCreate : roomsToCreate) {
            RoomCreationResultEntity result = new RoomCreationResultEntity();
            result.setRoomName(roomToCreate.getRoomName());
            try {
                createRoom(roomToCreate, serviceName);
                result.setResultType(RoomCreationResultEntity.RoomCreationResultType.Success);
                result.setMessage("Room was successfully created");
            } catch (AlreadyExistsException e) {
                result.setResultType(RoomCreationResultEntity.RoomCreationResultType.Success);
                result.setMessage("Room already existed and therefore not created again");
            } catch (NotAllowedException | ForbiddenException | ConflictException e) {
                result.setResultType(RoomCreationResultEntity.RoomCreationResultType.Failure);
                result.setMessage("Room creation failed due to " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            results.add(result);
        }
        return new RoomCreationResultEntities(results);
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
     *             the already exists exception
     */
    private void createRoom(MUCRoomEntity mucRoomEntity, String serviceName) throws NotAllowedException,
        ForbiddenException, ConflictException, AlreadyExistsException, ServiceException
    {
        log("Create a chat room: " + mucRoomEntity.getRoomName());
        // Set owner
        JID owner = XMPPServer.getInstance().createJID("admin", null);
        if (mucRoomEntity.getOwners() != null && mucRoomEntity.getOwners().size() > 0) {
            owner = new JID(mucRoomEntity.getOwners().get(0));
        } else {
            List<String> owners = new ArrayList<>();
            owners.add(owner.toBareJID());
            mucRoomEntity.setOwners(owners);
        }

        //	Check if chat service is available, if not create a new one
        boolean serviceRegistered = XMPPServer.getInstance().getMultiUserChatManager().isServiceRegistered(serviceName);
        if(!serviceRegistered) {
            XMPPServer.getInstance().getMultiUserChatManager().createMultiUserChatService(serviceName, serviceName, false);
        }

        MUCRoom room = MUCServiceController.getService(serviceName).getChatRoom(mucRoomEntity.getRoomName().toLowerCase(), owner);

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
        room.setCanSendPrivateMessage(mucRoomEntity.getAllowPM());

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

        MUCServiceController.getService(serviceName).syncChatRoom(room);
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
    public ParticipantEntities getRoomParticipants(String roomName, String serviceName) throws ServiceException
    {
        log("Get room participants for room: " + roomName);
        ParticipantEntities participantEntities = new ParticipantEntities();
        List<ParticipantEntity> participants = new ArrayList<>();

        Collection<MUCRole> serverParticipants = getRoom(serviceName, roomName).getParticipants();

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
    public OccupantEntities getRoomOccupants(String roomName, String serviceName) throws ServiceException
    {
        log("Get room occupants for room: " + roomName);
        OccupantEntities occupantEntities = new OccupantEntities();
        List<OccupantEntity> occupants = new ArrayList<>();

        Collection<MUCRole> serverOccupants = getRoom(serviceName, roomName).getOccupants();

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

        MUCRoom chatRoom = getRoom(serviceName, roomName);
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
        MUCRoom room = getRoom(serviceName, roomName);

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
        mucRoomEntity.setAllowPM(room.canSendPrivateMessage());

        ConcurrentGroupList<JID> owners = new ConcurrentGroupList<>(room.getOwners());
        ConcurrentGroupList<JID> admins = new ConcurrentGroupList<>(room.getAdmins());
        ConcurrentGroupList<JID> members = new ConcurrentGroupList<>(room.getMembers());
        ConcurrentGroupList<JID> outcasts = new ConcurrentGroupList<>(room.getOutcasts());

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
        List<JID> roles = new ArrayList<>();
        Collection<JID> existingOwners = new ArrayList<>();

        List<JID> mucRoomEntityOwners = MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners());
        Collection<JID> owners = new ArrayList<>(room.getOwners());

        // Find same owners
        for (JID jid : owners) {
            if (mucRoomEntityOwners.contains(jid)) {
                existingOwners.add(jid);
            }
        }

        // Don't delete the same owners
        owners.removeAll(existingOwners);
        room.addOwners(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners()), room.getRole());

        // Collect all roles to reset
        roles.addAll(owners);
        roles.addAll(room.getAdmins());
        roles.addAll(room.getMembers());
        roles.addAll(room.getOutcasts());

        for (JID jid : roles) {
            room.addNone(jid, room.getRole());
        }

        room.addOwners(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners()), room.getRole());
        if (mucRoomEntity.getAdmins() != null) {
            room.addAdmins(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getAdmins()), room.getRole());
        }
        if (mucRoomEntity.getMembers() != null) {
            for (String memberJid : mucRoomEntity.getMembers()) {
                room.addMember(new JID(memberJid), null, room.getRole());
            }
        }
        if (mucRoomEntity.getOutcasts() != null) {
            for (String outcastJid : mucRoomEntity.getOutcasts()) {
                room.addOutcast(new JID(outcastJid), null, room.getRole());
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
        MUCRoom room = getRoom(serviceName, roomName);
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
        MUCRoom room = getRoom(serviceName, roomName);
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
        MUCRoom room = getRoom(serviceName, roomName);
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
        MUCRoom room = getRoom(serviceName, roomName);
        try {
            room.addOutcast(UserUtils.checkAndGetJID(jid), null, room.getRole());
        } catch (NotAllowedException | ForbiddenException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not add outcast", jid, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Returns a collection of user addresses of every user that has a particular affiliation to a room.
     *
     * @param serviceName
     *            the service name of the room
     * @param roomName
     *            the name of the room for which to return affiliated users
     * @param affiliation
     *            the affiliation for which to return all users
     * @return All users that have the specified affiliation to the specified room.
     * @throws ServiceException On any issue looking up the room or its affiliated users.
     */
    public Collection<JID> getByAffiliation(@Nonnull final String serviceName, @Nonnull final String roomName, @Nonnull final MUCRole.Affiliation affiliation) throws ServiceException
    {
        final MUCRoom room = getRoom(serviceName, roomName);
        switch (affiliation) {
            case admin:
                return room.getAdmins();
            case member:
                return room.getMembers();
            case owner:
                return room.getOwners();
            case outcast:
                return room.getOutcasts();
            default:
                return room.getOccupants().stream()
                    .filter(o->affiliation.equals(o.getAffiliation()))
                    .map(MUCRole::getUserAddress)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Updates a list of users that have a particular affiliation to a room with a new list.
     *
     * This will remove all users as having an affiliation of this type with the room for users that are not in the
     * list of replacements.
     *
     * @param serviceName
     *            the service name of the room
     * @param roomName
     *            the name of the room for which to replace affiliated users
     * @param affiliation
     *            the affiliation for which to replace all users
     * @param jids
     *            the new list of affiliated users
     * @throws ServiceException On any issue looking up the room or changing its affiliated users.
     */
    public void replaceAffiliatedUsers(@Nonnull final String serviceName, @Nonnull final String roomName, @Nonnull final MUCRole.Affiliation affiliation, @Nonnull final Collection<String> jids) throws ServiceException
    {
        final Collection<JID> replacements = new HashSet<>();

        // Input validation.
        for (String replacement : jids) {
            try {
                replacements.add(new JID(replacement));
            } catch (IllegalArgumentException e) {
                throw new ServiceException("Unable to parse value as jid: " + replacement, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST, e);
            }
        }

        final Collection<JID> oldUsers = getByAffiliation(serviceName, roomName, affiliation);

        // The users to add are the replacements that aren't already in the old collection.
        final List<JID> toAdd = new ArrayList<>(replacements);
        toAdd.removeAll(oldUsers);

        // The users to remove are the old users that are no longer in the replacements.
        final List<JID> toRemove = new ArrayList<>(oldUsers);
        toRemove.removeAll(replacements);

        final MUCRoom room = getRoom(serviceName, roomName);
        try {
            // First, add all new affiliations (some affiliations aren't allowed to be empty, so removing things first could cause issues).
            switch (affiliation) {
                case admin:
                    room.addAdmins(toAdd, room.getRole());
                    break;

                case member:
                    for (final JID add : toAdd) {
                        room.addMember(add, null, room.getRole());
                    }
                    break;

                case owner:
                    room.addOwners(toAdd, room.getRole());
                    break;

                case outcast:
                    for (final JID add : toAdd) {
                        room.addOutcast(add, null, room.getRole());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unrecognized affiliation: " + affiliation);
            }

            // Next, remove the affiliations that are no longer wanted.
            for (final JID remove : toRemove) {
                room.addNone(remove, room.getRole());
            }
        } catch (ForbiddenException | NotAllowedException e) {
            throw new ServiceException("Forbidden to apply modification to list of " + affiliation, roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not apply modification to list of " + affiliation, roomName, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Adds to a list of users that have a particular affiliation to a room with a new list, without affecting the
     * pre-existing list.
     *
     * @param serviceName
     *            the service name of the room
     * @param roomName
     *            the name of the room for which to add affiliated users
     * @param affiliation
     *            the affiliation for which to add users
     * @param jids
     *            the list of additional affiliated users
     * @throws ServiceException On any issue looking up the room or changing its affiliated users.
     */
    public void addAffiliatedUsers(@Nonnull final String serviceName, @Nonnull final String roomName, @Nonnull final MUCRole.Affiliation affiliation, @Nonnull final Collection<String> jids) throws ServiceException
    {
        final Collection<JID> additions = new HashSet<>();

        // Input validation.
        for (String replacement : jids) {
            try {
                additions.add(new JID(replacement));
            } catch (IllegalArgumentException e) {
                throw new ServiceException("Unable to parse value as jid: " + replacement, roomName, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST, e);
            }
        }

        final Collection<JID> oldUsers = getByAffiliation(serviceName, roomName, affiliation);

        // The users to add are the additions that aren't already in the old collection.
        final List<JID> toAdd = new ArrayList<>(additions);
        toAdd.removeAll(oldUsers);

        final MUCRoom room = getRoom(serviceName, roomName);
        try {
            // Add all new affiliations.
            switch (affiliation) {
                case admin:
                    room.addAdmins(toAdd, room.getRole());
                    break;

                case member:
                    for (final JID add : toAdd) {
                        room.addMember(add, null, room.getRole());
                    }
                    break;

                case owner:
                    room.addOwners(toAdd, room.getRole());
                    break;

                case outcast:
                    for (final JID add : toAdd) {
                        room.addOutcast(add, null, room.getRole());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unrecognized affiliation: " + affiliation);
            }
        } catch (ForbiddenException | NotAllowedException e) {
            throw new ServiceException("Forbidden to apply modification to list of " + affiliation, roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            throw new ServiceException("Could not apply modification to list of " + affiliation, roomName, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
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
        MUCRoom room = getRoom(serviceName, roomName);
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
