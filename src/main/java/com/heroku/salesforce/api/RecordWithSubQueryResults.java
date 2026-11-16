/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a Salesforce record with optional sub query results.
 *
 * <p>Instances are immutable and therefore thread-safe.
 */
@Immutable
public final class RecordWithSubQueryResults extends Record {
  private final Map<String, RecordQueryResult> subQueryResults;

  RecordWithSubQueryResults(
      String type,
      Map<String, FieldValue> fieldValues,
      Map<String, RecordQueryResult> subQueryResults) {
    super(type, fieldValues);
    this.subQueryResults = subQueryResults;
  }

  /**
   * Returns the result of a sub query related to this record.
   *
   * <p>Records can have sub query results when the record is the result of a relationship query.
   *
   * @param objectName The object name of the sub query.
   * @return The result of a sub query related to this record.
   */
  @Nonnull
  public Optional<RecordQueryResult> getSubQueryResult(String objectName) {
    return Optional.ofNullable(subQueryResults.get(objectName));
  }
}
