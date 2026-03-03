package org.jivesoftware.openfire.plugin.rest.entity.pubsub;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

import static org.jivesoftware.openfire.plugin.rest.utils.PubSubUtils.xmlStrToPayload;


@XmlRootElement(name = "psnodepupitem")
@XmlType(propOrder = { "id", "publisher", "creationDate","isRawXMLPayload" ,"payload" })
public class PubSubNodePublishedItemEntity {

    private String id;
    private String publisher;
    private Date creationDate;
    private boolean isRawXMLPayload;
    private PubSubNodePublishedItemPayload payload;


    public PubSubNodePublishedItemEntity() {}

    public PubSubNodePublishedItemEntity(String id, String publisher, Date creationDate, String payloadXml) {
        this.id = id;
        this.publisher = publisher;
        this.creationDate = creationDate;
        this.payload = xmlStrToPayload(payloadXml, id);
        this.isRawXMLPayload = this.payload.getClass() == PubSubNodePublishedItemPayloadBase.class;
    }

    @XmlElement
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @XmlElement
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @XmlElement
    public boolean getIsRawXMLPayload(){
        return isRawXMLPayload;
    }

    @XmlElement
    public PubSubNodePublishedItemPayload getPayload() {
        return payload;
    }
    public void setPayloadXml(String payloadXml) {
        this.payload = xmlStrToPayload(payloadXml, this.id);
        this.isRawXMLPayload = this.payload.getClass() == PubSubNodePublishedItemPayloadBase.class;
    }
}
