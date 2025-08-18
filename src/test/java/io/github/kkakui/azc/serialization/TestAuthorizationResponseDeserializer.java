/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.serialization;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.kkakui.azc.api.AuthorizationResponse;
import io.github.kkakui.azc.exception.AuthorizationException;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AuthorizationResponseDeserializer}. */
public class TestAuthorizationResponseDeserializer {

  @Test
  void testParseValidResponseAllowed() throws AuthorizationException {
    String json = "{\"decision\": true, \"context\": {\"traceId\": \"123\"}}";
    AuthorizationResponse response = AuthorizationResponseDeserializer.parseResponseJson(json);
    assertTrue(response.isAllowed());
    assertEquals("123", response.getContext().get("traceId"));
  }

  @Test
  void testParseValidResponseDenied() throws AuthorizationException {
    String json = "{\"decision\": false}";
    AuthorizationResponse response = AuthorizationResponseDeserializer.parseResponseJson(json);
    assertFalse(response.isAllowed());
    assertNull(response.getContext());
  }

  @Test
  void testParseInvalidJsonThrowsAuthorizationException() {
    String invalidJson = "{\"decision\": true,"; // Malformed JSON
    AuthorizationException exception =
        assertThrows(
            AuthorizationException.class,
            () -> AuthorizationResponseDeserializer.parseResponseJson(invalidJson));

    assertEquals("Failed to deserialize authorization response from JSON.", exception.getMessage());
    assertNotNull(exception.getCause());
    assertTrue(exception.getCause() instanceof JsonProcessingException);
  }

  @Test
  void testParseNullJsonThrowsAuthorizationException() {
    AuthorizationException exception =
        assertThrows(
            AuthorizationException.class,
            () -> AuthorizationResponseDeserializer.parseResponseJson(null));
    assertEquals("Response JSON from server was null or empty.", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void testParseEmptyJsonThrowsAuthorizationException() {
    AuthorizationException exception =
        assertThrows(
            AuthorizationException.class,
            () -> AuthorizationResponseDeserializer.parseResponseJson("  "));
    assertEquals("Response JSON from server was null or empty.", exception.getMessage());
    assertNull(exception.getCause());
  }
}
