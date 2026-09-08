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

import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.SystemProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SystemController}, in particular for the retrieval of an individual system property.
 *
 * These tests mock the static {@link SystemProperty} and {@link JiveGlobals} entry points, as neither is usable
 * outside of a running Openfire server.
 *
 * @see <a href="https://github.com/igniterealtime/openfire-restAPI-plugin/issues/242">issue #242</a>
 */
public class SystemControllerTest {

    private SystemController systemController;

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
}
