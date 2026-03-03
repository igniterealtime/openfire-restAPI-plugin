package org.jivesoftware.openfire.plugin.rest.entity.pubsub;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "psnodes")
public class PubSubNodeEntities {
    private List<PubSubNodeEntity> nodes;
    private int count;

    public PubSubNodeEntities() {
    }
    public PubSubNodeEntities(List<PubSubNodeEntity> nodes) {
        this.nodes = nodes;
        this.count = nodes.size();
    }
    public PubSubNodeEntities(List<PubSubNodeEntity> nodes, int count) {
        this.nodes = nodes;
        this.count = count;
    }

    @XmlElement(name = "psnodeitem")
    @JsonProperty(value = "psnodeitem")
    public List<PubSubNodeEntity> getNodes() {
        return nodes;
    }
    @XmlElement(name = "psnodecount")
    public int getCount() {
        return count;
    }

    public void setNodes(List<PubSubNodeEntity> nodes) {
        this.nodes = nodes;
        this.count = nodes.size();
    }
}
