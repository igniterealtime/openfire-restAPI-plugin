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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

/**
 * The Class MsgArchiveController.
 */
public class MsgArchiveController {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(MsgArchiveController.class);

    /** The Constant INSTANCE. */
    public static final MsgArchiveController INSTANCE = new MsgArchiveController();

    /** The Constant USER_MESSAGE_COUNT. */
    private static final String USER_MESSAGE_COUNT = "select COUNT(1) from ofMessageArchive a " +
            "join ofPresence p on (a.sentDate > p.offlineDate) " +
            "WHERE a.toJID = ? AND p.username = ?";

    /**
     * Gets the single instance of MsgArchiveController.
     *
     * @return single instance of MsgArchiveController
     */
    public static MsgArchiveController getInstance() {
        return INSTANCE;
    }

    /**
     * The Constructor.
     */
    private MsgArchiveController() {
    }

    /**
     * Returns the total number of messages that haven't been delivered to the user.
     *
     * @param jid the jid
     * @return the total number of user unread messages.
     */
    public int getUnReadMessagesCount(JID jid) {
        int messageCount = 0;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(USER_MESSAGE_COUNT);
            pstmt.setString(1, jid.toBareJID());
            pstmt.setString(2, jid.getNode());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                messageCount = rs.getInt(1);
            }
        } catch (SQLException sqle) {
            LOG.warn("A database error prevented successful retrieval of the unread message count for user '{}' (the value '0' will be returned instead).", jid, sqle);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return messageCount;
    }
}
