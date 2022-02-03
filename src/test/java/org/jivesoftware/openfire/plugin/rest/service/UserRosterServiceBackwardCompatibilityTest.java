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
package org.jivesoftware.openfire.plugin.rest.service;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jivesoftware.openfire.plugin.rest.CustomJacksonMapperProvider;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.RosterEntities;
import org.jivesoftware.openfire.plugin.rest.entity.RosterItemEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.roster.RosterItem;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Asserts that service endpoints in <tt>restapi/v1/users/{username}/roster</tt> have a stable signature.
 *
 * The tests in this class interact with the REST API as instantiated in this plugin, and compare the output to output
 * that was previously recorded (at the time of test implementation) using older versions of Openfire and the REST API
 * plugin.
 *
 * This test implementation instantiate the REST API using the Jersey Test framework and a mock service controller
 * implementation, that, during implementation, has been verified to yield the same results as comparable interaction
 * with a fully deployed Openfire server on which the REST API plugin was installed.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public class UserRosterServiceBackwardCompatibilityTest extends JerseyTest {

    /**
     * Constructs the mock of the service controller that mimics the 'business logic' normally provided by a running
     * Openfire server.
     *
     * @return A mock of a UserServiceController
     */
    public static UserServiceController constructMockController() throws ServiceException {
        final UserServiceController controller = mock(UserServiceController.class, withSettings().lenient());

        final RosterItemEntity entity = new RosterItemEntity();
        entity.setJid("john@example.org");
        entity.setNickname("John");
        entity.setGroups(Collections.emptyList());
        entity.setSubscriptionType(RosterItem.SubType.BOTH.getValue());

        doAnswer(invocationOnMock -> new RosterEntities(Collections.singletonList(entity)))
            .when(controller).getRosterEntities(any());
        return controller;
    }

    @BeforeClass
    public static void setUpClass() throws ServiceException {
        // Override the service controller with a mock controller.
        UserServiceController.setInstance(constructMockController());
    }

    @Override
    protected Application configure() {
        // Configures the Jersey web application. This should mimic JerseyWrapper's implementation.
        return new ResourceConfig(UserRosterService.class, RESTExceptionMapper.class, CustomJacksonMapperProvider.class);
    }

    /**
     * Retrieves an XML representation of a user's roster from the <tt>restapi/v1/users/{username}/roster</tt> endpoint,
     * and asserts that representation is equal to a representation that was recorded using an earlier version of this
     * plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.5.6 with the restAPI plugin v1.4.0. Using the
     * restAPI plugin v1.6.0 on Openfire 4.6.1 and 4.6.7, as well as the restAPI plugin v1.7.0 on Openfire 4.7.0 (using
     * the same configuration of the roster) yields the exact same result.
     *
     * The roster definition is based on the 'jane' user as populated in a 'demoboot' server. It contains exactly one
     * roster item, "John" (john@example.org) without groups, in a two-way subscription state.
     */
    @Test
    public void getRosterXml() {
        Response response = target("restapi/v1/users/jane/roster").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><roster><rosterItem><jid>john@example.org</jid><nickname>John</nickname><subscriptionType>3</subscriptionType><groups/></rosterItem></roster>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getRosterXml()}
     *
     * The JSON-based output generated by restAPI plugin v1.4.0 on Openfire 4.6.4 has been reported to not conform to
     * this output (which was a primary motivator for this test to be implemented). The author of this test has tried,
     * but has not been able to reproduce that issue. Certain versions of the restAPI plugin contained different
     * versions of the third party Jackson libraries (used for JSON serialization), which might account for differences
     * in behavior between different deployments.
     *
     * While writing this test, it was observed that restAPI plugin v1.7.0 on Openfire 4.7.0 does not conform to any
     * of the earlier output (the property name holding group names is singular in v1.7.0, where it is pural in older
     * versions).
     *
     * @see #getRosterXml()
     * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/79">REST API issue #79</a>
     */
    @Test
    public void getChatRoomJson() {
        Response response = target("restapi/v1/users/jane/roster").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "{\"rosterItem\":[{\"jid\":\"john@example.org\",\"nickname\":\"John\",\"subscriptionType\":3,\"groups\":[]}]}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
