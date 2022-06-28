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

import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class LoggingUtils {
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("RestAPI-Plugin-Audit");

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
        USER_CHANGE_NAME
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
            AUDIT_LOG.info(logMessage);
        };
    }

    private static String parseParameters(Object[] parameters) {
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
}
