/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Subject}. */
public class TestSubject {

  @Test
  public void testBuildValidSubject() {
    Subject subject = new Subject.Builder().id("user123").type("user").build();
    assertNotNull(subject);
    assertEquals("user123", subject.getId());
    assertEquals("user", subject.getType());
  }

  @Test
  public void testBuildSubjectWithoutIdThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Subject.Builder().type("user").build();
            });
    assertEquals("Subject 'id' must not be null or blank.", exception.getMessage());
  }

  @Test
  public void testBuildSubjectWithoutTypeThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Subject.Builder().id("user123").build();
            });
    assertEquals("Subject 'type' must not be null or blank.", exception.getMessage());
  }
}
