/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents the response from an authorization service for a single access evaluation.
 *
 * @see <a
 *     href="https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md#access-evaluation-response">
 *     AuthZEN Authorization API Spec: Access Evaluation Response</a>
 */
public class AuthorizationResponse {
  private final boolean allowed;
  private final Map<String, Object> context;

  @JsonCreator
  public AuthorizationResponse(
      @JsonProperty("decision") boolean allowed,
      @JsonProperty("context") Map<String, Object> context) {
    this.allowed = allowed;
    this.context = context;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public Map<String, Object> getContext() {
    return context;
  }
}
