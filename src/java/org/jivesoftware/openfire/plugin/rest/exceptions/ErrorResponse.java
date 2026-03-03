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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ErrorResponse.
 */
@XmlRootElement(name = "error")
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
