/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Options for creating a bulk ingest job.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see BulkApi#newIngestJobOptionsBuilder()
 * @see BulkApi#createIngestJob(String, IngestOperation, IngestJobOptions)
 */
@Immutable
public final class IngestJobOptions {
  private final String externalIdFieldName;
  private final String assignmentRuleId;
  private final ColumnDelimiter columnDelimiter;
  private final LineEnding lineEnding;

  IngestJobOptions(
      @Nullable String externalIdFieldName,
      @Nullable String assignmentRuleId,
      ColumnDelimiter columnDelimiter,
      LineEnding lineEnding) {
    this.externalIdFieldName = externalIdFieldName;
    this.assignmentRuleId = assignmentRuleId;
    this.columnDelimiter = columnDelimiter;
    this.lineEnding = lineEnding;
  }

  /**
   * Returns the external ID field used to match records in {@link IngestOperation#UPSERT} jobs.
   *
   * @return The external ID field name, if set.
   */
  @Nonnull
  public Optional<String> getExternalIdFieldName() {
    return Optional.ofNullable(externalIdFieldName);
  }

  /**
   * Returns the id of the Case or Lead assignment rule to apply to the ingested records.
   *
   * @return The assignment rule id, if set.
   */
  @Nonnull
  public Optional<String> getAssignmentRuleId() {
    return Optional.ofNullable(assignmentRuleId);
  }

  /**
   * Returns the column delimiter of the uploaded CSV data.
   *
   * @return The column delimiter of the uploaded CSV data.
   */
  @Nonnull
  public ColumnDelimiter getColumnDelimiter() {
    return columnDelimiter;
  }

  /**
   * Returns the line ending of the uploaded CSV data.
   *
   * @return The line ending of the uploaded CSV data.
   */
  @Nonnull
  public LineEnding getLineEnding() {
    return lineEnding;
  }
}
