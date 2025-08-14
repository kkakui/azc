/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.transport;

import io.github.kkakui.azc.config.AuthzClientConfig;

/**
 * An interface defining the transport layer for sending authorization requests.
 *
 * <p>Implementations are responsible for the mechanism of sending a request body to a configured
 * endpoint and returning the response body.
 */
public interface Transport {
  String request(AuthzClientConfig config, String jsonBody) throws Exception;
}
