package org.jivesoftware.openfire.plugin.rest.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "clustering")
public class ClusteringEntity {

    String status;

    public ClusteringEntity(){};

    public ClusteringEntity(String status){
        this.status = status;
    }

    @XmlElement()
    public String getStatus(){
        return status;
    }

}
