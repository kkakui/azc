/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.transport.http;

import static org.junit.jupiter.api.Assertions.*;

import io.github.kkakui.azc.config.AuthzClientConfig;
import io.github.kkakui.azc.config.DefaultAuthzClientConfig;
import io.github.kkakui.azc.exception.AuthorizationException;
import io.github.kkakui.azc.exception.TransportException;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SimpleHttpClient}. */
public class TestSimpleHttpClient {

  private MockWebServer mockWebServer;
  private SimpleHttpClient client;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  public void testRequestSucceedsOnFirstAttempt() throws Exception {
    // Given
    client = new SimpleHttpClient(Duration.ofSeconds(1), 3);
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"decision\":true}"));

    String endpoint = mockWebServer.url("/").toString();
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("test-key")
            .apiKeyHeader("Authorization")
            .build();

    // When
    String response = client.request(config, "{}");

    // Then
    assertEquals("{\"decision\":true}", response);
    assertEquals(1, mockWebServer.getRequestCount());
  }

  @Test
  public void testRetriesOnServerErrorsAndSucceeds() throws Exception {
    // Given
    int maxRetries = 3;
    client =
        new SimpleHttpClient(Duration.ofMillis(100), maxRetries); // Short duration for faster test

    // Simulate two 500 errors then one success
    mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
    mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"decision\":true}"));

    String endpoint = mockWebServer.url("/").toString();
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("test-key")
            .apiKeyHeader("Authorization")
            .build();

    // When
    String response = client.request(config, "{}");

    // Then
    assertEquals("{\"decision\":true}", response);
    assertEquals(3, mockWebServer.getRequestCount()); // 1 initial + 2 retries
  }

  @Test
  public void testFailsAfterMaxRetriesOnNetworkError() {
    // Given
    int maxRetries = 2;
    client = new SimpleHttpClient(Duration.ofMillis(100), maxRetries);

    // Simulate failures that exceed the retry limit by disconnecting the socket
    for (int i = 0; i <= maxRetries; i++) {
      mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
    }

    String endpoint = mockWebServer.url("/").toString();
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("test-key")
            .apiKeyHeader("Authorization")
            .build();

    // When & Then
    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> client.request(config, "{}"));

    // Then: Verify the cause is a TransportException, which in turn is caused by an IOException
    Throwable cause = exception.getCause();
    assertNotNull(cause, "AuthorizationException should have a cause for transport errors");
    assertTrue(cause instanceof TransportException, "The cause should be a TransportException");
    assertNotNull(cause.getCause(), "TransportException should have a root cause");
    assertTrue(cause.getCause() instanceof IOException, "The root cause should be an IOException");

    assertEquals(maxRetries + 1, mockWebServer.getRequestCount());
  }

  @Test
  public void testFailsImmediatelyOnClientError() throws Exception {
    // Given
    client = new SimpleHttpClient(Duration.ofSeconds(1), 3); // 3 retries configured
    mockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(403) // Client error
            .setBody("Forbidden"));

    String endpoint = mockWebServer.url("/").toString();
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("test-key")
            .apiKeyHeader("Authorization")
            .build();

    // When & Then
    AuthorizationException exception =
        assertThrows(AuthorizationException.class, () -> client.request(config, "{}"));

    // Then
    assertTrue(exception.getMessage().startsWith("HTTP request failed with status 403"));
    assertEquals(1, mockWebServer.getRequestCount(), "Should not retry on client errors");
  }

  @Test
  public void testSendsRequestIdHeader() throws Exception {
    // Given
    client = new SimpleHttpClient(Duration.ofSeconds(1), 0);
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    String endpoint = mockWebServer.url("/").toString();
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("test-key")
            .apiKeyHeader("Authorization")
            .build();

    // When
    client.request(config, "{}");

    // Then
    assertEquals(1, mockWebServer.getRequestCount());
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    String requestId = recordedRequest.getHeader("X-Request-ID");

    assertNotNull(requestId, "X-Request-ID header should be present");
    assertFalse(requestId.isBlank(), "X-Request-ID header should not be blank");
    // Verify it's a valid UUID, as per our implementation.
    assertDoesNotThrow(() -> UUID.fromString(requestId));
  }

  @Test
  public void testBackoffDelayOccursDuringRetries() {
    // Given
    int maxRetries = 2;
    // Use a short timeout for the client itself to make requests fail fast
    client = new SimpleHttpClient(Duration.ofMillis(100), maxRetries);

    // Enqueue failures to trigger all retries
    for (int i = 0; i <= maxRetries; i++) {
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));
    }

    String endpoint = mockWebServer.url("/").toString();
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("test-key")
            .apiKeyHeader("Authorization")
            .build();

    // When
    long startTime = System.nanoTime();
    assertThrows(AuthorizationException.class, () -> client.request(config, "{}"));
    long durationMillis = Duration.ofNanos(System.nanoTime() - startTime).toMillis();

    // Then
    assertEquals(
        maxRetries + 1, mockWebServer.getRequestCount(), "All retries should be attempted");

    // Verify that a delay occurred. The first retry sleeps up to 500ms, the second up to 1000ms.
    // This assertion confirms that the Thread.sleep() logic is being executed.
    assertTrue(
        durationMillis > 100,
        "The total request time should reflect the backoff delay. "
            + "Actual: "
            + durationMillis
            + "ms. (Test could fail if random backoff is unusually low)");
  }

  @Test
  public void testSendsCorrectAuthorizationHeader() throws Exception {
    // Given
    client = new SimpleHttpClient();
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    String endpoint = mockWebServer.url("/").toString();
    // Configure the client to use the standard "Authorization" header.
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("my-secret-key")
            .apiKeyHeader("Authorization")
            .build();

    // When
    client.request(config, "{}");

    // Then
    assertEquals(1, mockWebServer.getRequestCount());
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    String authHeader = recordedRequest.getHeader("Authorization");

    assertNotNull(authHeader, "Authorization header should be present");
    assertEquals(
        "Bearer my-secret-key",
        authHeader,
        "Authorization header should be correctly formatted with 'Bearer' prefix");
  }

  @Test
  public void testSendsCorrectCustomApiKeyHeader() throws Exception {
    // Given
    client = new SimpleHttpClient();
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    String endpoint = mockWebServer.url("/").toString();
    // Use a custom header name
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("my-secret-key")
            .apiKeyHeader("X-Api-Key")
            .build();

    // When
    client.request(config, "{}");

    // Then
    assertEquals(1, mockWebServer.getRequestCount());
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    String authHeader = recordedRequest.getHeader("X-Api-Key");

    assertNotNull(authHeader, "Custom API key header should be present");
    assertEquals(
        "my-secret-key", authHeader, "Custom API key header should not have 'Bearer' prefix");
    assertNull(
        recordedRequest.getHeader("Authorization"),
        "Default Authorization header should not be present");
  }

  @Test
  public void testUsesDefaultAuthorizationHeaderWhenNotSpecified() throws Exception {
    // Given
    client = new SimpleHttpClient();
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
    String endpoint = mockWebServer.url("/").toString();

    // Configure the client with an API key but no explicit header name.
    AuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKey("my-default-key")
            // .apiKeyHeader(...) is intentionally omitted
            .build();

    // When
    client.request(config, "{}");

    // Then
    assertEquals(1, mockWebServer.getRequestCount());
    RecordedRequest recordedRequest = mockWebServer.takeRequest();
    String authHeader = recordedRequest.getHeader("Authorization");

    assertNotNull(authHeader, "Default Authorization header should be present");
    assertEquals(
        "Bearer my-default-key",
        authHeader,
        "Authorization header should be correctly formatted with 'Bearer' prefix");
  }
}
