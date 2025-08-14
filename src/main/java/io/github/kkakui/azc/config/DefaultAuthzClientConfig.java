/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.config;

/**
 * A default, concrete implementation of {@link AuthzClientConfig}.
 *
 * <p>This class holds the endpoint URL and authentication credentials (API key) for the
 * authorization service.
 */
public class DefaultAuthzClientConfig implements AuthzClientConfig {
  private final String endpoint;
  private final String apiKey;
  private final String apiKeyHeader;

  public DefaultAuthzClientConfig(String endpoint, String apiKeyHeader, String apiKey) {
    this.endpoint = endpoint;
    this.apiKeyHeader = apiKeyHeader;
    this.apiKey = apiKey;
  }

  @Override
  public String getEndpoint() {
    return endpoint;
  }

  public String getApiKeyHeader() {
    return apiKeyHeader;
  }

  public String getApiKey() {
    return apiKey;
  }
}
