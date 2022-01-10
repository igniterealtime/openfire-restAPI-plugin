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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class SystemProperties.
 */
@XmlRootElement(name = "properties")
public class SystemProperties {

    /** The properties. */
    List<SystemProperty> properties;

    /**
     * Instantiates a new system properties.
     */
    public SystemProperties() {

    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    @XmlElement(name = "property")
    public List<SystemProperty> getProperties() {
        return properties;
    }


    /**
     * Sets the properties.
     *
     * @param properties the new properties
     */
    public void setProperties(List<SystemProperty> properties) {
        this.properties = properties;
    }
}
