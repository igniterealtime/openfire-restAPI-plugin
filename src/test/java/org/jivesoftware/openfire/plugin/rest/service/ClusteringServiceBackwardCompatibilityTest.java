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
import org.jivesoftware.openfire.plugin.rest.controller.ClusteringController;
import org.jivesoftware.openfire.plugin.rest.controller.UserServiceController;
import org.jivesoftware.openfire.plugin.rest.entity.ClusterNodeEntities;
import org.jivesoftware.openfire.plugin.rest.entity.ClusterNodeEntity;
import org.jivesoftware.openfire.plugin.rest.entity.RosterEntities;
import org.jivesoftware.openfire.plugin.rest.entity.RosterItemEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.roster.RosterItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Asserts that service endpoints in <tt>restapi/v1/clustering</tt> have a stable signature.
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
public class ClusteringServiceBackwardCompatibilityTest extends JerseyTest {

    private TimeZone defaultTimeZone;

    /**
     * Constructs the mock of the service controller that mimics the 'business logic' normally provided by a running
     * Openfire server.
     *
     * @return A mock of a ClusteringController
     */
    public static ClusteringController constructMockController() throws ServiceException {
        final ClusteringController controller = mock(ClusteringController.class, withSettings().lenient());

        final ClusterNodeEntity one = new ClusterNodeEntity();
        one.setHostName("node1.example.org (198.51.100.1)");
        one.setNodeID("52a89928-1111-1111-1111-096de07400ac");
        one.setJoinedTime(Date.from(ZonedDateTime.parse("2022-02-07T16:09:51.517+01:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")).toInstant()));
        one.setSeniorMember(false);

        final ClusterNodeEntity two = new ClusterNodeEntity();
        two.setHostName("node2.example.org (198.51.100.2)");
        two.setNodeID("52a89928-2222-2222-2222-096de07400ac");
        two.setJoinedTime(Date.from(ZonedDateTime.parse("2022-01-04T01:41:01.924+01:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")).toInstant()));
        two.setSeniorMember(true);

        doAnswer(invocationOnMock -> new ClusterNodeEntities(Arrays.asList(one, two)) )
            .when(controller).getNodeEntities();
        doAnswer(invocationOnMock -> Optional.of(one) )
            .when(controller).getNodeEntity(eq(one.getNodeID()));
        doAnswer(invocationOnMock -> Optional.of(two) )
            .when(controller).getNodeEntity(eq(two.getNodeID()));
        doAnswer(invocationOnMock -> "SENIOR AND ONLY MEMBER")
            .when(controller).getClusterStatus();
        return controller;
    }

    @BeforeClass
    public static void setUpClass() throws ServiceException {
        // Override the service controller with a mock controller.
        ClusteringController.setInstance(constructMockController());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // XML timestamps will be local to the server that's running the test. Correct the local timezone to match the
        // timezone in which the expected result was recorded, for the duration of the test.
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        // Reset the default time zone to what it was prior to the test.
        TimeZone.setDefault(defaultTimeZone);
    }

    @Override
    protected Application configure() {
        // Configures the Jersey web application. This should mimic JerseyWrapper's implementation.
        return new ResourceConfig(ClusteringService.class, RESTExceptionMapper.class, CustomJacksonMapperProvider.class);
    }

    /**
     * Retrieves an XML representation of the clustering status from the <tt>restapi/v1/clustering/status</tt> endpoint,
     * and asserts that representation is equal to a representation that was recorded using an earlier version of this
     * plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.7.1 with the restAPI plugin v1.7.2.
     */
    @Test
    public void getStatusXml() {
        Response response = target("restapi/v1/clustering/status").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><clustering><status>SENIOR AND ONLY MEMBER</status></clustering>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getStatusXml()}
     */
    @Test
    public void getStatusJson() {
        Response response = target("restapi/v1/clustering/status").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "{\"status\":\"SENIOR AND ONLY MEMBER\"}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Retrieves an XML representation of the collection of all cluster nodes from the <tt>restapi/v1/clustering/nodes</tt>
     * endpoint, and asserts that representation is equal to a representation that was recorded using an earlier version
     * of this plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.7.1 with the restAPI plugin v1.7.2.
     */
    @Test
    public void getNodesXml() {
        Response response = target("restapi/v1/clustering/nodes").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><clusterNodes><clusterNode><hostName>node1.example.org (198.51.100.1)</hostName><joinedTime>2022-02-07T16:09:51.517+01:00</joinedTime><nodeID>52a89928-1111-1111-1111-096de07400ac</nodeID><seniorMember>false</seniorMember></clusterNode><clusterNode><hostName>node2.example.org (198.51.100.2)</hostName><joinedTime>2022-01-04T01:41:01.924+01:00</joinedTime><nodeID>52a89928-2222-2222-2222-096de07400ac</nodeID><seniorMember>true</seniorMember></clusterNode></clusterNodes>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getNodesXml()}
     */
    @Test
    public void getNodesJson() {
        Response response = target("restapi/v1/clustering/nodes").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "{\"clusterNodes\":[{\"hostName\":\"node1.example.org (198.51.100.1)\",\"nodeID\":\"52a89928-1111-1111-1111-096de07400ac\",\"joinedTime\":1644246591517,\"seniorMember\":false},{\"hostName\":\"node2.example.org (198.51.100.2)\",\"nodeID\":\"52a89928-2222-2222-2222-096de07400ac\",\"joinedTime\":1641256861924,\"seniorMember\":true}]}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Retrieves an XML representation of the collection of a specific cluster node from the
     * <tt>restapi/v1/clustering/nodes/{nodeID}</tt> endpoint, and asserts that representation is equal to a
     * representation that was recorded using an earlier version of this plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.7.1 with the restAPI plugin v1.7.2.
     */
    @Test
    public void getNodeXml() {
        Response response = target("restapi/v1/clustering/nodes/52a89928-2222-2222-2222-096de07400ac").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><clusterNode><hostName>node2.example.org (198.51.100.2)</hostName><joinedTime>2022-01-04T01:41:01.924+01:00</joinedTime><nodeID>52a89928-2222-2222-2222-096de07400ac</nodeID><seniorMember>true</seniorMember></clusterNode>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getNodesXml()}
     */
    @Test
    public void getNodeJson() {
        Response response = target("restapi/v1/clustering/nodes/52a89928-2222-2222-2222-096de07400ac").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "{\"hostName\":\"node2.example.org (198.51.100.2)\",\"nodeID\":\"52a89928-2222-2222-2222-096de07400ac\",\"joinedTime\":1641256861924,\"seniorMember\":true}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
