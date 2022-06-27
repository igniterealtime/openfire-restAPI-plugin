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
        CLUSTERING_GET_STATUS("GetClusterStatus", "Get current status of clustering"),
        CLUSTERING_GET_NODES("GetClusterNodes", "Get info on all cluster nodes"),
        CLUSTERING_GET_NODE("GetClusterNode", "Get info on a specific cluster node by ID"),

        GROUPS_LIST("GetGroups", "List all groups"),
        GROUPS_GET_BY_NAME("GetGroupByName", "Fetch a group given the name"),
        GROUPS_CREATE("CreateGroup", "Create a group given the definition"),
        GROUPS_UPDATE_BY_NAME("UpdateGroup", "Update the named group with the given definition"),
        GROUPS_DELETE("DeleteGroup", "Delete group with the given name"),
        ;

        private final String name;
        private final String description;
        AuditEvent(final String name, final String description){
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    public static void auditEvent(String event){
        auditEvent(event, "");
    }

    public static void auditEvent(String event, Object... parameters){
        if (JiveGlobals.getBooleanProperty(RESTServicePlugin.SERVICE_LOGGING_ENABLED, false)) {
            String[] parsedParameters = parseParameters(parameters);
            String logMessage = "Event: " + event;
            logMessage += " - ";
            logMessage += "Parameters: + " + Arrays.toString(parsedParameters);
            AUDIT_LOG.info(logMessage);
        };
    }

    private static String[] parseParameters(Object[] parameters) {
        ArrayList<String> parsed = new ArrayList<>();
        for (Object obj: parameters) {
            if(obj == null){
                parsed.add("null");
            } else {
                parsed.add(obj.toString());
            }
        }
        return parsed.toArray(new String[0]);
    }
}
