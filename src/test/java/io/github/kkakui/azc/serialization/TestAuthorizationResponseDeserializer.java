/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.serialization;

import static org.junit.jupiter.api.Assertions.*;

import io.github.kkakui.azc.api.AuthorizationResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AuthorizationResponseDeserializer}. */
public class TestAuthorizationResponseDeserializer {

  @Test
  public void testParseAllowResponseWithContext() throws Exception {
    String json =
        """
        {
          "decision": true,
          "context": {
            "id": "abc123",
            "reason_admin": {
              "en": "Access granted by rule 42"
            },
            "reason_user": {
              "en-200": "Access granted"
            }
          }
        }
        """;

    AuthorizationResponse response = AuthorizationResponseDeserializer.parseResponseJson(json);
    assertTrue(response.isAllowed());
    assertNotNull(response.getContext());

    Map<String, Object> context = response.getContext();
    assertEquals("abc123", context.get("id"));
    assertTrue(((Map<?, ?>) context.get("reason_admin")).containsKey("en"));
    assertTrue(((Map<?, ?>) context.get("reason_user")).containsKey("en-200"));
  }

  @Test
  public void testParseDenyResponseWithoutContext() throws Exception {
    String json = "{ \"decision\": false }";
    AuthorizationResponse response = AuthorizationResponseDeserializer.parseResponseJson(json);
    assertFalse(response.isAllowed());
    assertNull(response.getContext());
  }

  @Test
  public void testParseContextStructureAndTypes() throws Exception {
    String json =
        """
        {
          "decision": false,
          "context": {
            "id": "0",
            "reason_admin": {
              "en": "Request failed policy C076E82F"
            },
            "reason_user": {
              "en-403": "Insufficient privileges",
              "es-403": "Privilegios insuficientes"
            }
          }
        }
        """;

    AuthorizationResponse response = AuthorizationResponseDeserializer.parseResponseJson(json);
    assertFalse(response.isAllowed());
    assertNotNull(response.getContext());

    Map<String, Object> context = response.getContext();
    assertEquals("0", context.get("id"));

    Map<?, ?> admin = (Map<?, ?>) context.get("reason_admin");
    assertEquals("Request failed policy C076E82F", admin.get("en"));

    Map<?, ?> user = (Map<?, ?>) context.get("reason_user");
    assertEquals("Insufficient privileges", user.get("en-403"));
    assertEquals("Privilegios insuficientes", user.get("es-403"));
  }
}
