/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.transport.http;

import io.github.kkakui.azc.config.AuthzClientConfig;
import io.github.kkakui.azc.exception.AuthorizationException;
import io.github.kkakui.azc.exception.TransportException;
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
  public String request(AuthzClientConfig config, String jsonBody) throws AuthorizationException {
    String url = config.getEndpoint();
    if (url == null || url.isBlank()) {
      throw new AuthorizationException(
          "Invalid client configuration: Endpoint URL must be provided.",
          new TransportException("Endpoint URL is null or blank."));
    }

    String requestId = UUID.randomUUID().toString();

    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("X-Request-ID", requestId)
            .timeout(this.requestTimeout)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));

    config
        .getApiKey()
        .ifPresent(
            apiKey -> {
              String headerName = config.getApiKeyHeader().orElse("Authorization");
              String headerValue =
                  headerName.equalsIgnoreCase("Authorization") ? "Bearer " + apiKey : apiKey;
              requestBuilder.header(headerName, headerValue);
            });

    HttpRequest request = requestBuilder.build();

    for (int attempt = 0; ; attempt++) {
      try {
        logger.info(
            "Sending request (attempt "
                + (attempt + 1)
                + ") to: "
                + url
                + " with X-Request-ID: "
                + requestId);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        logger.info("Received response with status code: " + statusCode);

        if (statusCode >= 200 && statusCode < 300) {
          return response.body();
        }

        // For client errors (4xx), fail immediately without retry.
        if (statusCode >= 400 && statusCode < 500) {
          throw new AuthorizationException(
              "HTTP request failed with status " + statusCode + ": " + response.body());
        }

        // For server errors (5xx), we will enter the retry logic below.
        if (statusCode >= 500 && statusCode < 600) {
          if (attempt >= maxRetries) {
            throw new AuthorizationException(
                "Request failed after "
                    + (attempt + 1)
                    + " attempts with server error: "
                    + statusCode);
          }
          logger.warning("Server error on attempt " + (attempt + 1) + ". Retrying...");
        } else {
          // For other unexpected status codes
          throw new AuthorizationException(
              "HTTP request failed with unexpected status " + statusCode + ": " + response.body());
        }

      } catch (IOException e) { // Retryable network error
        if (attempt >= maxRetries) {
          throw new AuthorizationException(
              "Request failed after " + (attempt + 1) + " attempts due to a network error.",
              new TransportException("Network error.", e));
        }
        logger.warning(
            "Network error on attempt " + (attempt + 1) + ". Retrying... Error: " + e.getMessage());
      } catch (InterruptedException e) {
        // Not retryable. Propagate interruption.
        Thread.currentThread().interrupt();
        throw new AuthorizationException(
            "Request was interrupted.",
            new TransportException("Request thread was interrupted.", e));
      }

      // If we reach here, we are retrying. Perform backoff.
      try {
        long baseBackoff = 500; // 500ms
        long maxBackoff = 30000; // 30s
        long currentCeiling = (long) (baseBackoff * Math.pow(2, attempt));
        long backoff = Math.min(maxBackoff, currentCeiling);
        long sleepTime = (long) (Math.random() * backoff);
        logger.info("Retrying in " + sleepTime + " ms...");
        Thread.sleep(sleepTime);
      } catch (InterruptedException ie) {
        logger.warning("Retry loop interrupted.");
        Thread.currentThread().interrupt();
        throw new AuthorizationException(
            "Request was interrupted during retry backoff.",
            new TransportException("Retry backoff was interrupted.", ie));
      }
    }
  }
}
