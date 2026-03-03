package org.jivesoftware.openfire.plugin.rest.entity.pubsub;


import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "psnodeitem")
@XmlType(propOrder = { "nodeName", "nodeDescription", "nodeID", "nodeSubscriptions", "nodeAffiliations", "nodeCreationDate", "nodeModificationDate", "nodePublishedItems","nodePublishedItemCount" })
public class PubSubNodeEntity {

    private String nodeID;
    private String nodeName;
    private String nodeDescription;
    private int nodeSubscriptions;
    private int nodeAffiliations;

    private Date nodeCreationDate;
    private Date nodeModificationDate;

    private List<PubSubNodePublishedItemEntity> nodePublishedItems;
    private int nodePublishedItemCount;


    public PubSubNodeEntity() {
    }
    public PubSubNodeEntity(
        String nodeName,
        String nodeDescription,
        String nodeID,
        int nodeSubscriptions,
        int nodeAffiliations,
        Date nodeCreationDate,
        Date nodeModificationDate,
        List<PubSubNodePublishedItemEntity> nodePublishedItems
    ) {
        this.nodeName = nodeName;
        this.nodeDescription = nodeDescription;
        this.nodeID = nodeID;
        this.nodeSubscriptions = nodeSubscriptions;
        this.nodeAffiliations = nodeAffiliations;
        this.nodeCreationDate = nodeCreationDate;
        this.nodeModificationDate = nodeModificationDate;
        this.nodePublishedItems = nodePublishedItems;
        this.nodePublishedItemCount = nodePublishedItems.size();
    }

    @XmlElement
    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }
    @XmlElement
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    @XmlElement
    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }
    @XmlElement
    public int getNodeSubscriptions() {
        return nodeSubscriptions;
    }

    public void setNodeSubscriptions(int nodeSubscriptions) {
        this.nodeSubscriptions = nodeSubscriptions;
    }
    @XmlElement
    public int getNodeAffiliations() {
        return nodeAffiliations;
    }

    public void setNodeAffiliations(int nodeAffiliations) {
        this.nodeAffiliations = nodeAffiliations;
    }
    @XmlElement
    public Date getNodeCreationDate() {
        return nodeCreationDate;
    }

    public void setNodeCreationDate(Date nodeCreationDate) {
        this.nodeCreationDate = nodeCreationDate;
    }
    @XmlElement
    public Date getNodeModificationDate() {
        return nodeModificationDate;
    }

    public void setNodeModificationDate(Date nodeModificationDate) {
        this.nodeModificationDate = nodeModificationDate;
    }
    @XmlElement(name="psnodepupitem")
    @JsonProperty(value = "psnodepupitem")
    public List<PubSubNodePublishedItemEntity> getNodePublishedItems() {
        return nodePublishedItems;
    }

    public void setNodePublishedItems(List<PubSubNodePublishedItemEntity> nodePublishedItems) {
        this.nodePublishedItems = nodePublishedItems;
        this.nodePublishedItemCount = nodePublishedItems.size();
    }

    @XmlElement
    public int getNodePublishedItemCount() {
        return nodePublishedItemCount;
    }





}
