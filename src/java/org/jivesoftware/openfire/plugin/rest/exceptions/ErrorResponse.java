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

package org.jivesoftware.openfire.plugin.rest.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ErrorResponse.
 */
@XmlRootElement(name = "error")
@Schema(description = "A description of an error that occurred while processing a request.")
public class ErrorResponse {

	/** The resource. */
	private String resource;

	/** The message. */
	private String message;

	/** The exception. */
	private String exception;

	/** The exception stack. */
	private String exceptionStack;

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@XmlElement(name = "resource")
	@Schema(description = "The resource (for example, a username or room name) that the error relates to.", example = "john")
	public String getResource() {
		return resource;
	}

	/**
	 * Sets the resource.
	 *
	 * @param resource the new resource
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	@XmlElement(name = "message")
	@Schema(description = "A description of the error.", example = "Could not get user")
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets the exception.
	 *
	 * @return the exception
	 */
	@XmlElement(name = "exception")
	@Schema(description = "The type of the error.", example = "UserNotFoundException")
	public String getException() {
		return exception;
	}

	/**
	 * Sets the exception.
	 *
	 * @param exception the new exception
	 */
	public void setException(String exception) {
		this.exception = exception;
	}

	/**
	 * Gets the exception stack.
	 *
	 * @return the exception stack
	 */
	@XmlElement(name = "exceptionStack")
	@Schema(hidden = true)
	public String getExceptionStack() {
		return exceptionStack;
	}

	/**
	 * Sets the exception stack.
	 *
	 * @param exceptionStack the new exception stack
	 */
	public void setExceptionStack(String exceptionStack) {
		this.exceptionStack = exceptionStack;
	}
}
