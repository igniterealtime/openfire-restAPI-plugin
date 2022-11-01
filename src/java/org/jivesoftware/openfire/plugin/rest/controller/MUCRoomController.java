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
import org.jivesoftware.util.SystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Presence;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * The Class MUCRoomController.
 */
public class MUCRoomController {
    private static final Logger LOG = LoggerFactory.getLogger(MUCRoomController.class);

    /**
     * Names of MUC rooms _should_ be node-prepped. This, however, was not guaranteed the case in some versions of Openfire and this plugin.
     * Earlier versions of this plugin used a case-insensitive lookup to work around this. As this _should_ be unneeded, and is quite
     * resource intensive, this behavior has been made configurable (disabled by default).
     */
    public static final SystemProperty<Boolean> ROOM_NAME_CASE_INSENSITIVE_LOOKUP_ENABLED = SystemProperty.Builder.ofType(Boolean.class)
        .setPlugin("REST API")
        .setKey("plugin.restapi.muc.case-insensitive-lookup.enabled")
        .setDefaultValue(false)
        .setDynamic(true)
        .build();

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

    public static void log(String logMessage, Throwable t) {
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            LOG.info(logMessage, t);
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

        MUCRoom room = service.getChatRoom(JID.nodeprep(roomName));

        // Names of MUC rooms _should_ be node-prepped. This, however, was not guaranteed the case in some versions of Openfire and this plugin.
        // Earlier versions of this plugin used a case-insensitive lookup to work around this. As this _should_ be unneeded, and is quite
        // resource intensive, this behavior has been made configurable (disabled by default).
        if (room == null && ROOM_NAME_CASE_INSENSITIVE_LOOKUP_ENABLED.getValue())
        {
            // As a last resort (because it's resource intensive), iterate over all room(name)s for this service.
            for (String name : service.getAllRoomNames()) {
                if (name.equalsIgnoreCase(roomName) && (room = service.getChatRoom(roomName)) != null) {
                    LOG.info("Could not find a case-sensitive match for room '{}', but did find a case-insensitive match: '{}'. Case insensitive lookups are resource intensive. Verify that your database contains properly node-prepped MUC room names.", roomName, room.getName());
                    return room;
                } else if (JID.nodeprep(name).equalsIgnoreCase(roomName) && (room = service.getChatRoom(roomName)) != null) {
                    LOG.info("Could not find a case-sensitive match for room '{}', but did find a case-insensitive match after node-prepping: '{}'. Case insensitive lookups are resource intensive. Verify that your database contains properly node-prepped MUC room names.", roomName, room.getName());
                    return room;
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
                if (!StringUtils.containsIgnoringCase(roomInfo.getName(), JID.nodeprep(roomSearch)) &&
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
     * @param serviceName   the service name
     * @param mucRoomEntity the MUC room entity
     * @param sendInvitations   whether to send invitations to affiliated users
     * @throws ServiceException the service exception
     */
    public void createChatRoom(String serviceName, MUCRoomEntity mucRoomEntity, boolean sendInvitations) throws ServiceException {
        log("Create a chat room: " + mucRoomEntity.getRoomName());
        try {
            createRoom(mucRoomEntity, serviceName, sendInvitations);
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
     * @param sendInvitations
     *              whether to send invitations to affiliated users
     * @return
     *              a report detailing which creates were successful and which weren't
     * @throws ServiceException
     *              the service exception
     */
    public RoomCreationResultEntities createMultipleChatRooms(String serviceName, MUCRoomEntities mucRoomEntities, boolean sendInvitations) throws ServiceException {
        List<MUCRoomEntity> roomsToCreate = mucRoomEntities.getMucRooms();
        log("Create " + roomsToCreate.size() + " chat rooms");
        List<RoomCreationResultEntity> results = new ArrayList<>();
        for (MUCRoomEntity roomToCreate : roomsToCreate) {
            RoomCreationResultEntity result = new RoomCreationResultEntity();
            result.setRoomName(roomToCreate.getRoomName());
            try {
                createRoom(roomToCreate, serviceName, sendInvitations);
                result.setResultType(RoomCreationResultEntity.RoomCreationResultType.Success);
                result.setMessage("Room was successfully created");
            } catch (AlreadyExistsException e) {
                log("Already exists exception thrown while trying to create room: " + roomToCreate.getRoomName(), e);
                result.setResultType(RoomCreationResultEntity.RoomCreationResultType.Success);
                result.setMessage("Room already existed and therefore not created again");
            } catch (NotAllowedException | ForbiddenException | ConflictException e) {
                log("Failed to create room: " + roomToCreate.getRoomName(), e);
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
     * @param sendInvitations
     *            whether to send invitations to affiliated users
     * @throws ServiceException
     *             the service exception
     */
    public void updateChatRoom(String roomName, String serviceName, MUCRoomEntity mucRoomEntity, boolean sendInvitations)
            throws ServiceException {
        log("Update a chat room: " + mucRoomEntity.getRoomName());
        try {
            // If the room name is different throw exception
            if (!JID.nodeprep(roomName).equals(mucRoomEntity.getRoomName())) {
                throw new ServiceException(
                        "Could not update the channel. The room name is different to the entity room name.", roomName,
                        ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }
            createRoom(mucRoomEntity, serviceName, sendInvitations);
        } catch (NotAllowedException | ForbiddenException e) {
            log("Failed to update room: " + mucRoomEntity.getRoomName(), e);
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
        } catch (ConflictException e) {
            log("Failed to update room: " + mucRoomEntity.getRoomName(), e);
            throw new ServiceException("Could not update the channel", roomName, ExceptionType.NOT_ALLOWED, Response.Status.CONFLICT, e);
        } catch (AlreadyExistsException e) {
            log("Already exists exception thrown while trying to update room: " + mucRoomEntity.getRoomName(), e);
            throw new ServiceException("Could not update the channel", mucRoomEntity.getRoomName(),
                    ExceptionType.ALREADY_EXISTS, Response.Status.CONFLICT, e);
        }
    }

    /**
     * Creates the room.
     *
     * @param mucRoomEntity
     *             the MUC room entity
     * @param serviceName
     *             the service name
     * @param sendInvitations
     *             whether to send invitations to affiliated users
     * @throws NotAllowedException
     *             the not allowed exception
     * @throws ForbiddenException
     *             the forbidden exception
     * @throws ConflictException
     *             the conflict exception
     * @throws AlreadyExistsException
     *             the already exists exception
     */
    private void createRoom(MUCRoomEntity mucRoomEntity, String serviceName, boolean sendInvitations) throws NotAllowedException,
        ForbiddenException, ConflictException, AlreadyExistsException, ServiceException
    {
        log("Create or updating a chat room: " + mucRoomEntity.getRoomName());
        // Set owner
        JID owner = XMPPServer.getInstance().createJID("admin", null);
        if (mucRoomEntity.getOwners() != null && mucRoomEntity.getOwners().size() > 0) {
            owner = new JID(mucRoomEntity.getOwners().get(0));
        } else {
            log("Room '" + mucRoomEntity.getRoomName() + "' is being created/updated without an owner. Adding default owner (as having an owner is non-optional).");
            List<String> owners = new ArrayList<>();
            owners.add(owner.toBareJID());
            mucRoomEntity.setOwners(owners);
        }

        // Issue #159: Do not allow duplicate affiliations.
        final Collection<JID> allRequestedAffiliations = new LinkedList<>();
        if (mucRoomEntity.getOwners() != null) allRequestedAffiliations.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners()));
        if (mucRoomEntity.getAdmins() != null) allRequestedAffiliations.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getAdmins()));
        if (mucRoomEntity.getMembers() != null) allRequestedAffiliations.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getMembers()));
        if (mucRoomEntity.getOutcasts() != null) allRequestedAffiliations.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOutcasts()));
        final Set<JID> uniques = new HashSet<>();
        final Set<String> duplicates = allRequestedAffiliations.stream().filter(n -> !uniques.add(n)).map(JID::toString).collect(Collectors.toSet());
        if (!duplicates.isEmpty()) {
            throw new ConflictException("The requested room defines duplicate affiliations for the following entities: " + String.join(", ", duplicates));
        }

        //	Check if chat service is available, if not create a new one
        boolean serviceRegistered = XMPPServer.getInstance().getMultiUserChatManager().isServiceRegistered(serviceName);
        if(!serviceRegistered) {
            log("Creating a new service for the chat room that is being created: " + serviceName);
            XMPPServer.getInstance().getMultiUserChatManager().createMultiUserChatService(serviceName, serviceName, false);
        }

        log("Setting initial values for room that is being created/updated: " + mucRoomEntity.getRoomName());
        MUCRoom room = MUCServiceController.getService(serviceName).getChatRoom(mucRoomEntity.getRoomName(), owner);
        log("Room " + mucRoomEntity.getRoomName() + " is being " + (room.isLocked() ? "created" : "updated"));

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
        log("Setting roles for room that is being " + (room.isLocked() ? "created" : "updated") + ": " + mucRoomEntity.getRoomName());
        Collection<JID> allUsersWithNewAffiliations = null;
        if (!equalToAffiliations(room, mucRoomEntity)) {
            allUsersWithNewAffiliations = setRoles(room, mucRoomEntity);
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
        log("Unlocking room that is being " + (room.isLocked() ? "created" : "updated") + ": " + mucRoomEntity.getRoomName());
        room.unlock(room.getRole());

        // Save the room to the DB if the room should be persistent
        if (room.isPersistent()) {
            log("Persisting room that is being created/updated: " + mucRoomEntity.getRoomName());
            room.saveToDB();
        }

        log("Syncing room that is being created/updated: " + mucRoomEntity.getRoomName());
        MUCServiceController.getService(serviceName).syncChatRoom(room);

        if (sendInvitations && allUsersWithNewAffiliations != null) {
            log("Sending invitations for room that is being created/updated: " + mucRoomEntity.getRoomName());
            sendInvitationsFromRoom(room, null, allUsersWithNewAffiliations, null, true);
        }
        log("Done creating/updating room: " + mucRoomEntity.getRoomName());
    }

    private boolean equalToAffiliations(MUCRoom room, MUCRoomEntity mucRoomEntity) {
        if (mucRoomEntity == null || room == null) {
            return false;
        }

        ConcurrentGroupList<JID> owners = new ConcurrentGroupList<>(room.getOwners());
        ConcurrentGroupList<JID> admins = new ConcurrentGroupList<>(room.getAdmins());
        ConcurrentGroupList<JID> members = new ConcurrentGroupList<>(room.getMembers());
        ConcurrentGroupList<JID> outcasts = new ConcurrentGroupList<>(room.getOutcasts());

        Set<String> roomOwners = new HashSet<>(MUCRoomUtils.convertJIDsToStringList(owners)); // convertJIDsToStringList ignores group JIDs.
        Set<String> roomAdmins = new HashSet<>(MUCRoomUtils.convertJIDsToStringList(admins));
        Set<String> roomMembers = new HashSet<>(MUCRoomUtils.convertJIDsToStringList(members));
        Set<String> roomOutcasts = new HashSet<>(MUCRoomUtils.convertJIDsToStringList(outcasts));

        Set<String> entityOwners = mucRoomEntity.getOwners() != null ? new HashSet<>(mucRoomEntity.getOwners()) : new HashSet<>();
        Set<String> entityAdmins = mucRoomEntity.getAdmins() != null ? new HashSet<>(mucRoomEntity.getAdmins()) : new HashSet<>();
        Set<String> entityMembers = mucRoomEntity.getMembers() != null ? new HashSet<>(mucRoomEntity.getMembers()) : new HashSet<>();
        Set<String> entityOutcasts = mucRoomEntity.getOutcasts() != null ? new HashSet<>(mucRoomEntity.getOutcasts()) : new HashSet<>();

        Set<String> roomOwnerGroups = new HashSet<>(MUCRoomUtils.convertGroupsToStringList(owners.getGroups()));
        Set<String> roomAdminGroups = new HashSet<>(MUCRoomUtils.convertGroupsToStringList(admins.getGroups()));
        Set<String> roomMemberGroups = new HashSet<>(MUCRoomUtils.convertGroupsToStringList(members.getGroups()));
        Set<String> roomOutcastGroups = new HashSet<>(MUCRoomUtils.convertGroupsToStringList(outcasts.getGroups()));

        Set<String> entityOwnerGroups = mucRoomEntity.getOwnerGroups() != null ? new HashSet<>(mucRoomEntity.getOwnerGroups()) : new HashSet<>();
        Set<String> entityAdminGroups = mucRoomEntity.getAdminGroups() != null ? new HashSet<>(mucRoomEntity.getAdminGroups()) : new HashSet<>();
        Set<String> entityMemberGroups = mucRoomEntity.getMemberGroups() != null ? new HashSet<>(mucRoomEntity.getMemberGroups()) : new HashSet<>();
        Set<String> entityOutcastGroups = mucRoomEntity.getOutcastGroups() != null ? new HashSet<>(mucRoomEntity.getOutcastGroups()) : new HashSet<>();

        return entityOwners.equals(roomOwners)
            && entityAdmins.equals(roomAdmins)
            && entityMembers.equals(roomMembers)
            && entityOutcasts.equals(roomOutcasts)
            && entityOwnerGroups.equals(roomOwnerGroups)
            && entityAdminGroups.equals(roomAdminGroups)
            && entityMemberGroups.equals(roomMemberGroups)
            && entityOutcastGroups.equals(roomOutcastGroups);
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
     * Invites the user(s) or group(s) to the MUC room. This method differs from the other 'sendInvitations' methods in
     * that no checks are performed. This really just sends the invitation stanza(s).
     *
     * @param serviceName
     *            the service name
     * @param roomName
     *            the room name
     * @param mucInvitationsEntity
     *            the invitation entity containing invitation reason and jids to invite
     * @throws ServiceException
     *             the service exception
     */
    public void inviteUsersAndOrGroups(String serviceName, String roomName, MUCInvitationsEntity mucInvitationsEntity)
            throws ServiceException {
        MUCRoom room = getRoom(serviceName, roomName);

        // First determine where to send all the invitations
        Set<JID> targetJIDs = new HashSet<>();
        for (String jidString : mucInvitationsEntity.getJidsToInvite()) {
            JID jid = UserUtils.checkAndGetJID(jidString);
            // Is it a group? Then unpack and send to every single group member.
            Group g = UserUtils.getGroupIfIsGroup(jid);
            if (g != null) {
                targetJIDs.addAll(g.getAll());
            } else {
                targetJIDs.add(jid);
            }
        }

        // And now send
        for (JID jid : targetJIDs) {
            try {
                room.sendInvitation(jid, mucInvitationsEntity.getReason(), room.getRole(), null);
            } catch (ForbiddenException | CannotBeInvitedException e) {
                throw new ServiceException("Could not invite user", jid.toString(), ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
            }
        }
    }

    /**
     * Sends invitations "from the room" to a single user that is affiliated to the room.
     *
     * @see #sendInvitationsFromRoom(MUCRoom, EnumSet, Collection, String, boolean)
     *
     * @param room
     *          The room
     * @param affiliations
     *          The set of affiliations for which to send invitations, with a default of admin+owner+member when left
     *          unspecified (null)
     * @param limitToThisUserOrGroup
     *          The user or group for which to send invitations
     * @param reason
     *          The reason to include in the invitation, with a sensible default when left unspecified (null)
     * @param performAffiliationCheck
     *          Whether to validate if the user or group is actually affiliated to the room in the correct way
     * @throws ForbiddenException
     *          The forbidden exception
     */
    private void sendInvitationsToSingleJID(
        MUCRoom room,
        EnumSet<MUCRole.Affiliation> affiliations,
        JID limitToThisUserOrGroup,
        String reason,
        boolean performAffiliationCheck
    ) throws ForbiddenException {
        Set<JID> setOfOneJID = new HashSet<>();
        setOfOneJID.add(limitToThisUserOrGroup);
        sendInvitationsFromRoom(room, affiliations, setOfOneJID, reason, performAffiliationCheck);
    }

    /**
     * Sends invitations "from the room" to users that are affiliated to the room. The target audience can be limited to
     * a specific set of JIDs through the #limitToTheseUsers parameter. If this parameter is left null, invitations are
     * sent to all users with the specified affiliations.
     *
     * Before sending any invitation, this method checks whether the invitation recipient is actually affiliated to the
     * room in the way that the invitation expresses.
     *
     * @param room
     *          The room
     * @param affiliations
     *          The set of affiliations for which to send invitations, with a default of admin+owner+member when left
     *          unspecified (null)
     * @param limitToTheseUsers
     *          The collection of users for which to send invitations, with a default of "all" affiliated users when
     *          left unspecified (null)
     * @param reason
     *          The reason to include in the invitation, with a sensible default when left unspecified (null)
     * @param performAffiliationCheck
     *          Whether to validate if the user or group is actually affiliated to the room in the correct way
     * @throws ForbiddenException
     *          The forbidden exception
     */
    private void sendInvitationsFromRoom(
        MUCRoom room,
        EnumSet<MUCRole.Affiliation> affiliations,
        Collection<JID> limitToTheseUsers,
        String reason,
        boolean performAffiliationCheck
    ) throws ForbiddenException {

        if (affiliations == null) {
            affiliations = EnumSet.of(MUCRole.Affiliation.admin, MUCRole.Affiliation.member, MUCRole.Affiliation.owner);
        }
        MUCRole roomRole = MUCRole.createRoomRole(room);

        if (affiliations.contains(MUCRole.Affiliation.admin)) {
            Collection<JID> sendHere = limitToTheseUsers == null ? room.getAdmins() : limitToTheseUsers;
            for (JID roomAdmin : sendHere) {
                sendSingleInvitationFromRoom(
                    roomAdmin,
                    room,
                    roomRole,
                    MUCRole.Affiliation.admin,
                    reason == null ? "You are admin of room " + room.getName() : reason,
                    performAffiliationCheck ? (r, j) -> r.getAdmins().contains(j) : null
                );
            }
        }
        if (affiliations.contains(MUCRole.Affiliation.owner)) {
            Collection<JID> sendHere = limitToTheseUsers == null ? room.getOwners() : limitToTheseUsers;
            for (JID roomOwner : sendHere) {
                sendSingleInvitationFromRoom(
                    roomOwner,
                    room,
                    roomRole,
                    MUCRole.Affiliation.owner,
                    reason == null ? "You are owner of room " + room.getName() : reason,
                    performAffiliationCheck ? (r, j) -> r.getOwners().contains(j) : null
                );
            }
        }
        if (affiliations.contains(MUCRole.Affiliation.member)) {
            Collection<JID> sendHere = limitToTheseUsers == null ? room.getMembers() : limitToTheseUsers;
            for (JID roomMember : sendHere) {
                sendSingleInvitationFromRoom(
                    roomMember,
                    room,
                    roomRole,
                    MUCRole.Affiliation.member,
                    reason == null ? "You are member of room " + room.getName() : reason,
                    performAffiliationCheck ? (r, j) -> r.getMembers().contains(j) : null
                );
            }
        }
    }

    /**
     * Sends an invitation for a specific affiliation to a single JID, (optionally) performing a check if that JID is
     * actually affiliated to the room that way.
     *
     * @param sendHere
     *          The JID to send the invitation to
     * @param room
     *          The room
     * @param roomRole
     *          Role of the room (added for optimisation, to prevent the MUCRole.createRoomRole(room) from being called
     *          many times)
     * @param affiliation
     *          The affiliation for which the jid is invited
     * @param invitationReason
     *          The reason to include in the invitation message
     * @param validation
     *          Function to apply to the room and the jid to check whether the jid is actually affiliated in the correct
     *          way (or null if no validation is required)
     * @throws ForbiddenException
     *          The forbidden exception
     */
    private void sendSingleInvitationFromRoom(
        JID sendHere,
        MUCRoom room,
        MUCRole roomRole,
        MUCRole.Affiliation affiliation,
        String invitationReason,
        BiFunction<MUCRoom, JID, Boolean> validation
    ) throws ForbiddenException {
        boolean jidIsGroup = false;

        if (validation != null && !validation.apply(room, sendHere)) {
            log("User or group " + sendHere + " can not be invited to be " + affiliation + " of room " + room.getName() + " because it is not affiliated that way");
        } else {
            // First handle group behavior
            Group g = UserUtils.getGroupIfIsGroup(sendHere);
            if (g != null) {
                jidIsGroup = true;
                // This is a group jid, so we need to send the invitation to every single group member
                for (JID singleGroupMemberJID : g.getAll()) {
                    // Skip affiliation check, because it has already been done for the group, and the single user may not
                    // actually be known to be affiliated on its own merits
                    sendSingleInvitationFromRoom(singleGroupMemberJID, room, roomRole, affiliation, invitationReason, null);
                }
            }

            if (!jidIsGroup) {
                try {
                    room.sendInvitation(sendHere, invitationReason, roomRole, null);
                } catch (CannotBeInvitedException e) {
                    log("User " + sendHere + " can not be invited to be " + affiliation + " of room " + room.getName());
                }
            }
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
     * @return
     *             all users for which a role was added
     * @throws ForbiddenException
     *             the forbidden exception
     * @throws NotAllowedException
     *             the not allowed exception
     * @throws ConflictException
     *             the conflict exception
     */
    private static Collection<JID> setRoles(MUCRoom room, MUCRoomEntity mucRoomEntity) throws ForbiddenException, NotAllowedException, ConflictException
    {
        final Collection<JID> allNewAffiliations = new ArrayList<>();

        // Calculate which affiliations are to be removed from the room. These are all the old affiliations, that are now no longer affiliations.
        final List<JID> affiliationsToReset = new ArrayList<>();
        affiliationsToReset.addAll(room.getOwners());
        affiliationsToReset.addAll(room.getAdmins());
        affiliationsToReset.addAll(room.getMembers());
        affiliationsToReset.addAll(room.getOutcasts());

        // Calculate which are the new owners.
        final Collection<JID> newOwners = new ArrayList<>();
        if (mucRoomEntity.getOwners() != null) {
            newOwners.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOwners()));
        }
        if (mucRoomEntity.getOwnerGroups() != null) {
            for (final String groupName : mucRoomEntity.getOwnerGroups()) {
                newOwners.add(UserUtils.checkAndGetJID(groupName));
            }
        }
        affiliationsToReset.removeAll(newOwners); // Do not remove these from the room after we're done re-affiliating everyone!
        newOwners.removeAll(room.getOwners()); // Removing the ones that are already associated. We don't need to add these again.

        // Update the room by adding new owners.
        for (final JID newOwner : newOwners) {
            log("Adding new 'owner' affiliation for '" + newOwner + "' to room: " + room.getName());
            room.addOwner(newOwner, room.getRole());
            allNewAffiliations.add(newOwner);
        }

        // Calculate which are the new admins.
        final Collection<JID> newAdmins = new ArrayList<>();
        if (mucRoomEntity.getAdmins() != null) {
            newAdmins.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getAdmins()));
        }
        if (mucRoomEntity.getAdminGroups() != null) {
            for (final String groupName : mucRoomEntity.getAdminGroups()) {
                newAdmins.add(UserUtils.checkAndGetJID(groupName));
            }
        }
        affiliationsToReset.removeAll(newAdmins); // Do not remove these from the room after we're done re-affiliating everyone!
        newAdmins.removeAll(room.getAdmins()); // Removing the ones that are already associated. We don't need to add these again.

        // Update the room by adding new admins.
        for (final JID newAdmin : newAdmins) {
            log("Adding new 'admin' affiliation for '" + newAdmin + "' to room: " + room.getName());
            room.addAdmin(newAdmin, room.getRole());
            allNewAffiliations.add(newAdmin);
        }

        // Calculate which are the new members.
        final Collection<JID> newMembers = new ArrayList<>();
        if (mucRoomEntity.getMembers() != null) {
            newMembers.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getMembers()));
        }
        if (mucRoomEntity.getMemberGroups() != null) {
            for (final String groupName : mucRoomEntity.getMemberGroups()) {
                newMembers.add(UserUtils.checkAndGetJID(groupName));
            }
        }
        affiliationsToReset.removeAll(newMembers); // Do not remove these from the room after we're done re-affiliating everyone!
        newMembers.removeAll(room.getMembers()); // Removing the ones that are already associated. We don't need to add these again.

        // Update the room by adding new members.
        for (final JID newMember : newMembers) {
            log("Adding new 'member' affiliation for '" + newMember + "' to room: " + room.getName());
            room.addMember(newMember, null, room.getRole());
            allNewAffiliations.add(newMember);
        }

        // Calculate which are the new outcasts.
        final Collection<JID> newOutcasts = new ArrayList<>();
        if (mucRoomEntity.getOutcasts() != null) {
            newOutcasts.addAll(MUCRoomUtils.convertStringsToJIDs(mucRoomEntity.getOutcasts()));
        }
        if (mucRoomEntity.getOutcastGroups() != null) {
            for (final String groupName : mucRoomEntity.getOutcastGroups()) {
                newOutcasts.add(UserUtils.checkAndGetJID(groupName));
            }
        }
        affiliationsToReset.removeAll(newOutcasts); // Do not remove these from the room after we're done re-affiliating everyone!
        newOutcasts.removeAll(room.getOutcasts()); // Removing the ones that are already associated. We don't need to add these again.

        // Update the room by adding new outcasts.
        for (final JID newOutcast : newOutcasts) {
            log("Adding new 'outcast' affiliation for '" + newOutcast + "' to room: " + room.getName());
            room.addOutcast(newOutcast, null, room.getRole());
            allNewAffiliations.add(newOutcast);
        }

        // Finally, clean up every old affiliation that is not carrying over.
        for (JID affiliationToReset : affiliationsToReset) {
            log("Removing old affiliation for '" + affiliationToReset + "' from room: " + room.getName());
            room.addNone(affiliationToReset, room.getRole());
        }

        return allNewAffiliations;
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
     * @param sendInvitations
     *            whether to send invitations to newly affiliated users
     * @throws ServiceException On any issue looking up the room or changing its affiliated users.
     */
    public void replaceAffiliatedUsers(@Nonnull final String serviceName, @Nonnull final String roomName, @Nonnull final MUCRole.Affiliation affiliation, boolean sendInvitations, @Nonnull final String... jids) throws ServiceException
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

        try {
            if (sendInvitations) {
                sendInvitationsFromRoom(room, EnumSet.of(affiliation), toAdd, null, true);
            }
        } catch (ForbiddenException e) {
            throw new ServiceException("Can not send invitation to newly affiliated " + affiliation + " users or groups", roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
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
     * @param sendInvitations
     *            whether to send invitations to newly affiliated users
     * @throws ServiceException On any issue looking up the room or changing its affiliated users.
     */
    public void addAffiliatedUsers(@Nonnull final String serviceName, @Nonnull final String roomName, @Nonnull final MUCRole.Affiliation affiliation, boolean sendInvitations, @Nonnull final String... jids) throws ServiceException
    {
        final Collection<JID> additions = new HashSet<>();

        // Input validation.
        for (String replacement : jids) {
            try {
                additions.add(UserUtils.checkAndGetJID(replacement));
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

        try {
            if (sendInvitations) {
                sendInvitationsFromRoom(room, EnumSet.of(affiliation), toAdd, null, true);
            }
        } catch (ForbiddenException e) {
            throw new ServiceException("Can not send invitation to newly affiliated " + affiliation + " users or groups", roomName, ExceptionType.NOT_ALLOWED, Response.Status.FORBIDDEN, e);
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
     * @param affiliation
     *            the type of affiliation to remove. Null to remove any affiliation.
     * @throws ServiceException
     *             the service exception
     */
    public void deleteAffiliation(String serviceName, String roomName, MUCRole.Affiliation affiliation, String jid) throws ServiceException {
        MUCRoom room = getRoom(serviceName, roomName);
        try {
              JID userJid = UserUtils.checkAndGetJID(jid);

              if (affiliation != null && room.getAffiliation(userJid) != affiliation) {
                  throw new ConflictException("Entity does not have this affiliation with the room.");
              }
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
