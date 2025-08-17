/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Subject}. */
public class TestSubject {

  @Test
  void testBuilder_withRequiredFields() {
    Subject subject = new Subject.Builder().type("user").id("alice").build();
    assertEquals("user", subject.getType());
    assertEquals("alice", subject.getId());
    assertNotNull(subject.getProperties());
    assertTrue(subject.getProperties().isEmpty());
  }

  @Test
  void testBuilder_withProperties() {
    Subject subject =
        new Subject.Builder()
            .type("user")
            .id("bob")
            .addProperty("department", "engineering")
            .addProperty("active", true)
            .build();

    assertEquals("user", subject.getType());
    assertEquals("bob", subject.getId());
    Map<String, Object> properties = subject.getProperties();
    assertNotNull(properties);
    assertEquals(2, properties.size());
    assertEquals("engineering", properties.get("department"));
    assertEquals(true, properties.get("active"));
  }

  @Test
  void testBuilder_propertiesAreImmutable() {
    Subject subject =
        new Subject.Builder().type("user").id("charlie").addProperty("role", "guest").build();

    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          subject.getProperties().put("role", "admin");
        });
  }

  @Test
  void testBuilder_missingTypeThrowsException() {
    // Assuming the builder validates that 'type' is present.
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> new Subject.Builder().id("dave").build());
    assertEquals("Subject 'type' must not be null or blank.", e.getMessage());
  }

  @Test
  void testBuilder_missingIdThrowsException() {
    // Assuming the builder validates that 'id' is present.
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> new Subject.Builder().type("service").build());
    assertEquals("Subject 'id' must not be null or blank.", e.getMessage());
  }
}
