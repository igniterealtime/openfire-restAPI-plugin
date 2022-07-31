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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "outcasts")
public class OutcastEntities extends AffiliatedEntities
{
    List<String> outcasts;

    public OutcastEntities() {
    }

    public OutcastEntities(List<String> outcasts) {
        this.outcasts = outcasts;
    }

    @XmlElement(name = "outcast")
    @JsonProperty(value = "outcasts")
    public List<String> getOutcasts() {
        return outcasts;
    }

    public void setOutcasts(List<String> outcasts) {
        this.outcasts = outcasts;
    }

    @Override
    public String[] asUserReferences()
    {
        return outcasts == null ? new String[0] : outcasts.toArray(new String[0]);
    }
}
