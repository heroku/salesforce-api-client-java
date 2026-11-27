/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Options for creating a bulk query job.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see BulkApi#newQueryJobOptionsBuilder()
 * @see BulkApi#createQueryJob(String, QueryOperation, QueryJobOptions)
 */
@Immutable
public final class QueryJobOptions {
  private final ColumnDelimiter columnDelimiter;
  private final LineEnding lineEnding;

  QueryJobOptions(ColumnDelimiter columnDelimiter, LineEnding lineEnding) {
    this.columnDelimiter = columnDelimiter;
    this.lineEnding = lineEnding;
  }

  /**
   * Returns the column delimiter of the CSV results.
   *
   * @return The column delimiter of the CSV results.
   */
  @Nonnull
  public ColumnDelimiter getColumnDelimiter() {
    return columnDelimiter;
  }

  /**
   * Returns the line ending of the CSV results.
   *
   * @return The line ending of the CSV results.
   */
  @Nonnull
  public LineEnding getLineEnding() {
    return lineEnding;
  }
}
