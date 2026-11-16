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
import javax.annotation.concurrent.Immutable;

/**
 * Represents an error from the Data API. Contains detailed information about the underlying error.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see <a
 *     href="https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/errorcodes.htm">REST
 *     API Developer Guide - Status Codes and Error Responses</a>
 */
@Immutable
public final class DataApiError {
  private final String message;
  private final String errorCode;
  private final List<String> fields;

  DataApiError(String message, String errorCode, List<String> fields) {
    this.message = message;
    this.errorCode = errorCode;
    this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
  }

  /**
   * Returns the message of this error.
   *
   * @return The message of this error.
   */
  @Nonnull
  public String getMessage() {
    return message;
  }

  /**
   * Returns the error code for this error.
   *
   * @return The error code for this error.
   */
  @Nonnull
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Returns the field names where the error occurred. Might be empty for errors that are not
   * related to a specific field.
   *
   * @return The field names where the error occurred.
   */
  @Nonnull
  public List<String> getFields() {
    return fields;
  }
}
