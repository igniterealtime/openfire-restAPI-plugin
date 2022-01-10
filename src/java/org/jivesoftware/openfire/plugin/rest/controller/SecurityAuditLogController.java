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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.plugin.rest.entity.SecurityAuditLog;
import org.jivesoftware.openfire.plugin.rest.entity.SecurityAuditLogs;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.security.AuditWriteOnlyException;
import org.jivesoftware.openfire.security.SecurityAuditEvent;
import org.jivesoftware.openfire.security.SecurityAuditManager;

public class SecurityAuditLogController {
	public static final SecurityAuditLogController INSTANCE = new SecurityAuditLogController();

	public static SecurityAuditLogController getInstance() {
		return INSTANCE;
	}

	public SecurityAuditLogs getSecurityAuditLogs(String username, int offset, int limit, long startTimeTimeStamp, 
			long endTimeTimeStamp) throws ServiceException {
		Date startTime = null;
		Date endTime = null;
		
		if (startTimeTimeStamp != 0) {
			startTime = new Date(startTimeTimeStamp * 1000);
		}
		
		if (endTimeTimeStamp != 0) {
			endTime = new Date(endTimeTimeStamp * 1000);
		}
		
		List<SecurityAuditEvent> events = new ArrayList<SecurityAuditEvent>();
		
		try {
			events = SecurityAuditManager.getInstance().getEvents(username, offset, limit, startTime, endTime);
		} catch (AuditWriteOnlyException e) {
			throw new ServiceException("Could not get security audit logs, because the permission is set to write only",
					"SecurityLogs", "AuditWriteOnlyException", Response.Status.FORBIDDEN);
		}
		
		List<SecurityAuditLog> securityAuditLogs = new ArrayList<SecurityAuditLog>();
		for (SecurityAuditEvent event : events) {
			SecurityAuditLog log = new SecurityAuditLog(event.getMsgID(), event.getUsername(), event.getEventStamp().getTime() / 1000, event.getSummary(), event.getNode(), event.getDetails());
			securityAuditLogs.add(log);
		}
		
		return new SecurityAuditLogs(securityAuditLogs);
	}
}
