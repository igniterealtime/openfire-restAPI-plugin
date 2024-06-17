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

import java.util.*;

import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupAlreadyExistsException;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNameInvalidException;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.plugin.rest.entity.GroupEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.plugin.rest.utils.MUCRoomUtils;
import org.xmpp.packet.JID;

/**
 * The Class GroupController.
 */
public class GroupController {
    /** The Constant INSTANCE. */
    public static final GroupController INSTANCE = new GroupController();

    /**
     * Gets the single instance of GroupController.
     *
     * @return single instance of GroupController
     */
    public static GroupController getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     * @throws ServiceException
     *             the service exception
     */
    public List<GroupEntity> getGroups() throws ServiceException {
        Collection<Group> groups = GroupManager.getInstance().getGroups();
        List<GroupEntity> groupEntities = new ArrayList<>();
        for (Group group : groups) {
            GroupEntity groupEntity = new GroupEntity(group.getName(), group.getDescription());
            groupEntities.add(groupEntity);
        }

        return groupEntities;
    }

    /**
     * Gets the group.
     *
     * @param groupName
     *            the group name
     * @return the group
     * @throws ServiceException
     *             the service exception
     */
    public GroupEntity getGroup(String groupName) throws ServiceException {
        Group group;
        try {
            group = GroupManager.getInstance().getGroup(groupName);
        } catch (GroupNotFoundException e) {
            throw new ServiceException("Could not find group", groupName, ExceptionType.GROUP_NOT_FOUND,
                    Response.Status.NOT_FOUND, e);
        }

        GroupEntity groupEntity = new GroupEntity(group.getName(), group.getDescription());
        groupEntity.setAdmins(MUCRoomUtils.convertJIDsToStringList(group.getAdmins()));
        groupEntity.setMembers(MUCRoomUtils.convertJIDsToStringList(group.getMembers()));
        groupEntity.setShared("onlyGroup".equals(group.getProperties().get("sharedRoster.showInRoster")));

        return groupEntity;
    }

    /**
     * Creates the group.
     *
     * @param groupEntity
     *            the group entity
     * @return the group
     * @throws ServiceException
     *             the service exception
     */
    public Group createGroup(GroupEntity groupEntity) throws ServiceException {
        Group group;
        if (groupEntity != null && !groupEntity.getName().isEmpty()) {
            try {
                final Collection<JID> newMembers = new HashSet<>();
                final Collection<JID> newAdmins = new HashSet<>();

                // Input validation.
                for (final String newMember : groupEntity.getMembers()) {
                    try {
                        newMembers.add(newMember.contains("@") ? new JID(newMember) : XMPPServer.getInstance().createJID(newMember, null));
                    } catch (IllegalArgumentException e) {
                        throw new ServiceException("Cannot parse a member value as a JID: " + newMember, groupEntity.getName(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST, e);
                    }
                }
                for (final String newAdmin : groupEntity.getAdmins()) {
                    try {
                        newAdmins.add(newAdmin.contains("@") ? new JID(newAdmin) : XMPPServer.getInstance().createJID(newAdmin, null));
                    } catch (IllegalArgumentException e) {
                        throw new ServiceException("Cannot parse a admin value as a JID: " + newAdmin, groupEntity.getName(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST, e);
                    }
                }

                // Start creating the group.
                group = GroupManager.getInstance().createGroup(groupEntity.getName());
                group.setDescription(groupEntity.getDescription());

                final String showInRoster = groupEntity.getShared() ? "onlyGroup" : "nobody";
                group.getProperties().put("sharedRoster.showInRoster", showInRoster);

                group.getProperties().put("sharedRoster.displayName", groupEntity.getName());
                group.getProperties().put("sharedRoster.groupList", "");

                final Collection<JID> members = group.getMembers();
                for (final JID newMember : newMembers) {
                    // Unsure if #addAll works for the specific Collection subclass that is used in the Group implementation. Let's add them one-by-one to be safe.
                    members.add(newMember);
                }
                final Collection<JID> admins = group.getAdmins();
                for (final JID newAdmin : newAdmins) {
                    // Unsure if #addAll works for the specific Collection subclass that is used in the Group implementation. Let's add them one-by-one to be safe.
                    admins.add(newAdmin);
                }
            } catch (GroupAlreadyExistsException | GroupNameInvalidException e) {
                throw new ServiceException("Could not create a group", groupEntity.getName(),
                        ExceptionType.GROUP_ALREADY_EXISTS, Response.Status.CONFLICT, e);
            }
        } else {
            throw new ServiceException("Could not create new group", "groups",
                    ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        return group;
    }

    /**
     * Update group.
     *
     * @param groupName the group name
     * @param groupEntity the group entity
     * @return the group
     * @throws ServiceException the service exception
     */
    public Group updateGroup(String groupName, GroupEntity groupEntity) throws ServiceException {
        Group group;
        if (groupEntity != null && !groupEntity.getName().isEmpty()) {
            if (groupName.equals(groupEntity.getName())) {
                try {
                    final Collection<JID> newMembers = new HashSet<>();
                    final Collection<JID> newAdmins = new HashSet<>();

                    // Input validation.
                    group = GroupManager.getInstance().getGroup(groupName);
                    for (final String newMember : groupEntity.getMembers()) {
                        try {
                            newMembers.add(newMember.contains("@") ? new JID(newMember) : XMPPServer.getInstance().createJID(newMember, null));
                        } catch (IllegalArgumentException e) {
                            throw new ServiceException("Cannot parse a member value as a JID: " + newMember, groupEntity.getName(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST, e);
                        }
                    }
                    for (final String newAdmin : groupEntity.getAdmins()) {
                        try {
                            newAdmins.add(newAdmin.contains("@") ? new JID(newAdmin) : XMPPServer.getInstance().createJID(newAdmin, null));
                        } catch (IllegalArgumentException e) {
                            throw new ServiceException("Cannot parse a admin value as a JID: " + newAdmin, groupEntity.getName(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST, e);
                        }
                    }

                    group.setDescription(groupEntity.getDescription());
                    final String showInRoster = groupEntity.getShared() ? "onlyGroup" : "nobody";
                    group.getProperties().put("sharedRoster.showInRoster", showInRoster);

                    // Correct the member-list that already is in the group to match the desired state.
                    final Collection<JID> members = group.getMembers();
                    final Iterator<JID> membersIterator = members.iterator();
                    while (membersIterator.hasNext()) {
                        final JID oldMember = membersIterator.next();
                        if (newMembers.contains(oldMember)) {
                            // Already exists. No need to add it again.
                            newMembers.remove(oldMember);
                        } else {
                            // No longer exists. Remove from group.
                            membersIterator.remove();
                        }
                    }
                    for (final JID newMember : newMembers) {
                        // Unsure if #addAll works for the specific Collection subclass that is used in the Group implementation. Let's add them one-by-one to be safe.
                        members.add(newMember);
                    }

                    // Correct the admin-list that already is in the group to match the desired state.
                    final Collection<JID> admins = group.getAdmins();
                    final Iterator<JID> adminsIterator = admins.iterator();
                    while (adminsIterator.hasNext()) {
                        final JID oldAdmin = adminsIterator.next();
                        if (newAdmins.contains(oldAdmin)) {
                            // Already exists. No need to add it again.
                            newAdmins.remove(oldAdmin);
                        } else {
                            // No longer exists. Remove from group.
                            adminsIterator.remove();
                        }
                    }
                    for (final JID newAdmin : newAdmins) {
                        // Unsure if #addAll works for the specific Collection subclass that is used in the Group implementation. Let's add them one-by-one to be safe.
                        admins.add(newAdmin);
                    }
                } catch (GroupNotFoundException e) {
                    throw new ServiceException("Could not find group", groupName, ExceptionType.GROUP_NOT_FOUND,
                            Response.Status.NOT_FOUND, e);
                }
            } else {
                throw new ServiceException(
                        "Could not update the group. The group name is different to the payload group name.", groupName + " != " + groupEntity.getName(),
                        ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }
        } else {
            throw new ServiceException("Could not update new group", "groups",
                    ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        return group;
    }

    /**
     * Delete group.
     *
     * @param groupName
     *            the group name
     * @throws ServiceException
     *             the service exception
     */
    public void deleteGroup(String groupName) throws ServiceException {
        try {
            Group group = GroupManager.getInstance().getGroup(groupName);
            GroupManager.getInstance().deleteGroup(group);
        } catch (GroupNotFoundException e) {
            throw new ServiceException("Could not find group", groupName, ExceptionType.GROUP_NOT_FOUND,
                    Response.Status.NOT_FOUND, e);
        }
    }
}
