/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.context;

import io.github.kkakui.azc.model.Context;

/**
 * A factory for creating {@link io.github.kkakui.azc.model.Context} objects.
 *
 * <p>This allows for the dynamic creation and injection of contextual attributes (e.g., timestamp,
 * request source IP) into every authorization request made by the {@link
 * io.github.kkakui.azc.api.AuthzClient}.
 */
public interface ContextFactory {
  Context createContext();
}
