/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an action (or verb) in an authorization request.
 *
 * <p>An action is defined by its name and can include additional, arbitrary properties. This class
 * is immutable and should be constructed using its inner {@link Builder}.
 *
 * @see <a
 *     href="https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md#action">AuthZEN
 *     Authorization API Spec: Action</a>
 */
public class Action {
  private final String name;
  private final Map<String, Object> properties;

  private Action(String name, Map<String, Object> properties) {
    this.name = name;
    this.properties = Map.copyOf(properties);
  }

  public static class Builder {
    private String name;
    private Map<String, Object> properties = new HashMap<>();

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder addProperty(String key, Object value) {
      properties.put(key, value);
      return this;
    }

    public Action build() {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Action 'name' must not be null or blank.");
      }
      return new Action(name, properties);
    }
  }

  public String getName() {
    return name;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
