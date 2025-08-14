/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.config;

/**
 * Defines the configuration for the {@link io.github.kkakui.azc.api.AuthzClient}.
 *
 * <p>Implementations of this interface provide the necessary details for the client to connect and
 * authenticate to the authorization service endpoint.
 */
public interface AuthzClientConfig {
  String getEndpoint();
}
