/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/**
 * The operation of a query job.
 *
 * @see BulkApi#createQueryJob(String, QueryOperation)
 */
public enum QueryOperation {
  /** Returns records that are not deleted. */
  QUERY("query"),

  /** Returns records including deleted, merged and archived records. */
  QUERY_ALL("queryAll");

  private final String apiValue;

  QueryOperation(String apiValue) {
    this.apiValue = apiValue;
  }

  String getApiValue() {
    return apiValue;
  }
}
