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

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.plugin.rest.entity.SessionsCount;

/**
 * The Class StatisticsController.
 */
public class StatisticsController {
    
    /** The Constant INSTANCE. */
    public static final StatisticsController INSTANCE = new StatisticsController();

    /**
     * Gets the instance.
     *
     * @return the instance
     */
    public static StatisticsController getInstance() {
        return INSTANCE;
    }
    
    /**
     * Gets the concurent sessions.
     *
     * @return the concurent sessions
     */
    public SessionsCount getConcurentSessions() {
        int userSessionsCountLocal = SessionManager.getInstance().getUserSessionsCount(true);
        int userSessionsCountCluster = SessionManager.getInstance().getUserSessionsCount(false);
        
        return new SessionsCount(userSessionsCountLocal, userSessionsCountCluster);
    }
}
