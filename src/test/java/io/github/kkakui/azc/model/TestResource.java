/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Resource}. */
public class TestResource {

  @Test
  void testBuilder_withRequiredFields() {
    Resource resource = new Resource.Builder().type("document").id("doc-123").build();
    assertEquals("document", resource.getType());
    assertEquals("doc-123", resource.getId());
    assertNotNull(resource.getProperties());
    assertTrue(resource.getProperties().isEmpty());
  }

  @Test
  void testBuilder_withProperties() {
    Resource resource =
        new Resource.Builder()
            .type("folder")
            .id("folder-456")
            .addProperty("owner", "bob")
            .addProperty("public", false)
            .build();

    assertEquals("folder", resource.getType());
    assertEquals("folder-456", resource.getId());
    Map<String, Object> properties = resource.getProperties();
    assertNotNull(properties);
    assertEquals(2, properties.size());
    assertEquals("bob", properties.get("owner"));
    assertEquals(false, properties.get("public"));
  }

  @Test
  void testBuilder_propertiesAreImmutable() {
    Resource resource =
        new Resource.Builder()
            .type("report")
            .id("report-789")
            .addProperty("status", "draft")
            .build();

    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          resource.getProperties().put("status", "published");
        });
  }

  @Test
  void testBuilder_missingTypeThrowsException() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> new Resource.Builder().id("some-id").build());
    assertEquals("Resource 'type' must not be null or blank.", e.getMessage());
  }

  @Test
  void testBuilder_missingIdThrowsException() {
    Exception e =
        assertThrows(
            IllegalArgumentException.class, () -> new Resource.Builder().type("some-type").build());
    assertEquals("Resource 'id' must not be null or blank.", e.getMessage());
  }
}
