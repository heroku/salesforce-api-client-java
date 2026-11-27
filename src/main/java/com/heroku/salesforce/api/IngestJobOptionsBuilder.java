/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import javax.annotation.Nonnull;

/**
 * Builder for {@link IngestJobOptions}. Uses {@link ColumnDelimiter#COMMA} and {@link
 * LineEnding#LF} unless configured otherwise.
 *
 * @see BulkApi#newIngestJobOptionsBuilder()
 */
public final class IngestJobOptionsBuilder {
  private String externalIdFieldName;
  private String assignmentRuleId;
  private ColumnDelimiter columnDelimiter = ColumnDelimiter.COMMA;
  private LineEnding lineEnding = LineEnding.LF;

  IngestJobOptionsBuilder() {}

  /**
   * Sets the external ID field used to match records. Required for {@link IngestOperation#UPSERT}
   * jobs.
   *
   * @param externalIdFieldName The API name of the external ID field.
   * @return This {@link IngestJobOptionsBuilder} instance to allow method chaining.
   */
  @Nonnull
  public IngestJobOptionsBuilder withExternalIdFieldName(String externalIdFieldName) {
    this.externalIdFieldName = externalIdFieldName;
    return this;
  }

  /**
   * Sets the id of the Case or Lead assignment rule to apply to the ingested records.
   *
   * @param assignmentRuleId The id of the assignment rule.
   * @return This {@link IngestJobOptionsBuilder} instance to allow method chaining.
   */
  @Nonnull
  public IngestJobOptionsBuilder withAssignmentRuleId(String assignmentRuleId) {
    this.assignmentRuleId = assignmentRuleId;
    return this;
  }

  /**
   * Sets the column delimiter of the uploaded CSV data.
   *
   * @param columnDelimiter The column delimiter.
   * @return This {@link IngestJobOptionsBuilder} instance to allow method chaining.
   */
  @Nonnull
  public IngestJobOptionsBuilder withColumnDelimiter(ColumnDelimiter columnDelimiter) {
    this.columnDelimiter = columnDelimiter;
    return this;
  }

  /**
   * Sets the line ending of the uploaded CSV data.
   *
   * @param lineEnding The line ending.
   * @return This {@link IngestJobOptionsBuilder} instance to allow method chaining.
   */
  @Nonnull
  public IngestJobOptionsBuilder withLineEnding(LineEnding lineEnding) {
    this.lineEnding = lineEnding;
    return this;
  }

  /**
   * Returns a new and immutable {@link IngestJobOptions} instance based on the information stored
   * in this builder.
   *
   * @return The new {@link IngestJobOptions} instance.
   */
  @Nonnull
  public IngestJobOptions build() {
    return new IngestJobOptions(externalIdFieldName, assignmentRuleId, columnDelimiter, lineEnding);
  }
}
