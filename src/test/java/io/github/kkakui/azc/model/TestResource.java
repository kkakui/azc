/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Resource}. */
public class TestResource {

  @Test
  public void testBuildValidResource() {
    Resource resource = new Resource.Builder().id("doc456").type("document").build();
    assertNotNull(resource);
    assertEquals("doc456", resource.getId());
    assertEquals("document", resource.getType());
  }

  @Test
  public void testBuildResourceWithoutIdThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Resource.Builder().type("document").build();
            });
    assertEquals("Resource 'id' must not be null or blank.", exception.getMessage());
  }

  @Test
  public void testBuildResourceWithoutTypeThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Resource.Builder().id("doc456").build();
            });
    assertEquals("Resource 'type' must not be null or blank.", exception.getMessage());
  }
}
