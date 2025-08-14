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
  void testConfigValuesAreSetCorrectly() {
    String endpoint = "https://api.example.com/v1/authz";
    String apiKeyHeader = "X-API-Key";
    String apiKey = "secret-key-123";

    DefaultAuthzClientConfig config = new DefaultAuthzClientConfig(endpoint, apiKeyHeader, apiKey);

    assertEquals(endpoint, config.getEndpoint());
    assertEquals(apiKeyHeader, config.getApiKeyHeader());
    assertEquals(apiKey, config.getApiKey());
  }
}
