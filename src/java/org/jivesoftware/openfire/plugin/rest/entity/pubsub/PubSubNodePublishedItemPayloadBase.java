package org.jivesoftware.openfire.plugin.rest.entity.pubsub;

public class PubSubNodePublishedItemPayloadBase implements PubSubNodePublishedItemPayload {
    protected String xmlPayload = "";

    public PubSubNodePublishedItemPayloadBase(String payload) {
        this.xmlPayload = payload;
    }

    public String getXmlPayload() {
        return this.xmlPayload;
    }
}
