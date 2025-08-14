/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.context;

import static org.junit.jupiter.api.Assertions.*;

import io.github.kkakui.azc.model.Context;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ContextFactory} implementations. */
public class TestContextFactory {

  static class CustomContextFactory implements ContextFactory {
    @Override
    public Context createContext() {
      return new Context(
          Map.of(
              "ip", "192.168.0.1",
              "timestamp", Instant.parse("2025-01-01T00:00:00Z").toString(),
              "source", "unit-test"));
    }
  }

  @Test
  public void testCustomContextFactoryProducesExpectedAttributes() {
    ContextFactory factory = new CustomContextFactory();
    Context context = factory.createContext();

    Map<String, Object> attributes = context.getAttributes();
    assertEquals("192.168.0.1", attributes.get("ip"));
    assertEquals("2025-01-01T00:00:00Z", attributes.get("timestamp"));
    assertEquals("unit-test", attributes.get("source"));
    assertEquals(3, attributes.size());
  }

  @Test
  public void testDefaultContextFactoryIncludesTimestamp() {
    ContextFactory factory = new DefaultContextFactory();
    Context context = factory.createContext();

    assertNotNull(context.getAttributes().get("timestamp"));
    assertTrue(context.getAttributes().get("timestamp") instanceof String);
  }
}
