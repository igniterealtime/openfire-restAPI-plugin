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
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.plugin.rest.CustomJacksonMapperProvider;
import org.jivesoftware.openfire.plugin.rest.controller.MUCRoomController;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Asserts that service endpoints in <tt>restapi/v1/chatrooms/{roomName}/members</tt> have a stable signature.
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
public class MUCRoomMembersServiceBackwardCompatibilityTest extends JerseyTest {
    /**
     * Constructs the mock of the service controller that mimics the 'business logic' normally provided by a running
     * Openfire server.
     *
     * @return A mock of a MUCRoomController
     */
    public static MUCRoomController constructMockController() throws ServiceException {
        final MUCRoomController controller = mock(MUCRoomController.class, withSettings().lenient());

        return controller;
    }

    /**
     * Constructs a mock of the XmppServer implementation, providing enough metadata to allow these tests to run.
     *
     * @return A mock of a XmppServer
     */
    public static XMPPServer constructMockXmppServer() {
        final PluginManager pluginManager = mock(PluginManager.class, withSettings().lenient());

        final XMPPServer xmppServer = mock(XMPPServer.class, withSettings().lenient());

        doAnswer(invocationOnMock -> pluginManager)
            .when(xmppServer).getPluginManager();
        return xmppServer;
    }

    @BeforeClass
    public static void setUpClass() throws ServiceException {
        // A Mock XMPP server used to mock metadata used by the test.
        XMPPServer.setInstance(constructMockXmppServer());

        // Override the service controller with a mock controller.
        MUCRoomController.setInstance(constructMockController());
    }

    @Override
    protected Application configure() {
        // Configures the Jersey web application. This should mimic JerseyWrapper's implementation.
        return new ResourceConfig(MUCRoomAffiliationsService.class, RESTExceptionMapper.class, CustomJacksonMapperProvider.class);
    }

    /**
     * Issues a request to add new chat room member, using an XML representation POST'ed to the
     * <tt>restapi/v1/chatrooms/{roomName}/members/{member}</tt> endpoint, and asserts HTTP response status indicates
     * success.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used as input was verified to cause a member to be successfully added using Openfire 4.5.6
     * with the restAPI plugin v1.4.0, as well as the restAPI plugin v1.7.0 on Openfire 4.7.0. All of these versions
     * returned the HTTP response status code '200'.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome to our lobby!</li>
     * </ul>
     *
     * Note that the entity POST'ed to this service is empty. The service operates on query parts only.
     */
    @Test
    public void addMemberXml() {
        Response response = target("restapi/v1/chatrooms/lobby/members/jane").request(MediaType.APPLICATION_XML).post(Entity.xml(""));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #addMemberXml()}, using a JID instead of a username on the URL.
     */
    @Test
    public void addMemberJidXml() {
        Response response = target("restapi/v1/chatrooms/lobby/members/john@example.org").request(MediaType.APPLICATION_XML).post(Entity.xml(""));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #addMemberXml()}, but using headers defining JSON-based interaction.
     */
    @Test
    public void addMemberJson() {
        Response response = target("restapi/v1/chatrooms/lobby/members/jane").request(MediaType.APPLICATION_JSON).post(Entity.json(""));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #addMemberJidXml()}, but using headers defining JSON-based interaction.
     */
    @Test
    public void addMemberJidJson() {
        Response response = target("restapi/v1/chatrooms/lobby/members/john@example.org").request(MediaType.APPLICATION_JSON).post(Entity.json(""));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * Issues a request remove an existing chat room member, using an DELETE request to the
     * <tt>restapi/v1/chatrooms/{roomName}/members/{member}</tt> endpoint, and asserts HTTP response status indicates
     * success.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used as input was verified to cause a member to be successfully added using Openfire 4.5.6
     * with the restAPI plugin v1.4.0, as well as the restAPI plugin v1.7.0 on Openfire 4.7.0. All of these versions
     * returned the HTTP response status code '200'.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome to our lobby!</li>
     *     <li>Permissions:
     *     <ul>
     *         <li>owners: admin@example.org</li>
     *         <li>members: jane@example.org</li>
     *     </ul></li>
     * </ul>
     */
    @Test
    public void removeMemberXml() {
        Response response = target("restapi/v1/chatrooms/lobby/members/jane").request(MediaType.APPLICATION_XML).delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #removeMemberXml()}, using a JID instead of a username on the URL.
     */
    @Test
    public void removeMemberJidXml() {
        Response response = target("restapi/v1/chatrooms/lobby/members/jane@example.org").request(MediaType.APPLICATION_XML).delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #removeMemberXml()}, but using headers defining JSON-based interaction.
     */
    @Test
    public void removeMemberJson() {
        Response response = target("restapi/v1/chatrooms/lobby/members/jane").request(MediaType.APPLICATION_JSON).delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #removeMemberJidXml()}, but using headers defining JSON-based interaction.
     */
    @Test
    public void removeMemberJidJson() {
        Response response = target("restapi/v1/chatrooms/lobby/members/jane@example.org").request(MediaType.APPLICATION_JSON).delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Issues a request to add new chat room member group, using an XML representation POST'ed to the
     * <tt>restapi/v1/chatrooms/{roomName}/members/group/{groupName}</tt> endpoint, and asserts HTTP response status
     * indicates success.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used as input was verified to cause a member to be successfully added using Openfire 4.5.6
     * with the restAPI plugin v1.4.0, as well as the restAPI plugin v1.7.0 on Openfire 4.7.0. All of these versions
     * returned the HTTP response status code '201'.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome to our lobby!</li>
     * </ul>
     *
     * A group named 'test group' was created, that contained 'john@example.org' and 'jane@example.org'.
     *
     * Note that the entity POST'ed to this service is empty. The service operates on query parts only.
     */
    @Test
    public void addMemberGroupXml() {
        Response response = target("restapi/v1/chatrooms/lobby/members/group/test group").request(MediaType.APPLICATION_XML).post(Entity.xml(""));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #addMemberGroupXml()}, but using headers defining JSON-based interaction.
     */
    @Test
    public void addMemberGroupJson() {
        Response response = target("restapi/v1/chatrooms/lobby/members/group/test group").request(MediaType.APPLICATION_XML).post(Entity.xml(""));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * Issues a request remove an existing chat room member group, using an DELETE request to the
     * <tt>restapi/v1/chatrooms/{roomName}/members/group/{groupName}</tt> endpoint, and asserts HTTP response status
     * indicates success.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used as input was verified to cause a member to be successfully added using Openfire 4.5.6
     * with the restAPI plugin v1.4.0, as well as the restAPI plugin v1.7.0 on Openfire 4.7.0. All of these versions
     * returned the HTTP response status code '200'.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * A group named 'test group' was created, that contained 'john@example.org' and 'jane@example.org'.
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome to our lobby!</li>
     *     <li>Permissions:
     *     <ul>
     *         <li>owners: admin@example.org</li>
     *         <li>members: test group@example.org</li>
     *     </ul></li>
     * </ul>
     */
    @Test
    public void removeMemberGroupXml() {
        Response response = target("restapi/v1/chatrooms/lobby/members/group/test group").request(MediaType.APPLICATION_XML).delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * An equivalent to {@link #removeMemberGroupXml()}, but using headers defining JSON-based interaction.
     */
    @Test
    public void removeMemberGroupJson() {
        Response response = target("restapi/v1/chatrooms/lobby/members/group/test group").request(MediaType.APPLICATION_JSON).delete();

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
