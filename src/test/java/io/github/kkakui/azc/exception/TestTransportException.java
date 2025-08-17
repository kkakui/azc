/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link TransportException}. */
public class TestTransportException {

  @Test
  void testConstructorWithMessage() {
    String errorMessage = "Network connection failed.";
    TransportException exception = new TransportException(errorMessage);

    assertEquals(errorMessage, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void testConstructorWithMessageAndCause() {
    String errorMessage = "Failed to send request.";
    Throwable cause = new RuntimeException("Underlying I/O error");
    TransportException exception = new TransportException(errorMessage, cause);

    assertEquals(errorMessage, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}
