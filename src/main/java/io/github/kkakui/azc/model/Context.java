/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the environmental or contextual data for an authorization request.
 *
 * <p>The context is a container for arbitrary key-value pairs that can provide additional
 * information for policy evaluation, such as the time of the request or the source IP address. This
 * class is immutable.
 *
 * @see <a
 *     href="https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md#context">AuthZEN
 *     Authorization API Spec: Context</a>
 */
public class Context {
  private final Map<String, Object> attributes;

  public Context(Map<String, Object> attributes) {
    // Use an immutable copy to prevent external modifications
    this.attributes = attributes == null ? Collections.emptyMap() : Map.copyOf(attributes);
  }

  @JsonValue
  public Map<String, Object> getAttributes() {
    // The map is already immutable
    return attributes;
  }

  public Context merge(Context other) {
    if (other == null || other.getAttributes().isEmpty()) return this;
    Map<String, Object> mergedAttributes = new HashMap<>(this.attributes);
    mergedAttributes.putAll(other.getAttributes());
    return new Context(mergedAttributes);
  }
}
