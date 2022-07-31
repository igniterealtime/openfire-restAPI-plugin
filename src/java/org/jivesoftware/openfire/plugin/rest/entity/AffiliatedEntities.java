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

/**
 * A base class for pre-existing classes that each represent a collection of MUC-room affiliated users of a specific
 * type.
 *
 * The existence of this base class is to be able to generically process all types of affiliations, while each subclass
 * is allowed to retain the affiliation-specific JABX annotations.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public abstract class AffiliatedEntities
{
    /**
     * The list of user references (could be JIDs, usernames, Group-JIDs or group names) that are all affiliated in a
     * particular way to a MUC room.
     *
     * @return a list of user references
     */
    public abstract String[] asUserReferences();
}
