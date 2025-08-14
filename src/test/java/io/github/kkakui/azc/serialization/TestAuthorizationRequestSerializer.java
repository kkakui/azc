/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kkakui.azc.api.AuthorizationRequest;
import io.github.kkakui.azc.model.Action;
import io.github.kkakui.azc.model.Context;
import io.github.kkakui.azc.model.Resource;
import io.github.kkakui.azc.model.Subject;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AuthorizationRequestSerializer}. Verifies that AuthorizationRequest objects
 * are correctly serialized to JSON.
 */
public class TestAuthorizationRequestSerializer {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testSerializeRequestWithoutContext() throws Exception {
    // Given
    Subject subject =
        new Subject.Builder().id("user1").type("user").addProperty("role", "admin").build();
    Resource resource =
        new Resource.Builder()
            .id("doc1")
            .type("document")
            .addProperty("level", "confidential")
            .build();
    Action action = new Action.Builder().name("read").addProperty("scope", "internal").build();

    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .build();

    // When
    String json = AuthorizationRequestSerializer.buildRequestJson(request);
    JsonNode root = mapper.readTree(json);

    // Then
    assertFalse(root.has("context"));

    JsonNode subjectNode = root.path("subject");
    assertEquals("user1", subjectNode.path("id").asText());
    assertEquals("user", subjectNode.path("type").asText());
    assertEquals("admin", subjectNode.path("properties").path("role").asText());

    JsonNode resourceNode = root.path("resource");
    assertEquals("doc1", resourceNode.path("id").asText());
    assertEquals("document", resourceNode.path("type").asText());
    assertEquals("confidential", resourceNode.path("properties").path("level").asText());

    JsonNode actionNode = root.path("action");
    assertEquals("read", actionNode.path("name").asText());
    assertEquals("internal", actionNode.path("properties").path("scope").asText());
  }

  @Test
  public void testSerializeRequestWithContext() throws Exception {
    // Given
    Subject subject = new Subject.Builder().id("user1").type("user").build();
    Resource resource = new Resource.Builder().id("doc1").type("document").build();
    Action action = new Action.Builder().name("read").build();
    Context context = new Context(Map.of("ip_address", "192.168.1.1", "trace_id", "abc-123"));

    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .context(context)
            .build();

    // When
    String json = AuthorizationRequestSerializer.buildRequestJson(request);
    JsonNode root = mapper.readTree(json);

    // Then
    assertTrue(root.has("context"));
    JsonNode contextNode = root.path("context");
    assertEquals("192.168.1.1", contextNode.path("ip_address").asText());
    assertEquals("abc-123", contextNode.path("trace_id").asText());
  }
}
