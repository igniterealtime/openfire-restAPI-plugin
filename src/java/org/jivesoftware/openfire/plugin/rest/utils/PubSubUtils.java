package org.jivesoftware.openfire.plugin.rest.utils;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.jivesoftware.openfire.plugin.rest.entity.omemo.OmemoDevicesList;
import org.jivesoftware.openfire.plugin.rest.entity.pubsub.PubSubNodePublishedItemPayload;
import org.jivesoftware.openfire.plugin.rest.entity.pubsub.PubSubNodePublishedItemPayloadBase;
import org.jivesoftware.openfire.plugin.rest.entity.pubsub.PubSubNodePublishedItemPayloadTypes;

import java.io.IOException;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


public class PubSubUtils {

    static private Logger log = LoggerFactory.getLogger(PubSubUtils.class);

    private static final JAXBContext JAXB_CONTEXT;
    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(OmemoDevicesList.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to initialize JAXBContext", e);
        }
    }

    public static PubSubNodePublishedItemPayload xmlStrToPayload(String xml, String id) {

        if (id == null || id.isEmpty()) {
            return new PubSubNodePublishedItemPayloadBase(xml);
        }
        PubSubNodePublishedItemPayloadTypes payloadType = PubSubNodePublishedItemPayloadTypes.OTHER;
        try{
            payloadType =
                PubSubNodePublishedItemPayloadTypes.valueOf(
                    id.replaceAll("[-+.^:,]","").toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Could not determine payload type for id: " + id +
                 "; Using Base");
            payloadType = PubSubNodePublishedItemPayloadTypes.OTHER;
        }

        try{
            switch (payloadType){
                case OMEMODEVICES:
                    Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();

                    StringReader reader = new StringReader(xml);
                    OmemoDevicesList devicesList = (OmemoDevicesList) unmarshaller.unmarshal(reader);
                    devicesList.setXmlPayload(xml);
                    return devicesList;
                case OTHER:
                default:
                    return new PubSubNodePublishedItemPayloadBase(xml);
            }
        } catch (JAXBException e){
            log.error("Could not init Mapper for id: " + id + " as " + payloadType.name(), e);
            return new PubSubNodePublishedItemPayloadBase(xml);
        }
    }

}
