package org.jivesoftware.openfire.plugin.rest.entity.omemo;



import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.jivesoftware.openfire.plugin.rest.entity.pubsub.PubSubNodePublishedItemPayload;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "List", namespace = "eu.siacs.conversations.axolotl")
@XmlAccessorType(XmlAccessType.FIELD)
public class OmemoDevicesList implements PubSubNodePublishedItemPayload {

    @XmlElement(name = "device", namespace = "eu.siacs.conversations.axolotl")
    private List<OmemoDevice> device;

    @XmlTransient
    private String xmlPayload;

    public List<OmemoDevice> getDevice() {
        return device;
    }

    public void setDevice(List<OmemoDevice> device) {
        this.device = device;
    }

    @Override
    public String getXmlPayload() {
        return this.xmlPayload;
    }

    public void setXmlPayload(String xmlPayload) {
        this.xmlPayload = xmlPayload;
    }

}
