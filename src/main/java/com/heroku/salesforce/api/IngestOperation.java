/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/**
 * The operation an ingest job performs on the uploaded records.
 *
 * @see BulkApi#createIngestJob(String, IngestOperation)
 */
public enum IngestOperation {
  /** Creates new records. */
  INSERT("insert"),

  /** Updates existing records, identified by their Id. */
  UPDATE("update"),

  /**
   * Creates new records or updates existing ones, identified by an external ID field. Requires
   * {@link IngestJobOptionsBuilder#withExternalIdFieldName(String)}.
   */
  UPSERT("upsert"),

  /** Moves records, identified by their Id, to the Recycle Bin. */
  DELETE("delete"),

  /** Permanently deletes records, identified by their Id. */
  HARD_DELETE("hardDelete");

  private final String apiValue;

  IngestOperation(String apiValue) {
    this.apiValue = apiValue;
  }

  String getApiValue() {
    return apiValue;
  }
}
