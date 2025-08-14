/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kkakui.azc.api.AuthorizationRequest;

/**
 * A utility class for serializing an {@link AuthorizationRequest} object to a JSON string. This
 * class uses Jackson for data binding.
 */
public class AuthorizationRequestSerializer {
  private static final ObjectMapper mapper = new ObjectMapper();

  private AuthorizationRequestSerializer() {
    // Prevent instantiation of this utility class
  }

  public static String buildRequestJson(AuthorizationRequest request) throws Exception {
    // The model classes (AuthorizationRequest, Subject, etc.) are standard POJOs
    // with getters that Jackson can use for automatic data binding.
    // The @JsonValue annotation on Context.getAttributes() ensures the context
    // is serialized as a flat map, not an object containing an 'attributes' field.
    return mapper.writeValueAsString(request);
  }
}
