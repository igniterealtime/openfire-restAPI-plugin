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

/**
 * The Class ExceptionType.
 */
public final class ExceptionType {
    /** The Constant ILLEGAL_ARGUMENT_EXCEPTION. */
    public static final String ILLEGAL_ARGUMENT_EXCEPTION = "IllegalArgumentException";
    
    /** The Constant SHARED_GROUP_EXCEPTION. */
    public static final String SHARED_GROUP_EXCEPTION = "SharedGroupException";

    /** The Constant PROPERTY_NOT_FOUND. */
    public static final String PROPERTY_NOT_FOUND = "PropertyNotFoundException";

    /** The Constant USER_ALREADY_EXISTS_EXCEPTION. */
    public static final String USER_ALREADY_EXISTS_EXCEPTION = "UserAlreadyExistsException";

    /** The Constant USER_NOT_FOUND_EXCEPTION. */
    public static final String USER_NOT_FOUND_EXCEPTION = "UserNotFoundException";

    /** The Constant GROUP_ALREADY_EXISTS. */
    public static final String GROUP_ALREADY_EXISTS = "GroupAlreadyExistsException";

    /** The Constant GROUP_NOT_FOUND. */
    public static final String GROUP_NOT_FOUND = "GroupNotFoundException";

    public static final String MUCSERVICE_NOT_FOUND = "MUCServiceNotFoundException";

    /** The Constant ROOM_NOT_FOUND. */
    public static final String ROOM_NOT_FOUND = "RoomNotFoundException";

    /** The Constant NOT_ALLOWED. */
    public static final String NOT_ALLOWED = "NotAllowedException";

    /** The Constant ALREADY_EXISTS. */
    public static final String ALREADY_EXISTS = "AlreadyExistsException";

    /** The Constant CLUSTER_NODE_NOT_FOUND. */
    public static final String CLUSTER_NODE_NOT_FOUND = "ClusterNodeNotFoundException";

    /**
     * Instantiates a new exception type.
     */
    private ExceptionType() {
    }
}
