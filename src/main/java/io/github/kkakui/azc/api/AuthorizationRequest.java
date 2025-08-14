/*
 * SPDX-FileCopyrightText: Copyright (c) 2025 Kentaro Kakui
 * SPDX-License-Identifier: MIT
 */
package io.github.kkakui.azc.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.kkakui.azc.model.Action;
import io.github.kkakui.azc.model.Context;
import io.github.kkakui.azc.model.Resource;
import io.github.kkakui.azc.model.Subject;

/**
 * Represents a single authorization request, containing the subject, resource, action, and optional
 * context. This class is immutable. A builder is provided for convenient construction.
 *
 * @see <a
 *     href="https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md#access-evaluation-request">
 *     AuthZEN Authorization API Spec: Access Evaluation Request</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationRequest {

  private final Subject subject;
  private final Resource resource;
  private final Action action;
  private final Context context;

  private AuthorizationRequest(Builder builder) {
    this.subject = builder.subject;
    this.resource = builder.resource;
    this.action = builder.action;
    this.context = builder.context;
  }

  public static class Builder {
    private Subject subject;
    private Resource resource;
    private Action action;
    private Context context;

    public Builder subject(Subject subject) {
      this.subject = subject;
      return this;
    }

    public Builder resource(Resource resource) {
      this.resource = resource;
      return this;
    }

    public Builder action(Action action) {
      this.action = action;
      return this;
    }

    public Builder context(Context context) {
      this.context = context;
      return this;
    }

    public AuthorizationRequest build() {
      if (subject == null) {
        throw new IllegalArgumentException("Subject must be provided.");
      }

      if (resource == null) {
        throw new IllegalArgumentException("Resource must be provided.");
      }

      if (action == null) {
        throw new IllegalArgumentException("Action must be provided.");
      }
      return new AuthorizationRequest(this);
    }
  }

  public Subject getSubject() {
    return subject;
  }

  public Resource getResource() {
    return resource;
  }

  public Action getAction() {
    return action;
  }

  public Context getContext() {
    return context;
  }

  /**
   * Creates a new AuthorizationRequest by merging this request's context with another. If the
   * provided context is null or empty, returns this instance. If this request has no context, the
   * new request's context will be the one provided.
   *
   * @param otherContext The context to merge.
   * @return A new AuthorizationRequest with the merged context.
   */
  public AuthorizationRequest withMergedContext(Context otherContext) {
    if (otherContext == null || otherContext.getAttributes().isEmpty()) {
      return this; // No changes needed
    }

    Context newContext = (this.context == null) ? otherContext : this.context.merge(otherContext);

    // Use the existing builder to create a new, immutable request instance
    return new AuthorizationRequest.Builder()
        .subject(this.subject)
        .resource(this.resource)
        .action(this.action)
        .context(newContext)
        .build();
  }
}
