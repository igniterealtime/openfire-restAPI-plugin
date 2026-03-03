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

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.SharedGroupException;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.lockout.LockOutManager;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.dao.PropertyDAO;
import org.jivesoftware.openfire.plugin.rest.entity.*;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.utils.UserUtils;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.roster.RosterManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.StreamError;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Class UserServiceController.
 */
public class UserServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceController.class);

    /** The Constant INSTANCE. */
    private static UserServiceController INSTANCE = null;

    /** The user manager. */
    private final UserManager userManager;

    /** The roster manager. */
    private final RosterManager rosterManager;

    /** The server. */
    private final XMPPServer server;
    
    /** The lock out manager. */
    private final LockOutManager lockOutManager;

    private final VCardManager vcardManager;

    /**
     * Gets the single instance of UserServiceController.
     *
     * @return single instance of UserServiceController
     */
    public static UserServiceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserServiceController();
        }
        return INSTANCE;
    }

    /**
     * @param instance the mock/stub/spy controller to use.
     * @deprecated - for test use only
     */
    @Deprecated
    public static void setInstance(final UserServiceController instance) {
        UserServiceController.INSTANCE = instance;
    }

    /**
     * Instantiates a new user service controller.
     */
    private UserServiceController() {
        server = XMPPServer.getInstance();
        userManager = server.getUserManager();
        rosterManager = server.getRosterManager();
        lockOutManager = server.getLockOutManager();
        vcardManager = server.getVCardManager();
    }

    public static void log(String logMessage) {
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            LOG.info(logMessage);
        }
    }

    /**
     * Creates the user.
     *
     * @param userEntity
     *            the user entity
     * @throws ServiceException
     *             the service exception
     */
    public void createUser(UserEntity userEntity) throws ServiceException {
        if (userEntity != null && !userEntity.getUsername().isEmpty()) {
            if (userEntity.getPassword() == null) {
                throw new ServiceException("Could not create new user, because password is null",
                        userEntity.getUsername(), "PasswordIsNull", Response.Status.BAD_REQUEST);
            }
            log("Create a new user: " + userEntity.getUsername());
            try {
                userManager.createUser(userEntity.getUsername(), userEntity.getPassword(), userEntity.getName(),
                        userEntity.getEmail());
            } catch (UserAlreadyExistsException e) {
                throw new ServiceException("Could not create new user", userEntity.getUsername(),
                        ExceptionType.USER_ALREADY_EXISTS_EXCEPTION, Response.Status.CONFLICT);
            }
            addProperties(userEntity.getUsername(), userEntity.getProperties());
        } else {
            throw new ServiceException("Could not create new user",
                    "users", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Update user.
     *
     * @param username
     *            the username
     * @param userEntity
     *            the user entity
     * @throws ServiceException
     *             the service exception
     */
    public void updateUser(String username, UserEntity userEntity) throws ServiceException {
        if (userEntity != null && !username.isEmpty()) {
            log("Update the user: " + userEntity.getUsername());
            // Payload contains another username than provided over path
            // parameter
            if (userEntity.getUsername() != null) {
                if (!userEntity.getUsername().equals(username)) {
                    JustMarriedController.changeName(username, userEntity.getUsername(), true, userEntity.getEmail(),
                            userEntity.getName());
                    addProperties(userEntity.getUsername(), userEntity.getProperties());
                    return;
                }
            }
            User user = getAndCheckUser(username);
            if (userEntity.getPassword() != null) {
                user.setPassword(userEntity.getPassword());
            }
            if (userEntity.getName() != null) {
                user.setName(userEntity.getName());
            }
            if (userEntity.getEmail() != null) {
                user.setEmail(userEntity.getEmail());
            }

            addProperties(username, userEntity.getProperties());
        }
    }

    /**
     * Delete user.
     *
     * @param username
     *            the username
     * @throws ServiceException
     *             the service exception
     */
    public void deleteUser(String username) throws ServiceException {
        log("Delete the user: " + username);
        User user = getAndCheckUser(username);
        userManager.deleteUser(user);

        rosterManager.deleteRoster(server.createJID(username, null));
    }

    /**
     * Gets the user entities.
     *
     * When a property key (and possibly value) is provided, then the user that is returned is one for which the
     * specified property has been defined.
     *
     * @param userSearch
     *            the user search
     * @param propertyKey
     *            the property key (can be null)
     * @param propertyValue
     *            the property value (can be null)
     * @return the user entities
     * @throws ServiceException
     *              the service exception
     */
    public UserEntities getUserEntities(String userSearch, String propertyKey, String propertyValue)
            throws ServiceException {
        if (propertyKey != null) {
            log("Get users by property");
            return getUserEntitiesByProperty(propertyKey, propertyValue);
        }
        log("Get all users");
        UserEntities userEntities = new UserEntities();
        userEntities.setUsers(UserUtils.convertUsersToUserEntities(userManager.getUsers(), userSearch));
        return userEntities;
    }

    /**
     * Gets the user entity.
     *
     * @param username
     *            the username
     * @return the user entity
     * @throws ServiceException
     *             the service exception
     */
    public UserEntity getUserEntity(String username) throws ServiceException {
        log("Get user entity from user: " + username);
        return UserUtils.convertUserToUserEntity(getAndCheckUser(username));
    }

    /**
     * Enable user.
     *
     * @param username
     *            the username
     * @throws ServiceException
     *             the service exception
     */
    public void enableUser(String username) throws ServiceException {
        log("Enable the user: " + username);
        getAndCheckUser(username);
        lockOutManager.enableAccount(username);
    }

    /**
     * Disable user.
     *
     * @param username
     *            the username
     * @throws ServiceException
     *             the service exception
     */
    public void disableUser(String username) throws ServiceException {
        log("Disable the user: " + username);
        getAndCheckUser(username);
        lockOutManager.disableAccount(username, null, null);
        
        if (lockOutManager.isAccountDisabled(username)) {
            final StreamError error = new StreamError(StreamError.Condition.not_authorized);
            for (ClientSession session : SessionManager.getInstance().getSessions(username)) {
                session.deliverRawText(error.toXML());
                session.close();
            }
        }
    }

    /**
     * Gets the roster entities.
     *
     * @param username
     *            the username
     * @return the roster entities
     * @throws ServiceException
     *             the service exception
     */
    public RosterEntities getRosterEntities(String username) throws ServiceException {
        log("Get roster entities for user: " + username);
        Roster roster = getUserRoster(username);

        List<RosterItemEntity> rosterEntities = new ArrayList<>();
        for (RosterItem rosterItem : roster.getRosterItems()) {
            RosterItemEntity rosterItemEntity = new RosterItemEntity(rosterItem.getJid().toBareJID(),
                    rosterItem.getNickname(), rosterItem.getSubStatus().getValue());
            rosterItemEntity.setGroups(rosterItem.getGroups());

            rosterEntities.add(rosterItemEntity);
        }

        return new RosterEntities(rosterEntities);
    }

    /**
     * Adds the roster item.
     *
     * @param username
     *            the username
     * @param rosterItemEntity
     *            the roster item entity
     * @throws ServiceException
     *             the service exception
     * @throws UserAlreadyExistsException
     *             the user already exists exception
     * @throws SharedGroupException
     *             the shared group exception
     * @throws UserNotFoundException
     *             the user not found exception
     */
    public void addRosterItem(String username, RosterItemEntity rosterItemEntity) throws ServiceException,
            UserAlreadyExistsException, SharedGroupException, UserNotFoundException {
        Roster roster = getUserRoster(username);
        if (rosterItemEntity.getJid() == null) {
            throw new ServiceException("JID is null", "JID", "IllegalArgumentException", Response.Status.BAD_REQUEST);
        }
        JID jid = new JID(rosterItemEntity.getJid());
        log("Adding a roster item to: " + rosterItemEntity.getJid());
        try {
            roster.getRosterItem(jid);
            throw new UserAlreadyExistsException(jid.toBareJID());
        } catch (UserNotFoundException e) {
            // Roster item does not exist. Try to add it.
        }

        RosterItem rosterItem = roster.createRosterItem(jid, rosterItemEntity.getNickname(),
                rosterItemEntity.getGroups(), false, true);
        UserUtils.checkSubType(rosterItemEntity.getSubscriptionType());
        rosterItem.setSubStatus(RosterItem.SubType.getTypeFromInt(rosterItemEntity.getSubscriptionType()));
        roster.updateRosterItem(rosterItem);
    }

    /**
     * Update roster item.
     *
     * @param username
     *            the username
     * @param rosterJid
     *            the roster jid
     * @param rosterItemEntity
     *            the roster item entity
     * @throws ServiceException
     *             the service exception
     * @throws UserNotFoundException
     *             the user not found exception
     * @throws UserAlreadyExistsException
     *             the user already exists exception
     * @throws SharedGroupException
     *             the shared group exception
     */
    public void updateRosterItem(String username, String rosterJid, RosterItemEntity rosterItemEntity)
            throws ServiceException, UserNotFoundException, UserAlreadyExistsException, SharedGroupException {
        log("Updating a roster item for user: " + username + ", with roster JID: " + rosterJid);
        getAndCheckUser(username);

        Roster roster = getUserRoster(username);
        JID jid = new JID(rosterJid);
        RosterItem rosterItem = roster.getRosterItem(jid);

        if (rosterItemEntity.getNickname() != null) {
            rosterItem.setNickname(rosterItemEntity.getNickname());
        }
        if (rosterItemEntity.getGroups() != null) {
            rosterItem.setGroups(rosterItemEntity.getGroups());
        }
        UserUtils.checkSubType(rosterItemEntity.getSubscriptionType());

        rosterItem.setSubStatus(RosterItem.SubType.getTypeFromInt(rosterItemEntity.getSubscriptionType()));
        roster.updateRosterItem(rosterItem);
    }

    /**
     * Delete roster item.
     *
     * @param username
     *            the username
     * @param rosterJid
     *            the roster jid
     * @throws SharedGroupException
     *             the shared group exception
     * @throws ServiceException
     *             the service exception
     */
    public void deleteRosterItem(String username, String rosterJid) throws SharedGroupException, ServiceException {
        log("Deleting a roster item for user: " + username + ", with roster JID: " + rosterJid);
        getAndCheckUser(username);
        Roster roster = getUserRoster(username);
        JID jid = new JID(rosterJid);

        if (roster.deleteRosterItem(jid, true) == null) {
            throw new ServiceException("Roster Item could not deleted", jid.toBareJID(), "RosterItemNotFound",
                    Response.Status.NOT_FOUND);
        }
    }

    /**
     * Gets the user groups.
     *
     * @param username
     *            the username
     * @return the user groups
     * @throws ServiceException
     *             the service exception
     */
    public List<String> getUserGroups(String username) throws ServiceException {
        log("Get user groups for user: " + username);
        if (username.contains("@")) {
            final JID jid = new JID(username);
            if (jid.getDomain().equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
                username = jid.getNode();
            } else {
                // Implementing this would require us to iterate over all groups, which is a performance nightmare.
                throw new ServiceException("This service cannot be used for non-local users.", username, ExceptionType.GROUP_NOT_FOUND, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
        User user = getAndCheckUser(username);
        Collection<Group> groups = GroupManager.getInstance().getGroups(user);
        List<String> groupNames = new ArrayList<>();
        for (Group group : groups) {
            groupNames.add(group.getName());
        }

        return groupNames;
    }

    /**
     * Adds the user to group.
     *
     * @param username
     *            the username
     * @param userGroupsEntity
     *            the user groups entity
     * @throws ServiceException
     *             the service exception
     */
    public void addUserToGroups(String username, UserGroupsEntity userGroupsEntity) throws ServiceException {
        if (userGroupsEntity != null) {
            log("Adding user: " + username + " to groups");
            Collection<Group> groups = new ArrayList<>();

            for (String groupName : userGroupsEntity.getGroupNames()) {
                Group group;
                try {
                    group = GroupManager.getInstance().getGroup(groupName);
                    log("Adding user: " + username + " to a group: " + groupName);
                } catch (GroupNotFoundException e) {
                    // Create this group
                    group = GroupController.getInstance().createGroup(new GroupEntity(groupName, ""));
                }
                groups.add(group);
            }
            for (Group group : groups) {
                group.getMembers().add(username.contains("@") ? new JID(username) : XMPPServer.getInstance().createJID(username, null));
            }
        }
    }
    
    /**
     * Adds the user to group.
     *
     * @param username the username
     * @param groupName the group name
     * @throws ServiceException the service exception
     */
    public void addUserToGroup(String username, String groupName) throws ServiceException {
        log("Adding user: " + username + " to a group: " + groupName);
        Group group;
        try {
            group = GroupManager.getInstance().getGroup(groupName);
        } catch (GroupNotFoundException e) {
            // Create this group
            log("Group: " + groupName + " does not exist. Creating the group");
            group = GroupController.getInstance().createGroup(new GroupEntity(groupName, ""));
        }
        
        group.getMembers().add(username.contains("@") ? new JID(username) : XMPPServer.getInstance().createJID(username, null));
    }

    /**
     * Delete user from groups.
     *
     * @param username
     *            the username
     * @param userGroupsEntity
     *            the user groups entity
     * @throws ServiceException
     *             the service exception
     */
    public void deleteUserFromGroups(String username, UserGroupsEntity userGroupsEntity) throws ServiceException {
        if (userGroupsEntity != null) {
            log("Removing user: " + username + " from groups");
            for (String groupName : userGroupsEntity.getGroupNames()) {
                log("deleteUserFromGroups, "+username+ ", groupName: "+groupName);
                Group group;
                try {
                    group = GroupManager.getInstance().getGroup(groupName);
                } catch (GroupNotFoundException e) {
                    throw new ServiceException("Could not find group", groupName, ExceptionType.GROUP_NOT_FOUND,
                            Response.Status.NOT_FOUND, e);
                }
                log("Removing user: " + username + " from the group: " + groupName);
                group.getMembers().remove(username.contains("@") ? new JID(username) : XMPPServer.getInstance().createJID(username, null));
            }
        }
    }
    
    /**
     * Delete user from group.
     *
     * @param username the username
     * @param groupName the group name
     * @throws ServiceException the service exception
     */
    public void deleteUserFromGroup(String username, String groupName) throws ServiceException {
        log("Removing user: " + username + " from the group: " + groupName);
        Group group;
        try {
            group = GroupManager.getInstance().getGroup(groupName);
        } catch (GroupNotFoundException e) {
            throw new ServiceException("Could not find group", groupName, ExceptionType.GROUP_NOT_FOUND,
                    Response.Status.NOT_FOUND, e);
        }
        group.getMembers().remove(username.contains("@") ? new JID(username) : XMPPServer.getInstance().createJID(username, null));
    }

    /**
     * Gets the user entities by property key and or value.
     *
     * @param propertyKey
     *            the property key
     * @param propertyValue
     *            the property value (can be null)
     * @return the user entities by property
     * @throws ServiceException
     *             the service exception
     */
    public UserEntities getUserEntitiesByProperty(String propertyKey, String propertyValue) throws ServiceException {
        log("Get user entities by property key : " + propertyKey + "and property value: " + propertyValue);
        List<String> usernames = PropertyDAO.getUsernameByProperty(propertyKey, propertyValue);
        List<UserEntity> users = new ArrayList<>();
        UserEntities userEntities = new UserEntities();

        for (String username : usernames) {
            users.add(getUserEntity(username));
        }

        userEntities.setUsers(users);
        return userEntities;
    }

    /**
     * Adds the properties.
     *
     * @param username
     *            the username
     * @param properties
     *            user properties
     * @throws ServiceException
     *             the service exception
     */
    private void addProperties(String username, List<UserProperty> properties) throws ServiceException {
        log("Adding a property to user: " + username);
        User user = getAndCheckUser(username);
        user.getProperties().clear();
        if (properties != null) {
            for (UserProperty property : properties) {
                user.getProperties().put(property.getKey(), property.getValue());
            }
        }
    }

    /**
     * Gets the and check user.
     *
     * @param username
     *            the username
     * @return the and check user
     * @throws ServiceException
     *             the service exception
     */
    private User getAndCheckUser(String username) throws ServiceException {
        JID targetJID = server.createJID(username, null);
        if (targetJID.getNode() == null) {
            throw new ServiceException("Could not get user", username, ExceptionType.USER_NOT_FOUND_EXCEPTION,
                    Response.Status.NOT_FOUND);
        }

        try {
            return userManager.getUser(targetJID.getNode());
        } catch (UserNotFoundException e) {
            throw new ServiceException("Could not get user", username, ExceptionType.USER_NOT_FOUND_EXCEPTION,
                    Response.Status.NOT_FOUND, e);
        }
    }

    /**
     * Gets the user roster.
     *
     * @param username
     *            the username
     * @return the user roster
     * @throws ServiceException
     *             the service exception
     */
    private Roster getUserRoster(String username) throws ServiceException {
        log("getUserRoster, "+username);
        try {
            return rosterManager.getRoster(username);
        } catch (UserNotFoundException e) {
            throw new ServiceException("Could not get user roster", username, ExceptionType.USER_NOT_FOUND_EXCEPTION,
                    Response.Status.NOT_FOUND, e);
        }
    }

    /**
     * Retrieves a vCard for a user.
     *
     * @param username The username for which to return a vcard
     * @return A vCard (or null)
     */
    public Element getUserVCard(String username) throws ServiceException
    {
        log("Get user vCard for user: " + username);
        if (username.contains("@")) {
            final JID jid = new JID(username);
            if (jid.getDomain().equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
                username = jid.getNode();
            } else {
                // Implementing this would require us to iterate over all groups, which is a performance nightmare.
                throw new ServiceException("This service cannot be used for non-local users.", username, ExceptionType.USER_NOT_FOUND_EXCEPTION, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        return vcardManager.getVCard(username);
    }

    /**
     * Adds or updates a vCard for a user.
     *
     * @param username The username for which to return a vcard
     * @param data The raw XML vCard data
     */
    public void setUserVCard(String username, String data) throws ServiceException
    {
        log("Set user vCard for user: " + username);
        if (username.contains("@")) {
            final JID jid = new JID(username);
            if (jid.getDomain().equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
                username = jid.getNode();
            } else {
                // Implementing this would require us to iterate over all groups, which is a performance nightmare.
                throw new ServiceException("This service cannot be used for non-local users.", username, ExceptionType.USER_NOT_FOUND_EXCEPTION, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            final Document document = DocumentHelper.parseText(data);
            vcardManager.setVCard(username, document.getRootElement());
        } catch (DocumentException e) {
            throw new ServiceException("Could not parse the provided data as a vCard", username, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        } catch (UnsupportedOperationException e) {
            throw new ServiceException("Cannot update vCards in the system, as the vCard system is configured to be read-only.", username, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.CONFLICT);
        } catch (Exception e) {
            throw new ServiceException("Unexpected problem while trying to update vCard.", username, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Removes a vCard for a user.
     *
     * @param username The username for which to return a vcard
     */
    public void deleteUserVCard(String username) throws ServiceException
    {
        log("Get user vCard for user: " + username);
        if (username.contains("@")) {
            final JID jid = new JID(username);
            if (jid.getDomain().equals(XMPPServer.getInstance().getServerInfo().getXMPPDomain())) {
                username = jid.getNode();
            } else {
                // Implementing this would require us to iterate over all groups, which is a performance nightmare.
                throw new ServiceException("This service cannot be used for non-local users.", username, ExceptionType.USER_NOT_FOUND_EXCEPTION, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            vcardManager.deleteVCard(username);
        } catch (UnsupportedOperationException e) {
            throw new ServiceException("Cannot update vCards in the system, as the vCard system is configured to be read-only.", username, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.CONFLICT);
        }
    }
}
