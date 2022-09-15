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
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntities;
import org.jivesoftware.openfire.plugin.rest.entity.MUCRoomEntity;
import org.jivesoftware.openfire.plugin.rest.entity.OccupantEntities;
import org.jivesoftware.openfire.plugin.rest.entity.OccupantEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
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
 * Asserts that service endpoints in <tt>restapi/v1/chatrooms/{roomName}</tt> have a stable signature.
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
public class MUCRoomServiceBackwardCompatibilityTest extends JerseyTest {

    private TimeZone defaultTimeZone;

    /**
     * Constructs the mock of the service controller that mimics the 'business logic' normally provided by a running
     * Openfire server.
     *
     * @return A mock of a MUCRoomController
     */
    public static MUCRoomController constructMockController() throws ServiceException {
        final MUCRoomController controller = mock(MUCRoomController.class, withSettings().lenient());

        final MUCRoomEntity entity = new MUCRoomEntity();
        entity.setRoomName("lobby");
        entity.setNaturalName("Lobby");
        entity.setDescription("Welcome in our lobby!");
        entity.setSubject("Introduction to XMPP");
        entity.setCreationDate(Date.from(ZonedDateTime.parse("2022-02-07T16:09:51.517+01:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")).toInstant()));
        entity.setModificationDate(Date.from(ZonedDateTime.parse("2022-02-07T16:09:51.538+01:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")).toInstant()));
        entity.setMaxUsers(30);
        entity.setPersistent(true);
        entity.setPublicRoom(true);
        entity.setRegistrationEnabled(true);
        entity.setCanAnyoneDiscoverJID(true);
        entity.setCanOccupantsChangeSubject(false);
        entity.setCanOccupantsInvite(false);
        entity.setCanChangeNickname(true);
        entity.setLogEnabled(true);
        entity.setLoginRestrictedToNickname(false);
        entity.setMembersOnly(false);
        entity.setModerated(false);
        entity.setAllowPM("anyone");
        entity.setBroadcastPresenceRoles(Arrays.asList("moderator", "participant", "visitor"));
        entity.setOwners(Collections.singletonList("admin@example.org"));
        entity.setAdmins(Arrays.asList("john@example.org", "jane@example.org"));
        entity.setMembers(Collections.emptyList());
        entity.setOutcasts(Collections.emptyList());
        entity.setAdminGroups(Collections.emptyList());
        entity.setOwnerGroups(Collections.emptyList());
        entity.setMemberGroups(Collections.emptyList());
        entity.setOutcastGroups(Collections.emptyList());

        doAnswer(invocationOnMock -> new MUCRoomEntities(Collections.singletonList(entity)))
            .when(controller).getChatRooms(any(), any(), any(), nullable(Boolean.class));

        doAnswer(invocationOnMock -> entity)
            .when(controller).getChatRoom(any(), any(), nullable(Boolean.class));

        final OccupantEntity jane = new OccupantEntity();
        jane.setJid("lobby@conference.example.org/jane");
        jane.setUserAddress("jane@example.org/converse.js-131754909");
        jane.setRole("participant");
        jane.setAffiliation("member");

        final OccupantEntity john = new OccupantEntity();
        john.setJid("lobby@conference.example.org/John");
        john.setUserAddress("john@example.org/converse.js-57890634");
        john.setRole("participant");
        john.setAffiliation("none");

        final List<OccupantEntity> occupantEntities = new ArrayList<>();
        occupantEntities.add(jane);
        occupantEntities.add(john);

        doAnswer(invocation -> new OccupantEntities(occupantEntities))
            .when(controller).getRoomOccupants(any(), any());
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
        return new ResourceConfig(MUCRoomService.class, RESTExceptionMapper.class, CustomJacksonMapperProvider.class);
    }

    /**
     * Retrieves an XML representation of a single chat room from the <tt>restapi/v1/chatrooms/{roomName}</tt> endpoint,
     * and asserts that representation is equal to a representation that was recorded using an earlier version of this
     * plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.7.2 with the restAPI plugin v1.9.0. This
     * value has been observed to be equal with one exception to that obtained from (using the same configuration of the
     * room):
     *
     * <ul>
     *  <li>Openfire 4.5.6 with the restAPI plugin v1.4.0</li>
     *  <li>restAPI plugin v1.6.0 on Openfire 4.6.1 and 4.6.7</li>
     *  <li>restAPI plugin v1.7.0 on Openfire 4.7.0</li>
     * </ul>,
     *
     * The exception being the field 'allowPM', which has been added in version v1.9.0 of the restAPI plugin.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome in our lobby!</li>
     *     <li>Topic: Introduction to XMPP</li>
     *     <li>Permissions:
     *     <ul>
     *         <li>owners: admin@example.org</li>
     *         <li>admins: jane@example.org, john@example.org</li>
     *     </ul></li>
     * </ul>
     */
    @Test
    public void getChatRoomXml() {
        Response response = target("restapi/v1/chatrooms/lobby").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><chatRoom><roomName>lobby</roomName><naturalName>Lobby</naturalName><description>Welcome in our lobby!</description><subject>Introduction to XMPP</subject><creationDate>2022-02-07T16:09:51.517+01:00</creationDate><modificationDate>2022-02-07T16:09:51.538+01:00</modificationDate><maxUsers>30</maxUsers><persistent>true</persistent><publicRoom>true</publicRoom><registrationEnabled>true</registrationEnabled><canAnyoneDiscoverJID>true</canAnyoneDiscoverJID><canOccupantsChangeSubject>false</canOccupantsChangeSubject><canOccupantsInvite>false</canOccupantsInvite><canChangeNickname>true</canChangeNickname><logEnabled>true</logEnabled><loginRestrictedToNickname>false</loginRestrictedToNickname><membersOnly>false</membersOnly><moderated>false</moderated><broadcastPresenceRoles><broadcastPresenceRole>moderator</broadcastPresenceRole><broadcastPresenceRole>participant</broadcastPresenceRole><broadcastPresenceRole>visitor</broadcastPresenceRole></broadcastPresenceRoles><owners><owner>admin@example.org</owner></owners><admins><admin>john@example.org</admin><admin>jane@example.org</admin></admins><members/><outcasts/><ownerGroups/><adminGroups/><memberGroups/><outcastGroups/><allowPM>anyone</allowPM></chatRoom>", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getChatRoomXml()}
     *
     * The JSON-based output generated by restAPI plugin v1.7.0 on Openfire 4.7.0 is known to not conform to this output
     * (which was a primary motivator for this test to be implemented).
     *
     * @see #getChatRoomXml()
     * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/88">REST API issue #88</a>
     */
    @Test
    public void getChatRoomJson() {
        Response response = target("restapi/v1/chatrooms/lobby").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response should match that generated by older versions of this plugin.", "{\"roomName\":\"lobby\",\"naturalName\":\"Lobby\",\"description\":\"Welcome in our lobby!\",\"subject\":\"Introduction to XMPP\",\"creationDate\":1644246591517,\"modificationDate\":1644246591538,\"maxUsers\":30,\"persistent\":true,\"publicRoom\":true,\"registrationEnabled\":true,\"canAnyoneDiscoverJID\":true,\"canOccupantsChangeSubject\":false,\"canOccupantsInvite\":false,\"canChangeNickname\":true,\"logEnabled\":true,\"loginRestrictedToNickname\":false,\"membersOnly\":false,\"moderated\":false,\"broadcastPresenceRoles\":[\"moderator\",\"participant\",\"visitor\"],\"owners\":[\"admin@example.org\"],\"admins\":[\"john@example.org\",\"jane@example.org\"],\"members\":[],\"outcasts\":[],\"ownerGroups\":[],\"adminGroups\":[],\"memberGroups\":[],\"outcastGroups\":[],\"allowPM\":\"anyone\"}", content);
        assertEquals("HTTP response should have a status code that is 200.", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Retrieves an XML representation of all chat rooms from the <tt>restapi/v1/chatrooms/</tt> endpoint, and asserts
     * that representation is equal to a representation that was recorded using an earlier version of this plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.7.2 with the restAPI plugin v1.9.0. This
     * value has been observed to be equal with one exception to that obtained from (using the same configuration of the
     * room):
     *
     * <ul>
     *  <li>Openfire 4.5.6 with the restAPI plugin v1.4.0</li>
     *  <li>restAPI plugin v1.6.0 on Openfire 4.6.1 and 4.6.7</li>
     *  <li>restAPI plugin v1.7.0 on Openfire 4.7.0</li>
     * </ul>,
     *
     * The exception being the field 'allowPM', which has been added in version v1.9.0 of the restAPI plugin.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome in our lobby!</li>
     *     <li>Topic: Introduction to XMPP</li>
     *     <li>Permissions:
     *     <ul>
     *         <li>owners: admin@example.org</li>
     *         <li>admins: jane@example.org, john@example.org</li>
     *     </ul></li>
     * </ul>
     */
    @Test
    public void getChatRoomsXml() {
        Response response = target("restapi/v1/chatrooms").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response is: ", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><chatRooms><chatRoom><roomName>lobby</roomName><naturalName>Lobby</naturalName><description>Welcome in our lobby!</description><subject>Introduction to XMPP</subject><creationDate>2022-02-07T16:09:51.517+01:00</creationDate><modificationDate>2022-02-07T16:09:51.538+01:00</modificationDate><maxUsers>30</maxUsers><persistent>true</persistent><publicRoom>true</publicRoom><registrationEnabled>true</registrationEnabled><canAnyoneDiscoverJID>true</canAnyoneDiscoverJID><canOccupantsChangeSubject>false</canOccupantsChangeSubject><canOccupantsInvite>false</canOccupantsInvite><canChangeNickname>true</canChangeNickname><logEnabled>true</logEnabled><loginRestrictedToNickname>false</loginRestrictedToNickname><membersOnly>false</membersOnly><moderated>false</moderated><broadcastPresenceRoles><broadcastPresenceRole>moderator</broadcastPresenceRole><broadcastPresenceRole>participant</broadcastPresenceRole><broadcastPresenceRole>visitor</broadcastPresenceRole></broadcastPresenceRoles><owners><owner>admin@example.org</owner></owners><admins><admin>john@example.org</admin><admin>jane@example.org</admin></admins><members/><outcasts/><ownerGroups/><adminGroups/><memberGroups/><outcastGroups/><allowPM>anyone</allowPM></chatRoom></chatRooms>", content);
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getChatRoomsXml()}
     *
     * The JSON-based output generated by restAPI plugin v1.7.0 on Openfire 4.7.0 is known to not conform to this output
     * (which was a primary motivator for this test to be implemented).
     *
     * @see #getChatRoomsXml()
     * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/88">REST API issue #88</a>
     */
    @Test
    public void getChatRoomsJson() {
        Response response = target("restapi/v1/chatrooms").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response is: ", "{\"chatRooms\":[{\"roomName\":\"lobby\",\"naturalName\":\"Lobby\",\"description\":\"Welcome in our lobby!\",\"subject\":\"Introduction to XMPP\",\"creationDate\":1644246591517,\"modificationDate\":1644246591538,\"maxUsers\":30,\"persistent\":true,\"publicRoom\":true,\"registrationEnabled\":true,\"canAnyoneDiscoverJID\":true,\"canOccupantsChangeSubject\":false,\"canOccupantsInvite\":false,\"canChangeNickname\":true,\"logEnabled\":true,\"loginRestrictedToNickname\":false,\"membersOnly\":false,\"moderated\":false,\"broadcastPresenceRoles\":[\"moderator\",\"participant\",\"visitor\"],\"owners\":[\"admin@example.org\"],\"admins\":[\"john@example.org\",\"jane@example.org\"],\"members\":[],\"outcasts\":[],\"ownerGroups\":[],\"adminGroups\":[],\"memberGroups\":[],\"outcastGroups\":[],\"allowPM\":\"anyone\"}]}", content);
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Issues a request to create a new chat room, using an XML representation POST'ed to the
     * <tt>restapi/v1/chatrooms/</tt> endpoint, and asserts HTTP response status indicates success.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used as input was verified to cause a chat room to be successfully created using Openfire 4.5.6
     * with the restAPI plugin v1.4.0, the restAPI plugin v1.6.0 on Openfire 4.6.1 and 4.6.7, as well as the restAPI
     * plugin v1.7.0 on Openfire 4.7.0. All of these versions returned the HTTP response status code '201'.
     */
    @Test
    public void createChatRoomXml() {
        Response response = target("restapi/v1/chatrooms").request(MediaType.APPLICATION_XML).post(Entity.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><chatRoom><roomName>lobby</roomName><naturalName>Lobby</naturalName><description>Welcome in our lobby!</description><subject>Introduction to XMPP</subject><maxUsers>30</maxUsers><persistent>true</persistent><publicRoom>true</publicRoom><registrationEnabled>true</registrationEnabled><canAnyoneDiscoverJID>true</canAnyoneDiscoverJID><canOccupantsChangeSubject>false</canOccupantsChangeSubject><canOccupantsInvite>false</canOccupantsInvite><canChangeNickname>true</canChangeNickname><logEnabled>true</logEnabled><loginRestrictedToNickname>false</loginRestrictedToNickname><membersOnly>false</membersOnly><moderated>false</moderated><broadcastPresenceRoles><broadcastPresenceRole>moderator</broadcastPresenceRole><broadcastPresenceRole>participant</broadcastPresenceRole><broadcastPresenceRole>visitor</broadcastPresenceRole></broadcastPresenceRoles><owners><owner>admin@example.org</owner></owners><admins><admin>john@example.org</admin><admin>jane@example.org</admin></admins><members/><outcasts/><ownerGroups/><adminGroups/><memberGroups/><outcastGroups/></chatRoom>"));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #createChatRoomXml()}
     *
     * The restAPI plugin v1.7.0 on Openfire 4.7.0 is known to not accept the value that is provided as input, although
     * this value is accepted by older versions. This was a primary motivator for this test to be implemented.
     *
     * @see #createChatRoomXml()
     * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/88">REST API issue #88</a>
     */
    @Test
    public void createChatRoomJson() {
        Response response = target("restapi/v1/chatrooms").request(MediaType.APPLICATION_XML).post(Entity.json("{\"roomName\":\"lobby\",\"naturalName\":\"Lobby\",\"description\":\"Welcome in our lobby!\",\"subject\":\"Introduction to XMPP\",\"maxUsers\":30,\"persistent\":true,\"publicRoom\":true,\"registrationEnabled\":true,\"canAnyoneDiscoverJID\":true,\"canOccupantsChangeSubject\":false,\"canOccupantsInvite\":false,\"canChangeNickname\":true,\"logEnabled\":true,\"loginRestrictedToNickname\":false,\"membersOnly\":false,\"moderated\":false,\"broadcastPresenceRoles\":[\"moderator\",\"participant\",\"visitor\"],\"owners\":[\"admin@example.org\"],\"admins\":[\"john@example.org\",\"jane@example.org\"],\"members\":[],\"outcasts\":[],\"ownerGroups\":[],\"adminGroups\":[],\"memberGroups\":[],\"outcastGroups\":[]}"));

        assertEquals("Http Response should be 201: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    /**
     * Issues a request to create a new chat room, using an XML representation PUT to the
     * <tt>restapi/v1/chatrooms/{roomName}</tt> endpoint, and asserts HTTP response status indicates success.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used as input was verified to cause a chat room to be successfully created using Openfire 4.5.6
     * with the restAPI plugin v1.4.0, the restAPI plugin v1.6.0 on Openfire 4.6.1 and 4.6.7, as well as the restAPI
     * plugin v1.7.0 on Openfire 4.7.0. All of these versions returned the HTTP response status code '200'.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: This will be changed!</li>
     * </ul>
     */
    @Test
    public void updateChatRoomXml() {
        Response response = target("restapi/v1/chatrooms/lobby").request(MediaType.APPLICATION_XML).put(Entity.xml("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><chatRoom><roomName>lobby</roomName><naturalName>Lobby</naturalName><description>Welcome in our lobby!</description><subject>Introduction to XMPP</subject><maxUsers>30</maxUsers><persistent>true</persistent><publicRoom>true</publicRoom><registrationEnabled>true</registrationEnabled><canAnyoneDiscoverJID>true</canAnyoneDiscoverJID><canOccupantsChangeSubject>false</canOccupantsChangeSubject><canOccupantsInvite>false</canOccupantsInvite><canChangeNickname>true</canChangeNickname><logEnabled>true</logEnabled><loginRestrictedToNickname>false</loginRestrictedToNickname><membersOnly>false</membersOnly><moderated>false</moderated><broadcastPresenceRoles><broadcastPresenceRole>moderator</broadcastPresenceRole><broadcastPresenceRole>participant</broadcastPresenceRole><broadcastPresenceRole>visitor</broadcastPresenceRole></broadcastPresenceRoles><owners><owner>admin@example.org</owner></owners><admins><admin>john@example.org</admin><admin>jane@example.org</admin></admins><members/><outcasts/><ownerGroups/><adminGroups/><memberGroups/><outcastGroups/></chatRoom>"));

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #updateChatRoomXml()}
     *
     * The restAPI plugin v1.7.0 on Openfire 4.7.0 is known to not accept the value that is provided as input, although
     * this value is accepted by older versions. This was a primary motivator for this test to be implemented.
     *
     * @see #updateChatRoomXml()
     * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/88">REST API issue #88</a>
     */
    @Test
    public void updateChatRoomJson() {
        Response response = target("restapi/v1/chatrooms/lobby").request(MediaType.APPLICATION_XML).put(Entity.json("{\"roomName\":\"lobby\",\"naturalName\":\"Lobby\",\"description\":\"Welcome in our lobby!\",\"subject\":\"Introduction to XMPP\",\"maxUsers\":30,\"persistent\":true,\"publicRoom\":true,\"registrationEnabled\":true,\"canAnyoneDiscoverJID\":true,\"canOccupantsChangeSubject\":false,\"canOccupantsInvite\":false,\"canChangeNickname\":true,\"logEnabled\":true,\"loginRestrictedToNickname\":false,\"membersOnly\":false,\"moderated\":false,\"broadcastPresenceRoles\":[\"moderator\",\"participant\",\"visitor\"],\"owners\":[\"admin@example.org\"],\"admins\":[\"john@example.org\",\"jane@example.org\"],\"members\":[],\"outcasts\":[],\"ownerGroups\":[],\"adminGroups\":[],\"memberGroups\":[],\"outcastGroups\":[]}"));

        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Retrieves an XML representation of all occupants in a chat room from the
     * <tt>restapi/v1/chatrooms/{roomName}/occupants</tt> endpoint, and asserts that representation is equal to a
     * representation that was recorded using an earlier version of this plugin.
     *
     * The purpose of this test is to ensure that future versions of this plugin return a value that is compatible with
     * earlier versions of this plugin.
     *
     * The value that is used for comparison was obtained using Openfire 4.5.6 with the restAPI plugin v1.4.0, as well
     * as the restAPI plugin v1.7.0 on Openfire 4.7.0 (using the same configuration of the room) yields the exact same
     * result.
     *
     * The room configuration was based on a 'demoboot' server start in which a new MUC room is created, using these
     * values (leaving everything else on default):
     *
     * <ul>
     *     <li>Room ID: lobby</li>
     *     <li>Room Name: Lobby</li>
     *     <li>Description: Welcome in our lobby!</li>
     *     <li>Topic: Introduction to XMPP</li>
     *     <li>Permissions:
     *     <ul>
     *         <li>owners: admin@example.org</li>
     *         <li>members: jane@example.org</li>
     *     </ul></li>
     * </ul>
     *
     * Two clients were used to have users jane (using the nickname 'jane') and users 'john' (using the nickname "John")
     * join the room as occupants.
     */
    @Test
    public void getOccupantsXml()
    {
        Response response = target("restapi/v1/chatrooms/lobby/occupants").request(MediaType.APPLICATION_XML).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response is: ", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><occupants><occupant><affiliation>member</affiliation><jid>lobby@conference.example.org/jane</jid><role>participant</role><userAddress>jane@example.org/converse.js-131754909</userAddress></occupant><occupant><affiliation>none</affiliation><jid>lobby@conference.example.org/John</jid><role>participant</role><userAddress>john@example.org/converse.js-57890634</userAddress></occupant></occupants>", content);
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * The JSON-based equivalent of {@link #getOccupantsXml()}
     *
     * The JSON-based output generated by restAPI plugin v1.7.0 on Openfire 4.7.0 is known to not conform to this output
     * (which was a primary motivator for this test to be implemented).
     *
     * @see #getOccupantsXml()
     * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/88">REST API issue #88</a>
     */
    @Test
    public void getOccupantsJson()
    {
        Response response = target("restapi/v1/chatrooms/lobby/occupants").request(MediaType.APPLICATION_JSON).get();

        String content = response.readEntity(String.class);
        assertEquals("Content of response is: ", "{\"occupants\":[{\"jid\":\"lobby@conference.example.org/jane\",\"userAddress\":\"jane@example.org/converse.js-131754909\",\"role\":\"participant\",\"affiliation\":\"member\"},{\"jid\":\"lobby@conference.example.org/John\",\"userAddress\":\"john@example.org/converse.js-57890634\",\"role\":\"participant\",\"affiliation\":\"none\"}]}", content);
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
