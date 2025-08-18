/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.config;

import java.net.URI;
import java.util.Optional;

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

  private DefaultAuthzClientConfig(Builder builder) {
    this.endpoint = builder.endpoint;
    this.apiKey = builder.apiKey;
    this.apiKeyHeader = builder.apiKeyHeader;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String getEndpoint() {
    return endpoint;
  }

  @Override
  public Optional<String> getApiKey() {
    return Optional.ofNullable(apiKey);
  }

  @Override
  public Optional<String> getApiKeyHeader() {
    return Optional.ofNullable(apiKeyHeader);
  }

  public static class Builder {
    private String endpoint;
    private String apiKey;
    private String apiKeyHeader;

    private Builder() {}

    public Builder endpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder apiKeyHeader(String apiKeyHeader) {
      this.apiKeyHeader = apiKeyHeader;
      return this;
    }

    public DefaultAuthzClientConfig build() {
      if (endpoint == null || endpoint.isBlank()) {
        throw new IllegalStateException("Endpoint must be provided.");
      }
      try {
        // Validate that the endpoint is a valid URI
        URI.create(endpoint);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("Endpoint must be a valid URL.", e);
      }
      return new DefaultAuthzClientConfig(this);
    }
  }
}
