/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link Action}. */
public class TestAction {

  @Test
  public void testBuildValidAction() {
    Action action = new Action.Builder().name("read").build();
    assertNotNull(action);
    assertEquals("read", action.getName());
  }

  @Test
  public void testBuildSubjectWithoutNameThrowsException() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              new Action.Builder().build();
            });
    assertEquals("Action 'name' must not be null or blank.", exception.getMessage());
  }
}
