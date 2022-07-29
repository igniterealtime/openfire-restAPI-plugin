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

package org.jivesoftware.openfire.plugin.rest.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.plugin.rest.entity.UserEntity;
import org.jivesoftware.openfire.plugin.rest.entity.UserProperty;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupJID;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.xmpp.packet.JID;

/**
 * The Class UserUtils.
 */
public class UserUtils {

    /**
     * Instantiates a new user utils.
     */
    private UserUtils() {
        throw new AssertionError();
    }

    /**
     * Convert users to user entities.
     *
     * @param users            the users
     * @param userSearch the user search
     * @return the list
     */
    public static List<UserEntity> convertUsersToUserEntities(Collection<User> users, String userSearch) {
        List<UserEntity> result = new ArrayList<>();

        for (User user : users) {
            if (userSearch != null) {
                if (!user.getUsername().contains(userSearch)) {
                    continue;
                }
            }

            result.add(convertUserToUserEntity(user));
        }
        return result;
    }

    /**
     * Convert user to user entity.
     *
     * @param user
     *            the user
     * @return the user entity
     */
    public static UserEntity convertUserToUserEntity(User user) {
        UserEntity userEntity = new UserEntity(user.getUsername(), user.getName(), user.getEmail());

        List<UserProperty> userProperties = new ArrayList<>();
        for (Entry<String, String> property : user.getProperties().entrySet()) {
            userProperties.add(new UserProperty(property.getKey(), property.getValue()));
        }
        userEntity.setProperties(userProperties);

        return userEntity;
    }

    /**
     * Checks if is valid sub type.
     *
     * @param subType            the sub type
     * @throws UserAlreadyExistsException the user already exists exception
     */
    public static void checkSubType(int subType) throws UserAlreadyExistsException {
        if (!(subType >= -1 && subType <= 3)) {
            throw new UserAlreadyExistsException();
        }
    }
    
    
    /**
     * Check and get jid.
     *
     * @param jid the jid
     * @return the jid
     */
    public static JID checkAndGetJID(String jid) {
        if(isValidBareJid(jid)) {
            return new JID(jid);
        } else if (isValidGroupName(jid)) {
            GroupJID gjid = new GroupJID(jid);
            return gjid.asBareJID();
        } else {
            return XMPPServer.getInstance().createJID(jid, null);
        }
    }
    
    /**
     * Checks if is valid bare jid.
     *
     * @param jid the jid
     * @return true, if is valid bare jid
     */
    public static boolean isValidBareJid(String jid) {
        final int index = jid.indexOf('@');
        if (index == -1) {
            return false;
        } else return jid.indexOf('@', index + 1) == -1;
    }
    /**
     * Checks if this group exists.
     *
     * @param groupname The groupname as a string
     * @return true, if the groupname exists
     */
    public static boolean isValidGroupName(String groupname) {
        try {
            GroupManager.getInstance().getGroup(groupname);
        } catch(GroupNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the JID represents a group, and returns the group if it does.
     * @param maybeAGroupJIDOrAGroupName
     *          A JID that may or may not be a group JID or even just a group name
     * @return
     *          The group represented by the JID, or null if the JID does not represent a group
     */
    public static Group getGroupIfIsGroup(JID maybeAGroupJIDOrAGroupName) {
        try {
            return GroupManager.getInstance().getGroup(maybeAGroupJIDOrAGroupName);
        } catch (GroupNotFoundException e) {
            // Just return null
        }

        return null;
    }
}
