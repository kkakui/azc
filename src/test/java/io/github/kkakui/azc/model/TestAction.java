/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Action}. */
public class TestAction {

  @Test
  void testBuilder_withRequiredFields() {
    Action action = new Action.Builder().name("read").build();
    assertEquals("read", action.getName());
    assertNotNull(action.getProperties());
    assertTrue(action.getProperties().isEmpty());
  }

  @Test
  void testBuilder_withProperties() {
    Action action =
        new Action.Builder()
            .name("write")
            .addProperty("method", "PUT")
            .addProperty("field", "description")
            .build();

    assertEquals("write", action.getName());
    Map<String, Object> properties = action.getProperties();
    assertNotNull(properties);
    assertEquals(2, properties.size());
    assertEquals("PUT", properties.get("method"));
    assertEquals("description", properties.get("field"));
  }

  @Test
  void testBuilder_propertiesAreImmutable() {
    Action action = new Action.Builder().name("delete").addProperty("force", false).build();

    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          action.getProperties().put("force", true);
        });
  }

  @Test
  void testBuilder_missingNameThrowsException() {
    // Assuming the builder validates that 'name' is present.
    Exception e =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Action.Builder().build();
            });
    assertEquals("Action 'name' must not be null or blank.", e.getMessage());
  }
}
