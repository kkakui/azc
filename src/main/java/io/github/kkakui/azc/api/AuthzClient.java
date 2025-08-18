/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.api;

import io.github.kkakui.azc.config.AuthzClientConfig;
import io.github.kkakui.azc.context.ContextFactory;
import io.github.kkakui.azc.exception.AuthorizationException;
import io.github.kkakui.azc.serialization.AuthorizationRequestSerializer;
import io.github.kkakui.azc.serialization.AuthorizationResponseDeserializer;
import io.github.kkakui.azc.transport.Transport;

/**
 * The main client for interacting with the AuthZEN Authorization API.
 *
 * <p>This client provides a method to perform an authorization check by sending an {@link
 * AuthorizationRequest} to a configured Policy Decision Point (PDP) via a {@link
 * io.github.kkakui.azc.transport.Transport}. It can also be configured with a {@link
 * ContextFactory} to automatically inject contextual information into requests.
 */
public class AuthzClient {
  private final AuthzClientConfig config;
  private final Transport transport;
  private final ContextFactory contextFactory;

  public AuthzClient(AuthzClientConfig config, Transport transport) {
    this(config, transport, null);
  }

  public AuthzClient(AuthzClientConfig config, Transport transport, ContextFactory contextFactory) {
    this.config = config;
    this.transport = transport;
    this.contextFactory = contextFactory;
  }

  public AuthorizationResponse authorize(AuthorizationRequest request)
      throws AuthorizationException {
    try {
      if (contextFactory != null) {
        // Create a new request instance with the context from the factory merged in.
        request = request.withMergedContext(contextFactory.createContext());
      }
      String requestJson = AuthorizationRequestSerializer.buildRequestJson(request);
      String responseJson = transport.request(config, requestJson);
      return AuthorizationResponseDeserializer.parseResponseJson(responseJson);
    } catch (AuthorizationException e) {
      // Re-throw the specific exception from the transport layer or deserialization directly.
      throw e;
    } catch (Exception e) {
      // For any other unexpected exceptions, wrap them in an AuthorizationException.
      throw new AuthorizationException(
          "Authorization request failed due to an unexpected error", e);
    }
  }
}
