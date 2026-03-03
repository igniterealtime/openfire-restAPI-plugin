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
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "results")
@XmlType(propOrder = { "successResults", "failureResults", "otherResults" })
public class RoomCreationResultEntities {
    List<RoomCreationResultEntity> successResults;
    List<RoomCreationResultEntity> failureResults;

    // This last list is for if a new result type is defined, but no extra result list is added here - a "catch all"
    List<RoomCreationResultEntity> otherResults;

    public RoomCreationResultEntities() {
        this.successResults = new ArrayList<>();
        this.failureResults = new ArrayList<>();
        this.otherResults = new ArrayList<>();
    }

    public RoomCreationResultEntities(List<RoomCreationResultEntity> results) {
        this();
        addResults(results);
    }

    public void addResults(List<RoomCreationResultEntity> resultsToAdd) {
        resultsToAdd.forEach(this::addResult);
    }

    public void addResult(RoomCreationResultEntity resultToAdd) {
        switch (resultToAdd.getResultType()) {
            case Success:
                this.successResults.add(resultToAdd);
                break;
            case Failure:
                this.failureResults.add(resultToAdd);
                break;
            default:
                this.otherResults.add(resultToAdd);
        }
    }

    @XmlElement(name = "result")
    @XmlElementWrapper(name = "success")
    @JsonProperty(value = "success")
    @Schema(description = "All creation results of type success")
    public List<RoomCreationResultEntity> getSuccessResults() {
        return successResults;
    }

    @XmlElement(name = "result")
    @XmlElementWrapper(name = "failure")
    @JsonProperty(value = "failure")
    @Schema(description = "All creation results of type failure")
    public List<RoomCreationResultEntity> getFailureResults() {
        return failureResults;
    }

    @XmlElement(name = "result")
    @XmlElementWrapper(name = "other")
    @JsonProperty(value = "other")
    @Schema(description = "All creation results of a type other than success or failure")
    public List<RoomCreationResultEntity> getOtherResults() {
        return otherResults;
    }
}
