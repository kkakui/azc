/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AuthorizationResponse}. */
public class TestAuthorizationResponse {

  @Test
  public void testResponseWithAllowedTrueAndContext() {
    Map<String, Object> context = Map.of("traceId", "xyz-123");
    AuthorizationResponse response = new AuthorizationResponse(true, context);

    assertTrue(response.isAllowed());
    assertNotNull(response.getContext());
    assertEquals("xyz-123", response.getContext().get("traceId"));
  }

  @Test
  public void testResponseWithAllowedFalseAndContext() {
    Map<String, Object> context = Map.of("reason", "policy_violation");
    AuthorizationResponse response = new AuthorizationResponse(false, context);

    assertFalse(response.isAllowed());
    assertNotNull(response.getContext());
    assertEquals("policy_violation", response.getContext().get("reason"));
  }

  @Test
  public void testResponseWithAllowedTrueAndNullContext() {
    AuthorizationResponse response = new AuthorizationResponse(true, null);

    assertTrue(response.isAllowed());
    assertNull(response.getContext());
  }

  @Test
  public void testResponseWithAllowedFalseAndEmptyContext() {
    AuthorizationResponse response = new AuthorizationResponse(false, Collections.emptyMap());

    assertFalse(response.isAllowed());
    assertNotNull(response.getContext());
    assertTrue(response.getContext().isEmpty());
  }
}
