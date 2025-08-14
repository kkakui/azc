/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Context}. */
public class TestContext {

  @Test
  void testConstructorWithNullMap() {
    Context context = new Context(null);
    assertNotNull(context.getAttributes());
    assertTrue(context.getAttributes().isEmpty());
  }

  @Test
  void testConstructorCreatesImmutableCopy() {
    Map<String, Object> originalMap = new HashMap<>();
    originalMap.put("key", "value");
    Context context = new Context(originalMap);

    // Verify that modifying the original map doesn't affect the context
    originalMap.put("another_key", "another_value");
    assertEquals(1, context.getAttributes().size());
    assertEquals("value", context.getAttributes().get("key"));

    // Verify that the returned map is unmodifiable
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          context.getAttributes().put("new_key", "new_value");
        });
  }

  @Test
  void testMergeContexts() {
    Context context1 = new Context(Map.of("a", 1, "b", 2));
    Context context2 = new Context(Map.of("b", 99, "c", 3));

    Context mergedContext = context1.merge(context2);

    // Check merged result
    assertEquals(3, mergedContext.getAttributes().size());
    assertEquals(1, mergedContext.getAttributes().get("a"));
    assertEquals(99, mergedContext.getAttributes().get("b")); // from context2
    assertEquals(3, mergedContext.getAttributes().get("c"));

    // Check that original contexts are unchanged
    assertEquals(2, context1.getAttributes().size());
    assertEquals(2, context1.getAttributes().get("b"));
  }

  @Test
  void testMergeWithNullOrEmptyContextReturnsSameInstance() {
    Context context1 = new Context(Map.of("a", 1));

    Context mergedWithNull = context1.merge(null);
    assertSame(context1, mergedWithNull, "Merging with null should return the same instance");

    Context mergedWithEmpty = context1.merge(new Context(Map.of()));
    assertSame(context1, mergedWithEmpty, "Merging with empty should return the same instance");
  }
}
