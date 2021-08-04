package org.jivesoftware.openfire.plugin.rest.entity;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "clustering")
public class ClusteringEntities {

    String status;

    public ClusteringEntities(){};

    public ClusteringEntities(String status){
        this.status = status;
    }

    @XmlElement(name = "status")
    @JsonProperty(value = "status")
    public String getStatus(){
        return status;
    }

}
