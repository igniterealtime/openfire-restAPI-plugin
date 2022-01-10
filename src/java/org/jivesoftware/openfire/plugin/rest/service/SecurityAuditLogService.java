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

package org.jivesoftware.openfire.plugin.rest.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jivesoftware.openfire.plugin.rest.controller.SecurityAuditLogController;
import org.jivesoftware.openfire.plugin.rest.entity.SecurityAuditLogs;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("restapi/v1/logs/security")
@Tag(name = "Security Audit Log", description = "Inspecting the security audit log.")
public class SecurityAuditLogService {

	private SecurityAuditLogController securityAuditLogController;

	@PostConstruct
	public void init() {
		securityAuditLogController = SecurityAuditLogController.getInstance();
	}

	@GET
    @Operation( summary = "Get log entries",
        description = "Retrieve entries from the security audit log.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The requested log entries.", content = @Content(schema = @Schema(implementation = SecurityAuditLogs.class))),
            @ApiResponse(responseCode = "403", description = "The audit log is not readable (configured to be write-only).")
        })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public SecurityAuditLogs getSecurityAuditLogs(
            @Parameter(description = "The name of a user for which to filter events.", example = "admin", required = false) @QueryParam("username") String username,
            @Parameter(description = "Number of log entries to skip.", example = "0", required = false) @QueryParam("offset") int offset,
            @Parameter(description = "Number of log entries to retrieve.", example = "100", required = false) @DefaultValue("100") @QueryParam("limit") int limit,
            @Parameter(description = "Oldest timestamp of range of logs to retrieve. 0 for 'forever'.", required = false) @QueryParam("startTime") long startTime,
            @Parameter(description = "Most recent timestamp of range of logs to retrieve. 0 for 'now'.", required = false) @QueryParam("endTime") long endTime)
        throws ServiceException
    {
		return securityAuditLogController.getSecurityAuditLogs(username, offset, limit, startTime, endTime);
	}
}
