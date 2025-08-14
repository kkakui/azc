/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.api;

import static org.junit.jupiter.api.Assertions.*;

import io.github.kkakui.azc.model.Action;
import io.github.kkakui.azc.model.Context;
import io.github.kkakui.azc.model.Resource;
import io.github.kkakui.azc.model.Subject;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link AuthorizationRequest} and its builder. */
public class TestAuthorizationRequest {

  @Test
  public void testBuildValidRequest() {
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Resource resource = new Resource.Builder().id("file456").type("file").build();
    Action action = new Action.Builder().name("read").build();

    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .build();

    assertNotNull(request);
    assertNull(request.getContext());
    assertEquals("user123", request.getSubject().getId());
    assertEquals("file456", request.getResource().getId());
  }

  @Test
  public void testBuildRequestWithContext() {
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Resource resource = new Resource.Builder().id("file456").type("file").build();
    Action action = new Action.Builder().name("read").build();
    Context context = new Context(Map.of("ip_address", "192.168.1.1"));

    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .context(context)
            .build();

    assertNotNull(request);
    assertNotNull(request.getContext());
    assertEquals("192.168.1.1", request.getContext().getAttributes().get("ip_address"));
  }

  @Test
  public void testWithMergedContext_whenOriginalHasNoContext() {
    // Given
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Resource resource = new Resource.Builder().id("file456").type("file").build();
    Action action = new Action.Builder().name("read").build();
    AuthorizationRequest initialRequest =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .build();

    Context newContext = new Context(Map.of("ip_address", "192.168.1.1"));

    // When
    AuthorizationRequest mergedRequest = initialRequest.withMergedContext(newContext);

    // Then
    assertNotSame(initialRequest, mergedRequest, "A new instance should be created");
    assertNull(initialRequest.getContext(), "Initial request context should remain null");
    assertNotNull(mergedRequest.getContext(), "Merged request should have a context");
    assertEquals("192.168.1.1", mergedRequest.getContext().getAttributes().get("ip_address"));
  }

  @Test
  public void testWithMergedContext_whenMergingWithNullContext() {
    // Given
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Resource resource = new Resource.Builder().id("file456").type("file").build();
    Action action = new Action.Builder().name("read").build();
    Context initialContext = new Context(Map.of("ip_address", "192.168.1.1"));
    AuthorizationRequest initialRequest =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .context(initialContext)
            .build();

    // When
    AuthorizationRequest mergedRequest = initialRequest.withMergedContext(null);

    // Then
    assertSame(
        initialRequest, mergedRequest, "The same instance should be returned for efficiency");
    assertEquals(initialContext, mergedRequest.getContext(), "Context should be unchanged");
  }

  @Test
  public void testWithMergedContext_mergingTwoContexts() {
    // Given
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Resource resource = new Resource.Builder().id("file456").type("file").build();
    Action action = new Action.Builder().name("read").build();
    Context initialContext = new Context(Map.of("ip_address", "192.168.1.1"));
    AuthorizationRequest initialRequest =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .context(initialContext)
            .build();

    Context otherContext = new Context(Map.of("timestamp", "2024-01-01T00:00:00Z"));

    // When
    AuthorizationRequest mergedRequest = initialRequest.withMergedContext(otherContext);

    // Then
    assertNotSame(initialRequest, mergedRequest, "A new instance should be created");
    assertNotNull(mergedRequest.getContext());
    Map<String, Object> mergedAttributes = mergedRequest.getContext().getAttributes();
    assertEquals("192.168.1.1", mergedAttributes.get("ip_address"));
    assertEquals("2024-01-01T00:00:00Z", mergedAttributes.get("timestamp"));
    assertEquals(2, mergedAttributes.size());
  }

  @Test
  public void testNullSubjectThrowsException() {
    Resource resource = new Resource.Builder().id("file456").type("file").build();
    Action action = new Action.Builder().name("read").build();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new AuthorizationRequest.Builder()
                  .subject(null)
                  .resource(resource)
                  .action(action)
                  .build();
            });

    assertEquals("Subject must be provided.", exception.getMessage());
  }

  @Test
  public void testNullResourceThrowsException() {
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Action action = new Action.Builder().name("read").build();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new AuthorizationRequest.Builder()
                  .subject(subject)
                  .resource(null)
                  .action(action)
                  .build();
            });

    assertEquals("Resource must be provided.", exception.getMessage());
  }

  @Test
  public void testNullActionThrowsException() {
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    Resource resource = new Resource.Builder().id("file456").type("file").build();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new AuthorizationRequest.Builder()
                  .subject(subject)
                  .resource(resource)
                  .action(null)
                  .build();
            });

    assertEquals("Action must be provided.", exception.getMessage());
  }
}
