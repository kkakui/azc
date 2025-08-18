/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultAuthzClientConfig}. */
public class TestDefaultAuthzClientConfig {

  @Test
  void testBuilderBuildsConfigCorrectly() {
    String endpoint = "https://api.example.com/v1/authz";
    String apiKeyHeader = "X-API-Key";
    String apiKey = "secret-key-123";

    DefaultAuthzClientConfig config =
        DefaultAuthzClientConfig.builder()
            .endpoint(endpoint)
            .apiKeyHeader(apiKeyHeader)
            .apiKey(apiKey)
            .build();

    assertEquals(endpoint, config.getEndpoint());
    assertEquals(apiKeyHeader, config.getApiKeyHeader().orElse(null));
    assertEquals(apiKey, config.getApiKey().orElse(null));
  }

  @Test
  void testBuilderThrowsExceptionWhenEndpointIsNull() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              DefaultAuthzClientConfig.builder().apiKey("some-key").build();
            });
    assertEquals("Endpoint must be provided.", exception.getMessage());
  }

  @Test
  void testBuilderThrowsExceptionWhenEndpointIsBlank() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              DefaultAuthzClientConfig.builder().endpoint("  ").apiKey("some-key").build();
            });
    assertEquals("Endpoint must be provided.", exception.getMessage());
  }

  @Test
  void testBuilderThrowsExceptionForInvalidUrl() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              DefaultAuthzClientConfig.builder().endpoint("not a valid url").build();
            });
    assertEquals("Endpoint must be a valid URL.", exception.getMessage());
    assertNotNull(exception.getCause());
    assertTrue(exception.getCause() instanceof IllegalArgumentException);
  }
}
