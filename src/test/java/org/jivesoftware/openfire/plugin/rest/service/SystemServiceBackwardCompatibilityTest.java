/*
 * Copyright (C) 2026 Ignite Realtime Foundation. All rights reserved.
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
package org.jivesoftware.openfire.plugin.rest.service;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jivesoftware.openfire.plugin.rest.CustomJacksonMapperProvider;
import org.jivesoftware.openfire.plugin.rest.controller.SystemController;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperties;
import org.jivesoftware.openfire.plugin.rest.entity.SystemProperty;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Asserts that service endpoints in <tt>restapi/v1/system</tt> have a stable signature.
 *
 * The tests in this class interact with the REST API as instantiated in this plugin, using a mock service
 * controller implementation. Unlike the other <tt>*BackwardCompatibilityTest</tt> classes in this package, the
 * expected values recorded here were not captured from a historic release of this plugin, as none previously existed
 * for this service. Instead, they establish the current, stable response shape, so that future changes are made
 * deliberately rather than accidentally.
 *
 * This class also covers the scenario reported in
 * <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242">issue #242</a>, where
 * {@code GET /system/properties/{propertyKey}} could 404 for a property that {@code GET /system/properties} listed.
 * The controller-level cause and fix are covered by {@code SystemControllerTest}; here, the fix is confirmed to be
 * reachable and correctly mapped to a 404 response through the actual service and exception mapper.
 */
public class SystemServiceBackwardCompatibilityTest extends JerseyTest {

    private static final String EXISTING_KEY = "foo.bar.xyz";
    private static final String EXISTING_VALUE = "false";
    private static final String MISSING_KEY = "does.not.exist";

    /**
     * Constructs the mock of the service controller that mimics the 'business logic' normally provided by a running
     * Openfire server.
     *
     * @return A mock of a SystemController
     */
    public static SystemController constructMockController() throws ServiceException {
        final SystemController controller = mock(SystemController.class, withSettings().lenient());

        final SystemProperty one = new SystemProperty(EXISTING_KEY, EXISTING_VALUE);
        final SystemProperty two = new SystemProperty("xmpp.domain", "example.org");

        doAnswer(invocationOnMock -> {
            final SystemProperties result = new SystemProperties();
            result.setProperties(Arrays.asList(one, two));
            return result;
        }).when(controller).getSystemProperties();

        doAnswer(invocationOnMock -> one).when(controller).getSystemProperty(eq(EXISTING_KEY));
        doAnswer(invocationOnMock -> {
            throw new ServiceException("Could not find property", MISSING_KEY, ExceptionType.PROPERTY_NOT_FOUND, Response.Status.NOT_FOUND);
        }).when(controller).getSystemProperty(eq(MISSING_KEY));

        doAnswer(invocationOnMock -> null).when(controller).createSystemProperty(any());
        doAnswer(invocationOnMock -> null).when(controller).updateSystemProperty(eq(EXISTING_KEY), any());
        doAnswer(invocationOnMock -> null).when(controller).deleteSystemProperty(eq(EXISTING_KEY));

        return controller;
    }

    @BeforeClass
    public static void setUpClass() throws ServiceException {
        // Override the service controller with a mock controller.
        SystemController.setInstance(constructMockController());
    }

    @Override
    protected Application configure() {
        // Configures the Jersey web application. This should mimic JerseyWrapper's implementation.
        final ResourceConfig config = new ResourceConfig(SystemService.class, RESTExceptionMapper.class, CustomJacksonMapperProvider.class);

        // RESTExceptionMapper injects the servlet request via @Context. The test container used here isn't
        // servlet-based, so nothing would otherwise supply that binding, causing a NullPointerException from
        // the mapper for every non-2xx response.
        final HttpServletRequest mockRequest = mock(HttpServletRequest.class, withSettings().lenient());
        when(mockRequest.getMethod()).thenReturn("GET");
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mockRequest).to(HttpServletRequest.class);
            }
        });

        return config;
    }

    @Test
    public void getPropertiesXml() {
        Response response = target("restapi/v1/system/properties").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match the current, stable response shape.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><properties><property key=\"foo.bar.xyz\" value=\"false\"/><property key=\"xmpp.domain\" value=\"example.org\"/></properties>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getPropertiesXml()}
     */
    @Test
    public void getPropertiesJson() {
        Response response = target("restapi/v1/system/properties").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match the current, stable response shape.", "{\"property\":[{\"key\":\"foo.bar.xyz\",\"value\":\"false\"},{\"key\":\"xmpp.domain\",\"value\":\"example.org\"}]}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Retrieves a property that is known to exist. This is the counterpart of {@link #getPropertyNotFoundXml()},
     * which exercises the 404 that issue #242 reported for a property that does, in fact, exist.
     */
    @Test
    public void getPropertyXml() {
        Response response = target("restapi/v1/system/properties/" + EXISTING_KEY).request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match the current, stable response shape.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><property key=\"foo.bar.xyz\" value=\"false\"/>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getPropertyXml()}
     */
    @Test
    public void getPropertyJson() {
        Response response = target("restapi/v1/system/properties/" + EXISTING_KEY).request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match the current, stable response shape.", "{\"key\":\"foo.bar.xyz\",\"value\":\"false\"}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Retrieves a property that does not exist, and asserts that this is reported as a 404, rather than the request
     * erroring out in some other fashion.
     */
    @Test
    public void getPropertyNotFoundXml() {
        Response response = target("restapi/v1/system/properties/" + MISSING_KEY).request(MediaType.APPLICATION_XML).get();

        assertEquals("HTTP response should have a status code that is 404.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getPropertyNotFoundXml()}
     */
    @Test
    public void getPropertyNotFoundJson() {
        Response response = target("restapi/v1/system/properties/" + MISSING_KEY).request(MediaType.APPLICATION_JSON).get();

        assertEquals("HTTP response should have a status code that is 404.", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void createPropertyXml() {
        Response response = target("restapi/v1/system/properties").request(MediaType.APPLICATION_XML)
            .post(Entity.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><property key=\"" + EXISTING_KEY + "\" value=\"" + EXISTING_VALUE + "\"/>"));

        assertEquals("HTTP response should have a status code that is 201.", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    @Test
    public void updatePropertyXml() {
        Response response = target("restapi/v1/system/properties/" + EXISTING_KEY).request(MediaType.APPLICATION_XML)
            .put(Entity.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><property key=\"" + EXISTING_KEY + "\" value=\"" + EXISTING_VALUE + "\"/>"));

        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void deletePropertyXml() {
        Response response = target("restapi/v1/system/properties/" + EXISTING_KEY).request(MediaType.APPLICATION_XML).delete();

        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
