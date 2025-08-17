/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.exception;

/**
 * An exception thrown to indicate a problem during the transport of an authorization request, such
 * as a network error or an unrecoverable HTTP status code.
 */
public class TransportException extends Exception {
  /**
   * Constructs a new TransportException with the specified detail message.
   *
   * @param message the detail message.
   */
  public TransportException(String message) {
    super(message);
  }

  /**
   * Constructs a new TransportException with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   */
  public TransportException(String message, Throwable cause) {
    super(message, cause);
  }
}
