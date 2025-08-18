/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.config;

import java.util.Optional;

/**
 * Defines the configuration for the {@link io.github.kkakui.azc.api.AuthzClient}.
 *
 * <p>Implementations of this interface provide the necessary details for the client to connect and
 * authenticate to the authorization service endpoint.
 */
public interface AuthzClientConfig {
  /**
   * Returns the URL of the authorization service endpoint.
   *
   * @return the endpoint URL.
   */
  String getEndpoint();

  /**
   * Returns the API key for authentication.
   *
   * @return an {@link Optional} containing the API key, or empty if not configured.
   */
  Optional<String> getApiKey();

  /**
   * Returns the name of the HTTP header to use for the API key.
   *
   * <p>If this is empty, a default value (e.g., "Authorization") may be used by the transport.
   *
   * @return an {@link Optional} containing the header name, or empty to use the default.
   */
  Optional<String> getApiKeyHeader();
}
