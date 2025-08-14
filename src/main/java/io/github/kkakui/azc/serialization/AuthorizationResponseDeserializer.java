/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kkakui.azc.api.AuthorizationResponse;

/**
 * A utility class for deserializing a JSON string into an {@link AuthorizationResponse} object.
 * This class uses Jackson for data binding.
 */
public class AuthorizationResponseDeserializer {
  private static final ObjectMapper mapper = new ObjectMapper();

  private AuthorizationResponseDeserializer() {
    // Prevent instantiation of this utility class
  }

  public static AuthorizationResponse parseResponseJson(String json) throws Exception {
    // The AuthorizationResponse class is annotated with @JsonCreator and @JsonProperty
    // to allow Jackson to map the JSON fields ("decision", "context") directly
    // to the constructor parameters.
    return mapper.readValue(json, AuthorizationResponse.class);
  }
}
