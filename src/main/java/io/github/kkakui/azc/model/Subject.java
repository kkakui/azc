/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a subject (a user or machine principal) in an authorization request.
 *
 * <p>A subject is defined by its type and a unique ID, and can include additional, arbitrary
 * properties. This class is immutable and should be constructed using its inner {@link Builder}.
 *
 * @see <a
 *     href="https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md#subject">AuthZEN
 *     Authorization API Spec: Subject</a>
 */
public class Subject {
  private final String id;
  private final String type;
  private final Map<String, Object> properties;

  private Subject(String id, String type, Map<String, Object> properties) {
    this.id = id;
    this.type = type;
    this.properties = Map.copyOf(properties);
  }

  public static class Builder {
    private String id;
    private String type;
    private Map<String, Object> properties = new HashMap<>();

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder addProperty(String key, Object value) {
      properties.put(key, value);
      return this;
    }

    public Subject build() {
      if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("Subject 'id' must not be null or blank.");
      }
      if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("Subject 'type' must not be null or blank.");
      }
      return new Subject(id, type, properties);
    }
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
