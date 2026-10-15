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

package org.jivesoftware.openfire.plugin.rest.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class SecurityAuditLog.
 */
@XmlRootElement(name = "log")
@Schema(description = "An entry of the security audit log.")
public class SecurityAuditLog {

    /** The log id. */
    private long logId;
    
    /** The username. */
    private String username;
    
    /** The timestamp. */
    private long timestamp;
    
    /** The summary. */
    private String summary;
    
    /** The node. */
    private String node;
    
    /** The details. */
    private String details;

	/**
	 * Instantiates a new security audit log.
	 */
	public SecurityAuditLog() {
	}

	/**
	 * Instantiates a new security audit log.
	 *
	 * @param logId the log id
	 * @param username the username
	 * @param timestamp the timestamp
	 * @param summary the summary
	 * @param node the node
	 * @param details the details
	 */
	public SecurityAuditLog(long logId, String username, long timestamp, String summary, String node, String details) {
		this.logId = logId;
		this.username = username;
		this.timestamp = timestamp;
		this.summary = summary;
		this.node = node;
		this.details = details;
	}

	/**
	 * Gets the log id.
	 *
	 * @return the log id
	 */
	@XmlElement
	@Schema(description = "The unique identifier of the log entry.", example = "42")
	public long getLogId() {
		return logId;
	}

	/**
	 * Sets the log id.
	 *
	 * @param logId the new log id
	 */
	public void setLogId(long logId) {
		this.logId = logId;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	@XmlElement
	@Schema(description = "The username of the user that performed the audited action.", example = "admin")
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	@XmlElement
	@Schema(description = "The moment at which the audited action occurred, in seconds since the Unix epoch.", example = "1769862896")
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the summary.
	 *
	 * @return the summary
	 */
	@XmlElement
	@Schema(description = "A short description of the audited action.", example = "Created new user john")
	public String getSummary() {
		return summary;
	}

	/**
	 * Sets the summary.
	 *
	 * @param summary the new summary
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Gets the node.
	 *
	 * @return the node
	 */
	@XmlElement
	@Schema(description = "The node that triggered the audited action, usually a host name or IP address.", example = "xmpp1.example.org")
	public String getNode() {
		return node;
	}

	/**
	 * Sets the node.
	 *
	 * @param node the new node
	 */
	public void setNode(String node) {
		this.node = node;
	}

	/**
	 * Gets the details.
	 *
	 * @return the details
	 */
	@XmlElement
	@Schema(description = "Detailed information about the audited action.", example = "name = John Doe, email = john@example.org")
	public String getDetails() {
		return details;
	}

	/**
	 * Sets the details.
	 *
	 * @param details the new details
	 */
	public void setDetails(String details) {
		this.details = details;
	}


	
}
