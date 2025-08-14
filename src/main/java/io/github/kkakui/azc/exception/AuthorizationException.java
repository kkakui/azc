/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.exception;

/**
 * A custom exception thrown for errors that occur during the authorization process, such as
 * transport failures or issues with serializing/deserializing requests and responses.
 */
public class AuthorizationException extends Exception {
  public AuthorizationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthorizationException(String message) {
    super(message);
  }
}
