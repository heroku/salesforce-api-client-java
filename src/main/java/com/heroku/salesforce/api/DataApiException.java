/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** Signals that a data API error occurred. */
public final class DataApiException extends Exception {
  private final List<DataApiError> errors;

  /**
   * Constructs a DataApiException with the specified detail message.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   */
  public DataApiException(String message) {
    super(message);
    errors = Collections.unmodifiableList(new ArrayList<>());
  }

  /**
   * Constructs a DataApiException with the specified detail message and cause.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
   *     method). (A null value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public DataApiException(String message, Throwable cause) {
    super(message, cause);
    errors = Collections.unmodifiableList(new ArrayList<>());
  }

  /**
   * Constructs a DataApiException with the specified detail message and list of API errors.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   * @param errors The list API errors (which is saved for later retrieval by the {@link
   *     #getDataApiErrors()} method)
   */
  public DataApiException(String message, List<DataApiError> errors) {
    super(message);
    this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
  }

  /**
   * Returns the list of {@link DataApiError}s that occurred. This list might be empty if the
   * exception wasn't caused by an API error.
   *
   * @return The list of {@link DataApiError}s that occurred.
   */
  @Nonnull
  public List<DataApiError> getDataApiErrors() {
    return errors;
  }
}
