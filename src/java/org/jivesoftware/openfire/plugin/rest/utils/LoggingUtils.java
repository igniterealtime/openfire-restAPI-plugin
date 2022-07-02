/*
 * Copyright (C) 2022 Ignite Realtime Foundation. All rights reserved.
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

import org.eclipse.jetty.util.log.Log;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class LoggingUtils {
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("RestAPI-Plugin-Audit");
    private static final Logger LOG = LoggerFactory.getLogger(LoggingUtils.class);

    public enum AuditEvent {
        //Clustering
        CLUSTERING_GET_STATUS,
        CLUSTERING_GET_NODES,
        CLUSTERING_GET_NODE,

        //Groups
        GROUPS_LIST,
        GROUPS_GET_BY_NAME,
        GROUPS_CREATE,
        GROUPS_UPDATE_BY_NAME,
        GROUPS_DELETE,

        //JustMarried
        USER_CHANGE_NAME,

        //Messages
        MESSAGE_BROADCAST,

        //Message Archive
        MESSAGE_ARCHIVE_UNREAD_COUNT,

        //MUC
        // - MUC Affiliations
        MUC_LIST_AFFILIATED_USERS_FOR_AFFILIATION,
        MUC_REPLACE_AFFILIATED_USERS_FOR_AFFILIATION,
        MUC_REMOVE_AFFILIATED_USER_OR_GROUP_FOR_AFFILIATION,
        MUC_ADD_AFFILIATED_USERS_FOR_AFFILIATION,
        MUC_ADD_AFFILIATED_USER_OR_GROUP_AS_ADMIN,
        MUC_ADD_AFFILIATED_USER_OR_GROUP_AS_MEMBER,
        MUC_ADD_AFFILIATED_USER_OR_GROUP_AS_OUTCAST,
        MUC_ADD_AFFILIATED_USER_OR_GROUP_AS_OWNER,
        // - MUC Rooms
        MUC_LIST_ROOMS,
        MUC_GET_ROOM,
        MUC_DELETE_ROOM,
        MUC_CREATE_ROOM,
        MUC_UPDATE_ROOM,
        MUC_GET_PARTICIPANT_LIST,
        MUC_GET_OCCUPANT_LIST,
        MUC_GET_ROOM_HISTORY,
        MUC_INVITE_USER,
        ;
    }

    public static void auditEvent(AuditEvent event){
        auditEvent(event, "");
    }

    public static void auditEvent(AuditEvent event, Object... parameters){
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            String parameterString = parseParameters(parameters);
            String logMessage = "Event: " + event;
            logMessage += " - ";
            logMessage += "Parameters: + " + parameterString;
            logMessage += " - ";
            logMessage += "Caller: " + getCaller();
            AUDIT_LOG.info(logMessage);
        };
    }

    private static String parseParameters(Object[] parameters) {
        //TODO: Does this belong here?
        ArrayList<String> parsed = new ArrayList<>();
        for (Object obj: parameters) {
            if(obj == null){
                parsed.add("null");
            } else {
                try {
                    parsed.add(obj.toString());
                } catch (Exception e) {
                    parsed.add("unparseable");
                }
            }
        }
        return parsed.toString();
    }

    /*
    * Returns the name and method of the calling class.
    */
    private static String getCaller() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if(element.getClassName().equals(LoggingUtils.class.getName())){
                    continue;
                }
                return element.getClassName() + "." + element.getMethodName();
            }
        } catch (Exception e) {
            LOG.error("Unable to get caller of the logger. This should be impossible.", e);
        }
        return "unknown";
    }
}
