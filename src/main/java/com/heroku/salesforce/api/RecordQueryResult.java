/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Represents the result of a record query.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see DataApi#query(String)
 * @see DataApi#queryMore(RecordQueryResult)
 */
@Immutable
public final class RecordQueryResult {
  private final boolean done;
  private final long totalSize;
  private final Optional<String> nextRecordsPath;
  private final List<RecordWithSubQueryResults> records;

  RecordQueryResult(
      boolean done,
      long totalSize,
      Optional<String> nextRecordsPath,
      List<RecordWithSubQueryResults> records) {
    this.done = done;
    this.totalSize = totalSize;
    this.nextRecordsPath = nextRecordsPath;
    this.records = Collections.unmodifiableList(records);
  }

  RecordQueryResult(boolean done, long totalSize) {
    this(done, totalSize, Optional.empty(), Collections.emptyList());
  }

  /**
   * If true, no additional records can be retrieved from the query result. If false, one or more
   * records remain to be retrieved. Use {@link DataApi#queryMore(RecordQueryResult)} to receive the
   * next batch of records from this query result.
   *
   * @return If there are no more rows to be retrieved.
   */
  public boolean isDone() {
    return done;
  }

  /**
   * Returns the total amount of records returned by the query. This is not necessarily the same
   * amount of records returned by {@link #getRecords()}.
   *
   * @return The total amount of records returned by the query.
   * @see #isDone()
   */
  public long getTotalSize() {
    return totalSize;
  }

  /**
   * Returns the {@link RecordWithSubQueryResults}s in this query result. Use {@link #isDone()} to
   * determine if there are additional records to be queried with {@link
   * DataApi#queryMore(RecordQueryResult)}.
   *
   * @return The {@link RecordWithSubQueryResults}s in this query result.
   */
  @Nonnull
  public List<RecordWithSubQueryResults> getRecords() {
    return records;
  }

  Optional<String> getNextRecordsPath() {
    return nextRecordsPath;
  }
}
