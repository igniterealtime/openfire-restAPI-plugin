/*
 * Copyright (c) 2026 Ignite Realtime Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.rest.service;

import org.junit.jupiter.api.Test;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JerseyWrapper#getCustomAuthFilterClassObject(String)}.
 */
public class JerseyWrapperTest
{
    /**
     * A class implementing ContainerRequestFilter and carrying the correct @Priority annotation must be accepted.
     */
    @Test
    public void testValidContainerRequestFilterIsAccepted()
    {
        // Setup test fixture.
        final String className = ValidAuthFilter.class.getName();

        // Execute system under test.
        final Class<?> result = JerseyWrapper.getCustomAuthFilterClassObject(className);

        // Verify result.
        assertEquals(ValidAuthFilter.class, result, "A correctly annotated ContainerRequestFilter should be returned as-is.");
    }

    /**
     * A class implementing Feature and carrying the correct @Priority annotation must be accepted, for backwards compatibility.
     */
    @Test
    public void testValidFeatureIsAccepted()
    {
        // Setup test fixture.
        final String className = ValidAuthFeature.class.getName();

        // Execute system under test.
        final Class<?> result = JerseyWrapper.getCustomAuthFilterClassObject(className);

        // Verify result.
        assertEquals(ValidAuthFeature.class, result, "A correctly annotated Feature should be accepted for backwards compatibility.");
    }

    /**
     * A class implementing DynamicFeature and carrying the correct @Priority annotation must be accepted, for backwards compatibility.
     */
    @Test
    public void testValidDynamicFeatureIsAccepted()
    {
        // Setup test fixture.
        final String className = ValidAuthDynamicFeature.class.getName();

        // Execute system under test.
        final Class<?> result = JerseyWrapper.getCustomAuthFilterClassObject(className);

        // Verify result.
        assertEquals(ValidAuthDynamicFeature.class, result, "A correctly annotated DynamicFeature should be accepted for backwards compatibility.");
    }

    /**
     * A ContainerRequestFilter that lacks the @Priority annotation entirely must be rejected.
     */
    @Test
    public void testUnannotatedFilterIsRejected()
    {
        // Setup test fixture.
        final String className = UnannotatedFilter.class.getName();

        // Execute system under test.
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JerseyWrapper.getCustomAuthFilterClassObject(className),
            "A ContainerRequestFilter without @Priority(AUTHENTICATION) must not be accepted as an authentication filter.");

        // Verify result.
        assertTrue(exception.getMessage().contains(className), "The exception message should identify the rejected class name.");
    }

    /**
     * A ContainerRequestFilter carrying a @Priority annotation with a value other than AUTHENTICATION must be rejected.
     */
    @Test
    public void testWronglyAnnotatedFilterIsRejected()
    {
        // Setup test fixture.
        final String className = WronglyAnnotatedFilter.class.getName();

        // Execute system under test.
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JerseyWrapper.getCustomAuthFilterClassObject(className),
            "A filter annotated with a priority other than AUTHENTICATION must not be accepted as an authentication filter.");

        // Verify result.
        assertTrue(exception.getMessage().contains(className), "The exception message should identify the rejected class name.");
    }

    /**
     * A class that implements none of the acceptable contract shapes must be rejected, even when correctly annotated.
     */
    @Test
    public void testClassWithoutAcceptableContractShapeIsRejected()
    {
        // Setup test fixture.
        final String className = NotAFilterAtAll.class.getName();

        // Execute system under test.
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JerseyWrapper.getCustomAuthFilterClassObject(className),
            "A class that is not a ContainerRequestFilter, Feature, or DynamicFeature must not be accepted, regardless of annotation.");

        // Verify result.
        assertTrue(exception.getMessage().contains(className), "The exception message should identify the rejected class name.");
    }

    /**
     * A class name that cannot be resolved on the classpath must be rejected.
     */
    @Test
    public void testNonExistentClassNameIsRejected()
    {
        // Setup test fixture.
        final String className = "org.jivesoftware.openfire.plugin.rest.service.DoesNotExist";

        // Execute system under test.
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JerseyWrapper.getCustomAuthFilterClassObject(className),
            "A class name that does not resolve on the classpath must not be accepted as an authentication filter.");

        // Verify result.
        assertTrue(exception.getMessage().contains(className), "The exception message should identify the unresolved class name.");
    }

    /**
     * An empty class name must be rejected, independent of any caller-side validation for this case.
     */
    @Test
    public void testEmptyClassNameIsRejected()
    {
        // Setup test fixture.
        final String className = "";

        // Execute system under test.
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> JerseyWrapper.getCustomAuthFilterClassObject(className),
            "An empty class name must not be accepted, even though callers are expected to filter this case out beforehand.");

        // Verify result.
        assertNotNull(exception.getMessage(), "The exception should carry an explanatory message even for an empty class name.");
    }

    @Priority(Priorities.AUTHENTICATION)
    public static class ValidAuthFilter implements ContainerRequestFilter
    {
        @Override
        public void filter(ContainerRequestContext requestContext)
        {
            // No-op: test fixture only, never invoked.
        }
    }

    public static class UnannotatedFilter implements ContainerRequestFilter
    {
        @Override
        public void filter(ContainerRequestContext requestContext)
        {
            // No-op: test fixture only, never invoked.
        }
    }

    @Priority(Priorities.AUTHORIZATION)
    public static class WronglyAnnotatedFilter implements ContainerRequestFilter
    {
        @Override
        public void filter(ContainerRequestContext requestContext)
        {
            // No-op: test fixture only, never invoked.
        }
    }

    @Priority(Priorities.AUTHENTICATION)
    public static class ValidAuthFeature implements Feature
    {
        @Override
        public boolean configure(FeatureContext context)
        {
            return true; // No-op: test fixture only, never invoked.
        }
    }

    @Priority(Priorities.AUTHENTICATION)
    public static class ValidAuthDynamicFeature implements DynamicFeature
    {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context)
        {
            // No-op: test fixture only, never invoked.
        }
    }

    @Priority(Priorities.AUTHENTICATION)
    public static class NotAFilterAtAll
    {
        // Deliberately implements none of the acceptable contract shapes.
    }
}
