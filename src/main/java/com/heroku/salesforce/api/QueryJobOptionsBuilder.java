/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import javax.annotation.Nonnull;

/**
 * Builder for {@link QueryJobOptions}. Uses {@link ColumnDelimiter#COMMA} and {@link LineEnding#LF}
 * unless configured otherwise.
 *
 * @see BulkApi#newQueryJobOptionsBuilder()
 */
public final class QueryJobOptionsBuilder {
  private ColumnDelimiter columnDelimiter = ColumnDelimiter.COMMA;
  private LineEnding lineEnding = LineEnding.LF;

  QueryJobOptionsBuilder() {}

  /**
   * Sets the column delimiter of the CSV results.
   *
   * @param columnDelimiter The column delimiter.
   * @return This {@link QueryJobOptionsBuilder} instance to allow method chaining.
   */
  @Nonnull
  public QueryJobOptionsBuilder withColumnDelimiter(ColumnDelimiter columnDelimiter) {
    this.columnDelimiter = columnDelimiter;
    return this;
  }

  /**
   * Sets the line ending of the CSV results.
   *
   * @param lineEnding The line ending.
   * @return This {@link QueryJobOptionsBuilder} instance to allow method chaining.
   */
  @Nonnull
  public QueryJobOptionsBuilder withLineEnding(LineEnding lineEnding) {
    this.lineEnding = lineEnding;
    return this;
  }

  /**
   * Returns a new and immutable {@link QueryJobOptions} instance based on the information stored in
   * this builder.
   *
   * @return The new {@link QueryJobOptions} instance.
   */
  @Nonnull
  public QueryJobOptions build() {
    return new QueryJobOptions(columnDelimiter, lineEnding);
  }
}
