/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.context;

import io.github.kkakui.azc.model.Context;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation of {@link ContextFactory} that adds the current timestamp to the context
 * of every authorization request.
 */
public class DefaultContextFactory implements ContextFactory {

  @Override
  public Context createContext() {
    Map<String, Object> attrs = new HashMap<>();
    attrs.put("timestamp", Instant.now().toString());
    return new Context(attrs);
  }
}
