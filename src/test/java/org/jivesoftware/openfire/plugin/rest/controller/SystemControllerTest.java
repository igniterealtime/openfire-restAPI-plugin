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
package org.jivesoftware.openfire.plugin.rest.controller;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.SystemProperty;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests for {@link SystemController}, in particular for the retrieval of an individual system property.
 *
 * These tests mock the static {@link SystemProperty} and {@link JiveGlobals} entry points, as neither is usable
 * outside of a running Openfire server.
 *
 * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242">issue #242</a>
 * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/248">issue #248</a>
 */
public class SystemControllerTest {

    private SystemController systemController;

    /**
     * Constructs a mock of the XMPPServer implementation, providing enough metadata to allow
     * {@link RESTServicePlugin}'s static {@link SystemProperty} fields to be built.
     *
     * @return A mock of a XMPPServer
     */
    private static XMPPServer constructMockXmppServer() {
        final PluginManager pluginManager = mock(PluginManager.class, withSettings().lenient());
        final XMPPServer xmppServer = mock(XMPPServer.class, withSettings().lenient());
        doAnswer(invocationOnMock -> pluginManager).when(xmppServer).getPluginManager();
        return xmppServer;
    }

    @BeforeClass
    public static void setUpClass() {
        // SystemController#getForbiddenPropertyKeys() (invoked by getSystemProperty()) references
        // RESTServicePlugin.ENABLED, triggering the one-time, JVM-wide static initialization of RESTServicePlugin.
        // That initialization builds SystemProperty instances, which require a running server. Install a mock here
        // so that this test class does not depend on some other, unrelated test class having already done so, in
        // whatever arbitrary order Surefire happens to run test classes in.
        XMPPServer.setInstance(constructMockXmppServer());

        // Force RESTServicePlugin to load now, while the mock above is in place and no test-method-scoped
        // MockedStatic block is active, rather than relying on it being lazily loaded as a side effect of
        // whichever test happens to run first.
        RESTServicePlugin.ENABLED.getPlugin();
    }

    @AfterClass
    public static void tearDownClass() {
        XMPPServer.setInstance(null);
    }

    @Before
    public void setUp() {
        // Deliberately not using SystemController.getInstance(): that singleton can have been replaced by a mock
        // controller by other (Jersey-level) tests that run in the same JVM, via SystemController#setInstance.
        systemController = new SystemController();
    }

    /**
     * A property that was registered (by Openfire, or by a plugin) using the {@link SystemProperty} API, but that
     * has never been given an explicit value, should be returned using its default value, instead of causing a 404.
     *
     * This reproduces the bug reported in issue #242, where {@code /system/properties} lists such a property (as it
     * queries the {@link SystemProperty} registry, falling back to {@link JiveGlobals}), while
     * {@code /system/properties/{propertyKey}} 404'd (as it queried {@link JiveGlobals} only).
     */
    @Test
    public void testGetSystemPropertyThatIsRegisteredButUnset() throws Exception {
        final String key = "foo.bar.xyz";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class)) {
            final SystemProperty<?> registeredProperty = mock(SystemProperty.class);
            when(registeredProperty.getValueAsSaved()).thenReturn("false");
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.of(registeredProperty));

            final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty result = systemController.getSystemProperty(key);

            assertEquals(key, result.getKey());
            assertEquals("false", result.getValue());
        }
    }

    /**
     * A property that was registered (by Openfire, or by a plugin) using the {@link SystemProperty} API, but that
     * has never been given an explicit value, should be returned using its default value, instead of causing a 404.
     * This test uses the edge case where the default value is not set (and thus is {@code null}).
     */
    @Test
    public void testGetSystemPropertyThatIsRegisteredButUnsetWithDefaultNotSet() throws Exception
    {
        final String key = "foo.bar.xyz";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class)) {
            final SystemProperty<?> registeredProperty = mock(SystemProperty.class);
            when(registeredProperty.getValueAsSaved()).thenReturn(null);
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.of(registeredProperty));

            final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty result = systemController.getSystemProperty(key);

            assertEquals(key, result.getKey());
            assertNull(result.getValue());
        }
    }

    /**
     * A property that is registered using the {@link SystemProperty} API and has an explicit value should return
     * that value.
     */
    @Test
    public void testGetSystemPropertyThatIsRegisteredAndSet() throws Exception {
        final String key = "foo.bar.xyz";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class)) {
            final SystemProperty<?> registeredProperty = mock(SystemProperty.class);
            when(registeredProperty.getValueAsSaved()).thenReturn("true");
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.of(registeredProperty));

            final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty result = systemController.getSystemProperty(key);

            assertEquals(key, result.getKey());
            assertEquals("true", result.getValue());
        }
    }

    /**
     * A property that is not registered using the {@link SystemProperty} API, but that does exist as a plain
     * {@link JiveGlobals} property, should still be returned (pre-existing behavior, unaffected by the fix for
     * issue #242).
     */
    @Test
    public void testGetSystemPropertyThatIsOnlyInJiveGlobals() throws Exception {
        final String key = "some.unregistered.property";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class)) {
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.empty());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn("bar");

            final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty result = systemController.getSystemProperty(key);

            assertEquals(key, result.getKey());
            assertEquals("bar", result.getValue());
        }
    }

    /**
     * A property that is neither registered using the {@link SystemProperty} API, nor set as a plain
     * {@link JiveGlobals} property, should still result in a 404 (pre-existing behavior, unaffected by the fix for
     * issue #242).
     */
    @Test
    public void testGetSystemPropertyThatDoesNotExist() {
        final String key = "does.not.exist";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class)) {
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.empty());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn(null);

            final ServiceException exception = assertThrows(ServiceException.class, () -> systemController.getSystemProperty(key));

            assertEquals(ExceptionType.PROPERTY_NOT_FOUND, exception.getException());
            assertEquals(Response.Status.NOT_FOUND, exception.getStatus());
        }
    }

    /**
     * When a property is registered using the {@link SystemProperty} API, its value should be used without
     * consulting {@link JiveGlobals} directly (the {@link SystemProperty} API does that internally already).
     */
    @Test
    public void testGetSystemPropertyDoesNotConsultJiveGlobalsWhenRegistered() throws Exception {
        final String key = "foo.bar.xyz";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class)) {
            final SystemProperty<?> registeredProperty = mock(SystemProperty.class);
            when(registeredProperty.getValueAsSaved()).thenReturn("false");
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.of(registeredProperty));

            systemController.getSystemProperty(key);

            jiveGlobalsMock.verify(() -> JiveGlobals.getProperty(eq(key)), never());
        }
    }

    /**
     * A property that belongs to this plugin (identified by its {@code plugin.restapi.} key prefix) must remain
     * forbidden even when it is not (yet) present in {@link SystemProperty#getProperties()} - which can happen when
     * the class that declares it (e.g. {@code MUCRoomController}) has not yet been loaded by the JVM, as
     * SystemProperty registration happens as a side effect of static initialization.
     */
    @Test
    public void testGetSystemPropertyWithRestApiPrefixIsForbiddenEvenWhenNotYetRegistered() {
        final String key = "plugin.restapi.muc.room-mutex.enabled";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class)) {
            // Simulate the property's owning class not having loaded yet: it's absent from both lookups.
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.empty());
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn("false");

            final ServiceException exception = assertThrows(ServiceException.class, () -> systemController.getSystemProperty(key));

            assertEquals(ExceptionType.NOT_ALLOWED, exception.getException());
            assertEquals(Response.Status.FORBIDDEN, exception.getStatus());
        }
    }

    /**
     * Creates a mock of a {@link SystemProperty} registration.
     *
     * @param key The key of the property
     * @param value The value of the property, as saved
     * @param encrypted Whether the property is flagged as being encrypted
     * @return A mock of a SystemProperty
     */
    private static SystemProperty<?> mockRegisteredProperty(final String key, final String value, final boolean encrypted) {
        final SystemProperty<?> registeredProperty = mock(SystemProperty.class);
        when(registeredProperty.getKey()).thenReturn(key);
        when(registeredProperty.getValueAsSaved()).thenReturn(value);
        when(registeredProperty.isEncrypted()).thenReturn(encrypted);
        when(registeredProperty.getPlugin()).thenReturn("Openfire");
        return registeredProperty;
    }

    /**
     * Verifies that retrieving a registered property that is flagged as being encrypted is forbidden.
     */
    @Test
    public void testGetSystemPropertyThatIsRegisteredAndEncryptedIsForbidden()
    {
        // Setup test fixture.
        final String key = "foo.bar.secret";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            final SystemProperty<?> registeredProperty = mockRegisteredProperty(key, "s3cr3t", true);
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.of(registeredProperty));
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.singletonList(registeredProperty));

            // Execute system under test.
            final ServiceException result = assertThrows("Expected retrieval of a registered, encrypted property to be rejected, but its value was returned.", ServiceException.class, () -> systemController.getSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected exception type when retrieving a registered, encrypted property.", ExceptionType.NOT_ALLOWED, result.getException());
            assertEquals("Unexpected HTTP status when retrieving a registered, encrypted property.", Response.Status.FORBIDDEN, result.getStatus());
        }
    }

    /**
     * Verifies that retrieving an unregistered property of which the value is stored encrypted (such as the LDAP
     * admin password) is forbidden.
     */
    @Test
    public void testGetSystemPropertyThatIsOnlyInJiveGlobalsAndEncryptedIsForbidden()
    {
        // Setup test fixture.
        final String key = "foo.bar.secret";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.empty());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn("s3cr3t");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq(key))).thenReturn(true);

            // Execute system under test.
            final ServiceException result = assertThrows("Expected retrieval of an unregistered property with an encrypted value to be rejected, but its value was returned.", ServiceException.class, () -> systemController.getSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected exception type when retrieving an unregistered property with an encrypted value.", ExceptionType.NOT_ALLOWED, result.getException());
            assertEquals("Unexpected HTTP status when retrieving an unregistered property with an encrypted value.", Response.Status.FORBIDDEN, result.getStatus());
        }
    }

    /**
     * Verifies that retrieving an unencrypted property that is considered sensitive based on its name (such as the
     * SMTP password) is forbidden.
     */
    @Test
    public void testGetSystemPropertyThatIsSensitiveIsForbidden()
    {
        // Setup test fixture.
        final String key = "mail.smtp.password";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(() -> SystemProperty.getProperty(eq(key))).thenReturn(Optional.empty());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn("s3cr3t");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertySensitive(anyString())).thenCallRealMethod();

            // Execute system under test.
            final ServiceException result = assertThrows("Expected retrieval of property '" + key + "' (which has a sensitive name) to be rejected, but its value was returned.", ServiceException.class, () -> systemController.getSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected exception type when retrieving a property with a sensitive name.", ExceptionType.NOT_ALLOWED, result.getException());
            assertEquals("Unexpected HTTP status when retrieving a property with a sensitive name.", Response.Status.FORBIDDEN, result.getStatus());
        }
    }

    /**
     * Verifies that retrieving all properties omits those that are encrypted or sensitive, while still returning others.
     */
    @Test
    public void testGetSystemPropertiesOmitsEncryptedAndSensitiveProperties()
    {
        // Setup test fixture.
        final SystemProperty<?> registeredPlain = mockRegisteredProperty("registered.plain", "a", false);
        final SystemProperty<?> registeredEncrypted = mockRegisteredProperty("registered.encrypted", "b", true);

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Arrays.asList(registeredPlain, registeredEncrypted));
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList("unregistered.plain", "unregistered.encrypted", "ldap.adminPassword"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("c");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq("unregistered.encrypted"))).thenReturn(true);
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertySensitive(anyString())).thenCallRealMethod();

            // Execute system under test.
            final List<String> result = systemController.getSystemProperties().getProperties().stream()
                .map(org.jivesoftware.openfire.plugin.rest.entity.SystemProperty::getKey)
                .collect(Collectors.toList());

            // Verify result.
            assertEquals("Expected exactly the properties that are neither encrypted nor sensitive to be returned (in order).",
                Arrays.asList("registered.plain", "unregistered.plain"), result);
        }
    }

    /**
     * Verifies that creating a property that is stored encrypted is forbidden, and does not change the stored value.
     */
    @Test
    public void testCreateEncryptedPropertyIsForbidden()
    {
        // Setup test fixture.
        final String key = "foo.bar.secret";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq(key))).thenReturn(true);

            // Execute system under test.
            final ServiceException result = assertThrows("Expected creation of an encrypted property to be rejected, but it was accepted.", ServiceException.class, () -> systemController.createSystemProperty(property));

            // Verify result.
            assertEquals("Unexpected HTTP status when creating an encrypted property.", Response.Status.FORBIDDEN, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString()), never().description("Rejected creation of an encrypted property should not have changed its stored value."));
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString(), anyBoolean()), never().description("Rejected creation of an encrypted property should not have changed its stored value."));
        }
    }

    /**
     * Verifies that creating a property is rejected when its key is not valid: when it contains characters that
     * Openfire or its database could interpret rather than store literally (such as whitespace, SQL LIKE wildcards
     * or non-ASCII characters), or when it has an empty part (such as a trailing dot, which Openfire strips).
     */
    @Test
    public void testCreatePropertyWithInvalidKeyIsRejected()
    {
        for (final String key : Arrays.asList("", ".", "foo.", ".foo", "foo..bar", "foo.bar ", " foo.bar", "foo bar", "foo%", "%", "foo\\.bar", "foo[.]bar", "pl\u00fcgin.restapi.secret"))
        {
            final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

            try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
                 final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
            {
                // Setup test fixture.
                systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());

                // Execute system under test.
                final ServiceException result = assertThrows("Expected creation of property '" + key + "' (which has an invalid key) to be rejected, but it was accepted.", ServiceException.class, () -> systemController.createSystemProperty(property));

                // Verify result.
                assertEquals("Unexpected HTTP status when creating property '" + key + "'.", Response.Status.BAD_REQUEST, result.getStatus());
                jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString()), never().description("Rejected creation of property '" + key + "' should not have stored anything."));
                jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString(), anyBoolean()), never().description("Rejected creation of property '" + key + "' should not have stored anything."));
            }
        }
    }

    /**
     * Verifies that creating a property is allowed when its key contains all characters that are allowed in a key,
     * such as the apostrophes that Openfire uses in the keys of properties for caches of MUC services.
     */
    @Test
    public void testCreatePropertyWithValidKeyIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "cache.MUCService'conference'Rooms.max_size-2";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());

            // Execute system under test.
            systemController.createSystemProperty(property);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(eq(key), eq("new")));
        }
    }

    /**
     * Verifies that updating a property that is stored encrypted is forbidden, and does not change the stored value
     * (which would otherwise also be stored without encryption).
     */
    @Test
    public void testUpdateEncryptedPropertyIsForbidden()
    {
        // Setup test fixture.
        final String key = "foo.bar.secret";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn("s3cr3t");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq(key))).thenReturn(true);

            // Execute system under test.
            final ServiceException result = assertThrows("Expected update of an encrypted property to be rejected, but it was accepted.", ServiceException.class, () -> systemController.updateSystemProperty(key, property));

            // Verify result.
            assertEquals("Unexpected HTTP status when updating an encrypted property.", Response.Status.FORBIDDEN, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString()), never().description("Rejected update of an encrypted property should not have changed its stored value."));
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString(), anyBoolean()), never().description("Rejected update of an encrypted property should not have changed its stored value."));
        }
    }

    /**
     * Verifies that creating a property is rejected when its key differs only in case from the key of an existing
     * property. Depending on the database, such keys are considered equal, so that creating the property could change
     * the value of the existing property.
     */
    @Test
    public void testCreatePropertyWithKeyDifferingOnlyInCaseIsRejected()
    {
        for (final String[] keys : Arrays.asList(new String[] {"Foo.Bar", "foo.bar"}, new String[] {"Plugin.RestAPI.Secret", "plugin.restapi.secret"}))
        {
            final String key = keys[0];
            final String existingKey = keys[1];
            final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

            try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
                 final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
            {
                // Setup test fixture.
                systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
                jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Collections.singletonList(existingKey));

                // Execute system under test.
                final ServiceException result = assertThrows("Expected creation of property '" + key + "' (which differs only in case from existing property '" + existingKey + "') to be rejected, but it was accepted.", ServiceException.class, () -> systemController.createSystemProperty(property));

                // Verify result.
                assertEquals("Unexpected HTTP status when creating property '" + key + "'.", Response.Status.CONFLICT, result.getStatus());
                jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString()), never().description("Rejected creation of property '" + key + "' should not have stored anything."));
                jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString(), anyBoolean()), never().description("Rejected creation of property '" + key + "' should not have stored anything."));
            }
        }
    }

    /**
     * Verifies that creating a property that has the exact same key as an existing property is allowed (which
     * overwrites the value of the existing property).
     */
    @Test
    public void testCreatePropertyWithKeyOfExistingPropertyIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "foo.bar";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo.bar.child", "foo.barx"));

            // Execute system under test.
            systemController.createSystemProperty(property);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(eq(key), eq("new")));
        }
    }

    /**
     * Verifies that updating a property is rejected when its key differs only in case from the key of another
     * existing property. Depending on the database, such keys are considered equal, so that updating the property
     * could change the value of the other property.
     */
    @Test
    public void testUpdatePropertyWithKeyDifferingOnlyInCaseIsRejected()
    {
        // Setup test fixture.
        final String key = "Foo.Bar";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo.bar"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected update of property '" + key + "' (which differs only in case from existing property 'foo.bar') to be rejected, but it was accepted.", ServiceException.class, () -> systemController.updateSystemProperty(key, property));

            // Verify result.
            assertEquals("Unexpected HTTP status when updating property '" + key + "'.", Response.Status.CONFLICT, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString()), never().description("Rejected update of property '" + key + "' should not have stored anything."));
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), anyString(), anyBoolean()), never().description("Rejected update of property '" + key + "' should not have stored anything."));
        }
    }

    /**
     * Verifies that updating a property is allowed when no other property has a key that differs only in case.
     */
    @Test
    public void testUpdatePropertyIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "foo.bar";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "new");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo.bar.child"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            systemController.updateSystemProperty(key, property);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(eq(key), eq("new")));
        }
    }

    /**
     * Verifies that creating a property without a value is rejected. Openfire treats setting a null value as a
     * deletion that also removes all child properties, which would allow forbidden child properties (such as this
     * plugin's own configuration) to be deleted by 'creating' their parent.
     */
    @Test
    public void testCreatePropertyWithNullValueIsRejected()
    {
        // Setup test fixture.
        final String key = "plugin";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, null);

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "plugin.restapi.secret"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected creation of a property without a value to be rejected, but it was accepted.", ServiceException.class, () -> systemController.createSystemProperty(property));

            // Verify result.
            assertEquals("Unexpected exception type when creating a property without a value.", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, result.getException());
            assertEquals("Unexpected HTTP status when creating a property without a value.", Response.Status.BAD_REQUEST, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), nullable(String.class)), never().description("Rejected creation of a property without a value should not have removed it (or its child properties)."));
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), nullable(String.class), anyBoolean()), never().description("Rejected creation of a property without a value should not have removed it (or its child properties)."));
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected creation of a property without a value should not have removed it (or its child properties)."));
        }
    }

    /**
     * Verifies that a request to create a property that does not contain a property definition is rejected as a bad
     * request (rather than causing an internal server error).
     */
    @Test
    public void testCreateWithoutPropertyIsRejected()
    {
        // Setup test fixture.
        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());

            // Execute system under test.
            final ServiceException result = assertThrows("Expected creation without a property definition to be rejected, but it was accepted.", ServiceException.class, () -> systemController.createSystemProperty(null));

            // Verify result.
            assertEquals("Unexpected exception type when creating without a property definition.", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, result.getException());
            assertEquals("Unexpected HTTP status when creating without a property definition.", Response.Status.BAD_REQUEST, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), nullable(String.class)), never().description("Rejected creation without a property definition should not have changed any property."));
        }
    }

    /**
     * Verifies that creating a property with an empty value is allowed, as (unlike a null value) that is stored as a
     * value, rather than causing the property to be deleted.
     */
    @Test
    public void testCreatePropertyWithEmptyValueIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "foo.bar";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());

            // Execute system under test.
            systemController.createSystemProperty(property);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(eq(key), eq("")), description("Expected a property with an empty value to be stored."));
        }
    }

    /**
     * Verifies that updating a property without a value is rejected. Openfire treats setting a null value as a
     * deletion that also removes all child properties, which would allow forbidden child properties (such as this
     * plugin's own configuration) to be deleted by 'updating' their parent.
     */
    @Test
    public void testUpdatePropertyWithNullValueIsRejected()
    {
        // Setup test fixture.
        final String key = "plugin";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, null);

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "plugin.restapi.secret"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected update of a property without a value to be rejected, but it was accepted.", ServiceException.class, () -> systemController.updateSystemProperty(key, property));

            // Verify result.
            assertEquals("Unexpected exception type when updating a property without a value.", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, result.getException());
            assertEquals("Unexpected HTTP status when updating a property without a value.", Response.Status.BAD_REQUEST, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), nullable(String.class)), never().description("Rejected update of a property without a value should not have removed it (or its child properties)."));
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), nullable(String.class), anyBoolean()), never().description("Rejected update of a property without a value should not have removed it (or its child properties)."));
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected update of a property without a value should not have removed it (or its child properties)."));
        }
    }

    /**
     * Verifies that a request to update a property that does not contain a property definition is rejected as a bad
     * request (rather than causing an internal server error).
     */
    @Test
    public void testUpdateWithoutPropertyIsRejected()
    {
        // Setup test fixture.
        final String key = "foo.bar";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Collections.singletonList(key));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected update without a property definition to be rejected, but it was accepted.", ServiceException.class, () -> systemController.updateSystemProperty(key, null));

            // Verify result.
            assertEquals("Unexpected exception type when updating without a property definition.", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, result.getException());
            assertEquals("Unexpected HTTP status when updating without a property definition.", Response.Status.BAD_REQUEST, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(anyString(), nullable(String.class)), never().description("Rejected update without a property definition should not have changed any property."));
        }
    }

    /**
     * Verifies that updating a property with an empty value is allowed, as (unlike a null value) that is stored as a
     * value, rather than causing the property to be deleted.
     */
    @Test
    public void testUpdatePropertyWithEmptyValueIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "foo.bar";
        final org.jivesoftware.openfire.plugin.rest.entity.SystemProperty property = new org.jivesoftware.openfire.plugin.rest.entity.SystemProperty(key, "");

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Collections.singletonList(key));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            systemController.updateSystemProperty(key, property);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.setProperty(eq(key), eq("")), description("Expected a property with an empty value to be stored."));
        }
    }

    /**
     * Verifies that deleting a property that is stored encrypted is forbidden, and does not remove it.
     */
    @Test
    public void testDeleteEncryptedPropertyIsForbidden()
    {
        // Setup test fixture.
        final String key = "foo.bar.secret";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(eq(key))).thenReturn("s3cr3t");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq(key))).thenReturn(true);

            // Execute system under test.
            final ServiceException result = assertThrows("Expected deletion of an encrypted property to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected HTTP status when deleting an encrypted property.", Response.Status.FORBIDDEN, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of an encrypted property should not have removed it."));
        }
    }

    /**
     * Verifies that deleting a property is forbidden when one of its child properties is stored encrypted, as
     * Openfire deletes all child properties along with the property that is deleted.
     */
    @Test
    public void testDeleteParentOfEncryptedPropertyIsForbidden()
    {
        // Setup test fixture.
        final String key = "foo.bar";
        final String childKey = "foo.bar.secret";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo.bar.plain", childKey));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq(childKey))).thenReturn(true);

            // Execute system under test.
            final ServiceException result = assertThrows("Expected deletion of a parent of an encrypted property to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected HTTP status when deleting a parent of an encrypted property.", Response.Status.FORBIDDEN, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of a parent of an encrypted property should not have removed anything."));
        }
    }

    /**
     * Verifies that deleting a property is forbidden when it is the parent of this plugin's own properties (which
     * would otherwise delete the configuration of this plugin), even though its key does not have the prefix of
     * those properties itself.
     */
    @Test
    public void testDeleteParentOfRestApiPropertiesIsForbidden()
    {
        // Setup test fixture.
        final String key = "plugin.restapi";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "plugin.restapi.secret"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected deletion of the parent of this plugin's properties to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected HTTP status when deleting the parent of this plugin's properties.", Response.Status.FORBIDDEN, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of the parent of this plugin's properties should not have removed anything."));
        }
    }

    /**
     * Verifies that deleting a property is allowed when none of its child properties is forbidden, and that a
     * forbidden property that merely shares a prefix with it (without being a child) does not prevent deletion.
     */
    @Test
    public void testDeleteParentOfAllowedPropertiesIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "foo.bar";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo.bar.plain", "foo.barsecret"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");
            jiveGlobalsMock.when(() -> JiveGlobals.isPropertyEncrypted(eq("foo.barsecret"))).thenReturn(true);

            // Execute system under test.
            systemController.deleteSystemProperty(key);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(eq(key)));
        }
    }

    /**
     * Verifies that deleting a property is rejected when the database statement that Openfire uses to delete it could
     * also delete a property other than the property itself and its children. That statement uses SQL LIKE without
     * escaping the key, so that the {@code _} character acts as a wildcard. Matching can also be case-insensitive,
     * depending on the database.
     */
    @Test
    public void testDeleteOfKeyThatMatchesOtherPropertyInDatabaseIsRejected()
    {
        for (final String key : Arrays.asList("foo_bar", "foo.ba_", "f_o.bar", "_oo", "Foo.Bar", "Foo.Bar.Baz"))
        {
            try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
                 final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
            {
                // Setup test fixture.
                systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
                jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo.bar.baz"));
                jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

                // Execute system under test.
                final ServiceException result = assertThrows("Expected deletion of property '" + key + "' (which the database could match to 'foo.bar.baz') to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

                // Verify result.
                assertEquals("Unexpected HTTP status when deleting property '" + key + "'.", Response.Status.CONFLICT, result.getStatus());
                jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of property '" + key + "' should not have removed anything."));
            }
        }
    }

    /**
     * Verifies that deleting a property is rejected when the database statement that Openfire uses to delete it could
     * also delete the children of a different property, of which the key differs only where the key of the deleted
     * property has an underscore.
     */
    @Test
    public void testDeleteOfKeyWithUnderscoreThatMatchesChildrenOfOtherPropertyIsRejected()
    {
        // Setup test fixture.
        final String key = "foo_bar";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "fooXbar", "fooXbar.child"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected deletion of property '" + key + "' (which the database could match to 'fooXbar.child') to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected HTTP status when deleting property '" + key + "'.", Response.Status.CONFLICT, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of property '" + key + "' should not have removed anything."));
        }
    }

    /**
     * Verifies that deleting a property is rejected when the database statement that Openfire uses to delete it could
     * also delete a forbidden property.
     */
    @Test
    public void testDeleteOfKeyThatMatchesForbiddenPropertyInDatabaseIsRejected()
    {
        // Setup test fixture.
        final String key = "plugin_restapi";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "plugin.restapi.secret"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            final ServiceException result = assertThrows("Expected deletion of property '" + key + "' (which the database could match to 'plugin.restapi.secret') to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

            // Verify result.
            assertEquals("Unexpected HTTP status when deleting property '" + key + "'.", Response.Status.CONFLICT, result.getStatus());
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of property '" + key + "' should not have removed anything."));
        }
    }

    /**
     * Verifies that deleting a property that has an underscore in its key is allowed, when the database statement
     * that Openfire uses to delete it cannot match any other property.
     */
    @Test
    public void testDeleteOfKeyWithUnderscoreIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "foo_bar";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "foo_bar.child", "plugin.restapi.secret"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            systemController.deleteSystemProperty(key);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(eq(key)));
        }
    }

    /**
     * Verifies that deleting a property is rejected when its key is not valid (see
     * {@link #testCreatePropertyWithInvalidKeyIsRejected()}).
     */
    @Test
    public void testDeletePropertyWithInvalidKeyIsRejected()
    {
        for (final String key : Arrays.asList("", ".", "foo.", ".foo", "foo..bar", "foo.bar ", " foo.bar", "foo bar", "foo%", "%", "foo\\.bar", "foo[.]bar", "pl\u00fcgin.restapi.secret"))
        {
            try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
                 final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
            {
                // Setup test fixture.
                systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
                jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Collections.singletonList(key));
                jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

                // Execute system under test.
                final ServiceException result = assertThrows("Expected deletion of property '" + key + "' (which has an invalid key) to be rejected, but it was accepted.", ServiceException.class, () -> systemController.deleteSystemProperty(key));

                // Verify result.
                assertEquals("Unexpected HTTP status when deleting property '" + key + "'.", Response.Status.BAD_REQUEST, result.getStatus());
                jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(anyString()), never().description("Rejected deletion of property '" + key + "' should not have removed anything."));
            }
        }
    }

    /**
     * Verifies that deleting a property that has apostrophes in its key (as used by Openfire in the keys of
     * properties for caches of MUC services) is allowed.
     */
    @Test
    public void testDeleteOfKeyWithApostrophesIsAllowed() throws Exception
    {
        // Setup test fixture.
        final String key = "cache.MUCService'conference'Rooms.size";

        try (final MockedStatic<SystemProperty> systemPropertyMock = mockStatic(SystemProperty.class);
             final MockedStatic<JiveGlobals> jiveGlobalsMock = mockStatic(JiveGlobals.class))
        {
            systemPropertyMock.when(SystemProperty::getProperties).thenReturn(Collections.emptyList());
            jiveGlobalsMock.when(JiveGlobals::getPropertyNames).thenReturn(Arrays.asList(key, "cache.MUCService'conference'Rooms.maxLifetime"));
            jiveGlobalsMock.when(() -> JiveGlobals.getProperty(anyString())).thenReturn("value");

            // Execute system under test.
            systemController.deleteSystemProperty(key);

            // Verify result.
            jiveGlobalsMock.verify(() -> JiveGlobals.deleteProperty(eq(key)));
        }
    }
}
