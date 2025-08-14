/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.transport.http;

import io.github.kkakui.azc.config.AuthzClientConfig;
import io.github.kkakui.azc.config.DefaultAuthzClientConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * An implementation of {@link HttpTransport} that uses Java's built-in {@link HttpClient}.
 *
 * <p>This client handles sending authorization requests over HTTP, including setting appropriate
 * headers for content type and API key authentication. It also features a retry mechanism with
 * exponential backoff for handling transient server errors.
 */
public class SimpleHttpClient implements HttpTransport {
  private static final Logger logger = Logger.getLogger(SimpleHttpClient.class.getName());
  private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);
  private static final int DEFAULT_MAX_RETRIES = 3;

  private final HttpClient client;
  private final int maxRetries;
  private final Duration requestTimeout;

  public SimpleHttpClient() {
    this(DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, DEFAULT_MAX_RETRIES);
  }

  public SimpleHttpClient(Duration connectTimeout) {
    this(connectTimeout, DEFAULT_REQUEST_TIMEOUT, DEFAULT_MAX_RETRIES);
  }

  public SimpleHttpClient(int maxRetries) {
    this(DEFAULT_CONNECT_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, maxRetries);
  }

  public SimpleHttpClient(Duration connectTimeout, int maxRetries) {
    this(connectTimeout, DEFAULT_REQUEST_TIMEOUT, maxRetries);
  }

  /**
   * The primary constructor for SimpleHttpClient.
   *
   * @param connectTimeout The timeout for establishing a connection.
   * @param requestTimeout The timeout for the entire request-response exchange.
   * @param maxRetries The maximum number of retries for transient failures.
   */
  public SimpleHttpClient(Duration connectTimeout, Duration requestTimeout, int maxRetries) {
    this.client = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    this.maxRetries = maxRetries;
    this.requestTimeout = requestTimeout;
  }

  @Override
  public String request(AuthzClientConfig config, String jsonBody) throws Exception {
    String url = config.getEndpoint();
    if (url == null || url.isBlank()) {
      throw new IllegalArgumentException("Endpoint URL must be provided in AuthzClientConfig.");
    }

    String requestId = UUID.randomUUID().toString();

    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("X-Request-ID", requestId)
            .timeout(this.requestTimeout)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

    // This transport requires API key authentication.
    // It expects the config to be a DefaultAuthzClientConfig to retrieve the key.
    if (config instanceof DefaultAuthzClientConfig dac) {
      String apiKey = dac.getApiKey();
      if (apiKey != null && !apiKey.isBlank()) {
        String headerName = dac.getApiKeyHeader() != null ? dac.getApiKeyHeader() : "Authorization";
        String headerValue =
            headerName.equalsIgnoreCase("Authorization") ? "Bearer " + apiKey : apiKey;
        requestBuilder.header(headerName, headerValue);
      }
    } else {
      logger.warning(
          "AuthzClientConfig is not an instance of DefaultAuthzClientConfig. "
              + "API key authentication will be skipped, which may result in authorization failures.");
    }

    HttpRequest request = requestBuilder.build();

    int attempt = 0;
    while (true) {
      try {
        logger.info("Sending request to: " + url + " with X-Request-ID: " + requestId);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        logger.info("Received response with status code: " + statusCode);
        // Success
        if (statusCode >= 200 && statusCode < 300) {
          return response.body();
        }
        // For server errors (5xx), we want to retry. Throw an IOException to be caught by the retry
        // logic.
        if (statusCode >= 500 && statusCode < 600) {
          throw new IOException("Server error (will retry): " + statusCode);
        }
        // For client errors (4xx) and other non-success codes, fail immediately.
        throw new Exception(
            "HTTP request failed with status " + statusCode + ": " + response.body());

      } catch (IOException e) { // Only retry on specific transient errors
        logger.warning("Request failed on attempt " + (attempt + 1) + ": " + e.getMessage());
        if (++attempt > maxRetries) {
          throw e;
        }
        try {
          // Implement exponential backoff with full jitter to prevent thundering herd issues.
          long baseBackoff = 500; // 500ms
          long maxBackoff = 30000; // 30s
          long currentCeiling = (long) (baseBackoff * Math.pow(2, attempt - 1));
          long backoff = Math.min(maxBackoff, currentCeiling);
          long sleepTime = (long) (Math.random() * backoff);
          logger.info("Retrying in " + sleepTime + " ms...");
          Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
          logger.warning("Retry loop interrupted.");
          Thread.currentThread().interrupt(); // Restore the interrupted status.
          throw e; // Re-throw the original IOException to exit the retry loop.
        }
      }
    }
  }
}
