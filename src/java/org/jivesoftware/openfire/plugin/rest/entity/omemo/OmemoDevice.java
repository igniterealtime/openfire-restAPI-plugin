package org.jivesoftware.openfire.plugin.rest.entity.omemo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class OmemoDevice {
        @XmlAttribute
        private long id;

        @XmlAttribute
        private String name;

        @XmlAttribute
        private String created;

        @XmlAttribute
        private String hwidentifier;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getCreated() {
            return created;
        }

        public String getHwidentifier() {
            return hwidentifier;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public void setHwidentifier(String hwidentifier) {
            this.hwidentifier = hwidentifier;
        }

}
