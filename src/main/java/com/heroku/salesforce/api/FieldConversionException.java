/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/** Signals that a {@link Record}'s field could not be converted into the requested type. */
public class FieldConversionException extends RuntimeException {
  /**
   * Constructs a FieldConversionException with the specified detail message.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   */
  public FieldConversionException(String message) {
    super(message);
  }

  /**
   * Constructs a FieldConversionException with the specified detail message and cause.
   *
   * @param message The detail message (which is saved for later retrieval by the {@link
   *     Throwable#getMessage()} method)
   * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()}
   *     method). (A null value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public FieldConversionException(String message, Throwable cause) {
    super(message, cause);
  }
}
