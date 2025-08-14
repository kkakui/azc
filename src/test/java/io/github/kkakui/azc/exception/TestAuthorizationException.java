/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link AuthorizationException}. */
public class TestAuthorizationException {

  @Test
  void testExceptionWithMessage() {
    String message = "A test error occurred";
    AuthorizationException exception = new AuthorizationException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void testExceptionWithMessageAndCause() {
    String message = "A test error occurred";
    Throwable cause = new RuntimeException("The root cause");
    AuthorizationException exception = new AuthorizationException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
