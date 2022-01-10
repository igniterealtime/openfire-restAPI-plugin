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

import javax.ws.rs.core.Response.Status;

/**
 * The Class ServiceException.
 */
public class ServiceException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4351720088030656859L;

    /** The resource. */
    private String resource;

    /** The exception. */
    private String exception;

    /** The status. */
    private Status status;

    /**
     * Instantiates a new service exception.
     *
     * @param msg the msg
     * @param resource the resource
     * @param exception the exception
     * @param status the status
     */
    public ServiceException(String msg, String resource, String exception, Status status) {
        super(msg);
        this.resource = resource;
        this.exception = exception;
        this.status = status;
    }

    /**
     * Instantiates a new service exception.
     *
     * @param msg the msg
     * @param resource the resource
     * @param exception the exception
     * @param status the status
     * @param cause the cause
     */
    public ServiceException(String msg, String resource, String exception, Status status, Throwable cause) {
        super(msg, cause);
        this.resource = resource;
        this.exception = exception;
        this.status = status;
    }

    /**
     * Gets the resource.
     * 
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     * 
     * @param resource
     *            the new resource
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Gets the exception.
     * 
     * @return the exception
     */
    public String getException() {
        return exception;
    }

    /**
     * Sets the exception.
     * 
     * @param exception
     *            the new exception
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
