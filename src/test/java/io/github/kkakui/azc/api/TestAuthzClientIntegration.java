/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.api;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kkakui.azc.config.AuthzClientConfig;
import io.github.kkakui.azc.context.ContextFactory;
import io.github.kkakui.azc.exception.AuthorizationException;
import io.github.kkakui.azc.exception.TransportException;
import io.github.kkakui.azc.model.Action;
import io.github.kkakui.azc.model.Context;
import io.github.kkakui.azc.model.Resource;
import io.github.kkakui.azc.model.Subject;
import io.github.kkakui.azc.transport.http.HttpTransport;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Integration-style tests for {@link AuthzClient}. These tests use mock transport and config
 * objects to verify the client's interaction with its dependencies.
 */
public class TestAuthzClientIntegration {

  static class MockHttpTransport implements HttpTransport {
    private String lastJsonBody;

    @Override
    public String request(AuthzClientConfig config, String jsonBody) throws AuthorizationException {
      this.lastJsonBody = jsonBody;
      assertEquals("https://mock-endpoint", config.getEndpoint());
      return """
                {
                  "decision": true,
                  "context": {
                    "policy": "mock-policy",
                    "trace_id": "xyz-123"
                  }
                }
            """;
    }

    public String getLastJsonBody() {
      return lastJsonBody;
    }
  }

  static class MockAuthzClientConfig implements AuthzClientConfig {
    private final String endpoint;

    public MockAuthzClientConfig(String endpoint) {
      this.endpoint = endpoint;
    }

    @Override
    public String getEndpoint() {
      return endpoint;
    }

    @Override
    public Optional<String> getApiKey() {
      return Optional.empty();
    }

    @Override
    public Optional<String> getApiKeyHeader() {
      return Optional.empty();
    }
  }

  static class StaticContextFactory implements ContextFactory {
    @Override
    public Context createContext() {
      return new Context(
          Map.of(
              "source",
              "integration-test",
              "timestamp",
              Instant.parse("2025-08-06T00:00:00Z").toString()));
    }
  }

  @Test
  public void testAuthorize_withoutContextFactory() throws AuthorizationException {
    // This test covers the AuthzClient constructor that does NOT take a ContextFactory.
    // Given
    AuthzClientConfig config = new MockAuthzClientConfig("https://mock-endpoint");
    HttpTransport mockTransport = new MockHttpTransport();
    AuthzClient client = new AuthzClient(config, mockTransport);

    Subject subject = new Subject.Builder().id("alice").type("user").build();
    Resource resource = new Resource.Builder().id("doc789").type("file").build();
    Action action = new Action.Builder().name("read").build();
    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .build();

    // When
    AuthorizationResponse response = client.authorize(request);

    // Then
    assertTrue(response.isAllowed());
    assertNotNull(response.getContext());
    assertEquals("mock-policy", response.getContext().get("policy"));
  }

  @Test
  public void testAuthorize_withContextFactory_andNoInitialContext() throws Exception {
    // This test covers the case where the request has no context, but the factory provides one.
    // Given
    AuthzClientConfig config = new MockAuthzClientConfig("https://mock-endpoint");
    ContextFactory contextFactory = new StaticContextFactory();
    MockHttpTransport mockTransport = new MockHttpTransport();
    AuthzClient client = new AuthzClient(config, mockTransport, contextFactory);

    Subject subject = new Subject.Builder().id("alice").type("user").build();
    Resource resource = new Resource.Builder().id("doc789").type("file").build();
    Action action = new Action.Builder().name("read").build();
    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .build(); // No context in the initial request

    // When
    client.authorize(request);

    // Then: Verify the factory's context was added to the JSON sent to the transport.
    String sentJson = mockTransport.getLastJsonBody();
    assertNotNull(sentJson);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode contextNode = mapper.readTree(sentJson).path("context");

    assertFalse(contextNode.isMissingNode());
    assertEquals("integration-test", contextNode.path("source").asText());
    assertEquals("2025-08-06T00:00:00Z", contextNode.path("timestamp").asText());
  }

  @Test
  public void testAuthorize_withContextFactory_andMergingContext() throws Exception {
    // This test covers the constructor WITH a ContextFactory and ensures the factory's
    // context is merged with the request's original context.
    // Given
    Subject subject =
        new Subject.Builder().id("alice").type("user").addProperty("role", "tester").build();
    Resource resource =
        new Resource.Builder()
            .id("doc789")
            .type("file")
            .addProperty("level", "confidential")
            .build();
    Action action = new Action.Builder().name("read").addProperty("context", "internal").build();

    AuthzClientConfig config = new MockAuthzClientConfig("https://mock-endpoint");
    ContextFactory contextFactory = new StaticContextFactory();
    MockHttpTransport mockTransport = new MockHttpTransport();

    AuthzClient client = new AuthzClient(config, mockTransport, contextFactory);

    // Create a request with its own initial context
    Context initialContext = new Context(Map.of("ip_address", "192.168.1.100"));
    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(subject)
            .resource(resource)
            .action(action)
            .context(initialContext)
            .build();

    // When
    AuthorizationResponse response = client.authorize(request);

    // Then
    assertTrue(response.isAllowed());
    assertNotNull(response.getContext());
    assertEquals("mock-policy", response.getContext().get("policy"));
    assertEquals("xyz-123", response.getContext().get("trace_id"));

    // And verify the context was merged correctly before sending
    String sentJson = mockTransport.getLastJsonBody();
    assertNotNull(sentJson);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode contextNode = mapper.readTree(sentJson).path("context");

    assertFalse(contextNode.isMissingNode());
    assertEquals("192.168.1.100", contextNode.path("ip_address").asText()); // from request
    assertEquals("integration-test", contextNode.path("source").asText()); // from factory
    assertEquals("2025-08-06T00:00:00Z", contextNode.path("timestamp").asText()); // from factory
  }

  static class MockFailingTransport implements HttpTransport {
    @Override
    public String request(AuthzClientConfig config, String jsonBody) throws AuthorizationException {
      throw new AuthorizationException(
          "Simulated network failure", new TransportException("Connection timed out"));
    }
  }

  @Test
  public void testAuthorize_propagatesTransportExceptionCorrectly() {
    // Given
    AuthzClientConfig config = new MockAuthzClientConfig("https://mock-endpoint");
    HttpTransport mockTransport = new MockFailingTransport();
    AuthzClient client = new AuthzClient(config, mockTransport);

    AuthorizationRequest request =
        new AuthorizationRequest.Builder()
            .subject(new Subject.Builder().id("s").type("t").build())
            .resource(new Resource.Builder().id("r").type("t").build())
            .action(new Action.Builder().name("a").build())
            .build();

    // When & Then
    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> client.authorize(request));

    // Verify that the exception is the one from the transport layer, not a newly wrapped one.
    assertEquals("Simulated network failure", exception.getMessage());

    // And verify the cause is the TransportException
    Throwable cause = exception.getCause();
    assertNotNull(
        cause, "AuthorizationException should have a cause for transport-related failures");
    assertTrue(
        cause instanceof TransportException,
        "The cause of the AuthorizationException should be a TransportException");
    assertEquals("Connection timed out", cause.getMessage());
  }
}
