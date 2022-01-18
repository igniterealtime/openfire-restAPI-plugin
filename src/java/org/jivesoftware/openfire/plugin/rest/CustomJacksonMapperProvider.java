/*
 * Copyright (C) 2022 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.openfire.plugin.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Replaces the default ObjectMapper behavior as provided by Jackson.
 *
 * @author Guus der Kinderen, guus.der.kinderen@gmail.com
 */
@Provider
public class CustomJacksonMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper mapper;

    public CustomJacksonMapperProvider() {
        mapper = new ObjectMapper();

        // Configure Jackson to use JAXB annotations as the primary, and  Jackson annotations as the secondary source.
        mapper.registerModule(new JaxbAnnotationModule());
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
