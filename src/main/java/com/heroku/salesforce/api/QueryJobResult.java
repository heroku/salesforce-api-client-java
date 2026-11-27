/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A page of CSV results of a query job.
 *
 * <p>Instances hold an open HTTP response and must be closed after use, for example with
 * try-with-resources.
 *
 * @see BulkApi#getQueryJobResults(QueryJobReference)
 * @see BulkApi#getMoreQueryJobResults(QueryJobResult)
 */
public final class QueryJobResult implements Closeable {
  private final QueryJobReference job;
  private final InputStream data;
  private final long numberOfRecords;
  private final String locator;
  private final Long maxRecords;

  QueryJobResult(
      QueryJobReference job,
      InputStream data,
      long numberOfRecords,
      @Nullable String locator,
      @Nullable Long maxRecords) {
    this.job = job;
    this.data = data;
    this.numberOfRecords = numberOfRecords;
    this.locator = locator;
    this.maxRecords = maxRecords;
  }

  /**
   * Returns the CSV data of this page, including the header row.
   *
   * @return A stream of CSV data.
   */
  @Nonnull
  public InputStream getData() {
    return data;
  }

  /**
   * Returns the number of records in this page.
   *
   * @return The number of records in this page.
   */
  public long getNumberOfRecords() {
    return numberOfRecords;
  }

  /**
   * Returns whether this is the last page of results.
   *
   * @return {@code true} if there are no more results, {@code false} otherwise.
   * @see BulkApi#getMoreQueryJobResults(QueryJobResult)
   */
  public boolean isDone() {
    return locator == null;
  }

  @Override
  public void close() throws IOException {
    data.close();
  }

  QueryJobReference getJob() {
    return job;
  }

  Optional<String> getLocator() {
    return Optional.ofNullable(locator);
  }

  Optional<Long> getMaxRecords() {
    return Optional.ofNullable(maxRecords);
  }
}
