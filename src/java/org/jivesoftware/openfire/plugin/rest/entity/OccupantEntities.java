package org.jivesoftware.openfire.plugin.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "occupants")
public class OccupantEntities {
    List<OccupantEntity> occupants;

    public OccupantEntities() {
    }

    public OccupantEntities(List<OccupantEntity> occupants) {
        this.occupants = occupants;
    }

    @XmlElement(name = "occupant")
    @JsonProperty(value = "occupants")
    public List<OccupantEntity> getOccupants() {
        return occupants;
    }

    public void setOccupants(List<OccupantEntity> occupants) {
        this.occupants = occupants;
    }
}
