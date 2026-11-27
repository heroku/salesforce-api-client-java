/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Information about a bulk query job.
 *
 * <p>Instances are immutable and therefore thread-safe. They reflect the state of the job at the
 * time of the API call that returned them.
 *
 * @see BulkApi#getJobInfo(QueryJobReference)
 */
@Immutable
public final class QueryJobInfo {
  private final QueryJobReference reference;
  private final String objectType;
  private final QueryOperation operation;
  private final QueryJobState state;
  private final ColumnDelimiter columnDelimiter;
  private final LineEnding lineEnding;
  private final long numberRecordsProcessed;
  private final int retries;
  private final String errorMessage;
  private final String createdById;
  private final OffsetDateTime createdDate;
  private final OffsetDateTime systemModstamp;
  private final Duration totalProcessingTime;

  QueryJobInfo(
      QueryJobReference reference,
      String objectType,
      QueryOperation operation,
      QueryJobState state,
      ColumnDelimiter columnDelimiter,
      LineEnding lineEnding,
      long numberRecordsProcessed,
      int retries,
      @Nullable String errorMessage,
      String createdById,
      OffsetDateTime createdDate,
      OffsetDateTime systemModstamp,
      Duration totalProcessingTime) {
    this.reference = reference;
    this.objectType = objectType;
    this.operation = operation;
    this.state = state;
    this.columnDelimiter = columnDelimiter;
    this.lineEnding = lineEnding;
    this.numberRecordsProcessed = numberRecordsProcessed;
    this.retries = retries;
    this.errorMessage = errorMessage;
    this.createdById = createdById;
    this.createdDate = createdDate;
    this.systemModstamp = systemModstamp;
    this.totalProcessingTime = totalProcessingTime;
  }

  /**
   * Returns a reference to the job, for use with other {@link BulkApi} methods.
   *
   * @return A reference to the job.
   */
  @Nonnull
  public QueryJobReference getReference() {
    return reference;
  }

  /**
   * Returns the id of the job.
   *
   * @return The id of the job.
   */
  @Nonnull
  public String getId() {
    return reference.getId();
  }

  /**
   * Returns the type of object the job queries, e.g. {@code Account}.
   *
   * @return The object type.
   */
  @Nonnull
  public String getObjectType() {
    return objectType;
  }

  /**
   * Returns the operation of the job.
   *
   * @return The operation of the job.
   */
  @Nonnull
  public QueryOperation getOperation() {
    return operation;
  }

  /**
   * Returns the processing state of the job.
   *
   * @return The processing state of the job.
   */
  @Nonnull
  public QueryJobState getState() {
    return state;
  }

  /**
   * Returns the column delimiter of the job's CSV results.
   *
   * @return The column delimiter.
   */
  @Nonnull
  public ColumnDelimiter getColumnDelimiter() {
    return columnDelimiter;
  }

  /**
   * Returns the line ending of the job's CSV results.
   *
   * @return The line ending.
   */
  @Nonnull
  public LineEnding getLineEnding() {
    return lineEnding;
  }

  /**
   * Returns the number of records processed so far.
   *
   * @return The number of processed records.
   */
  public long getNumberRecordsProcessed() {
    return numberRecordsProcessed;
  }

  /**
   * Returns the number of times Salesforce retried processing the job.
   *
   * @return The number of retries.
   */
  public int getRetries() {
    return retries;
  }

  /**
   * Returns the error message of a {@link QueryJobState#FAILED} job.
   *
   * @return The error message, if the job has one.
   */
  @Nonnull
  public Optional<String> getErrorMessage() {
    return Optional.ofNullable(errorMessage);
  }

  /**
   * Returns the id of the user who created the job.
   *
   * @return The id of the user who created the job.
   */
  @Nonnull
  public String getCreatedById() {
    return createdById;
  }

  /**
   * Returns the date and time the job was created.
   *
   * @return The creation date of the job.
   */
  @Nonnull
  public OffsetDateTime getCreatedDate() {
    return createdDate;
  }

  /**
   * Returns the date and time the job was last modified.
   *
   * @return The last modification date of the job.
   */
  @Nonnull
  public OffsetDateTime getSystemModstamp() {
    return systemModstamp;
  }

  /**
   * Returns the total time spent processing the job.
   *
   * @return The total processing time.
   */
  @Nonnull
  public Duration getTotalProcessingTime() {
    return totalProcessingTime;
  }
}
