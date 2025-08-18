/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kkakui.azc.api.AuthorizationResponse;
import io.github.kkakui.azc.exception.AuthorizationException;

/**
 * A utility class for deserializing the JSON response from the authorization service into an {@link
 * AuthorizationResponse} object.
 */
public final class AuthorizationResponseDeserializer {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private AuthorizationResponseDeserializer() {}

  public static AuthorizationResponse parseResponseJson(String json) throws AuthorizationException {
    if (json == null || json.isBlank()) {
      throw new AuthorizationException("Response JSON from server was null or empty.");
    }
    try {
      return MAPPER.readValue(json, AuthorizationResponse.class);
    } catch (JsonProcessingException e) {
      // Wrap the specific parsing exception in our application-specific exception.
      throw new AuthorizationException(
          "Failed to deserialize authorization response from JSON.", e);
    }
  }
}
