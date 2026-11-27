/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/**
 * The column delimiter used in the CSV data of a bulk job.
 *
 * @see IngestJobOptionsBuilder#withColumnDelimiter(ColumnDelimiter)
 * @see QueryJobOptionsBuilder#withColumnDelimiter(ColumnDelimiter)
 */
public enum ColumnDelimiter {
  /** Backquote character (`). */
  BACKQUOTE("BACKQUOTE"),

  /** Caret character (^). */
  CARET("CARET"),

  /** Comma character (,). */
  COMMA("COMMA"),

  /** Pipe character (|). */
  PIPE("PIPE"),

  /** Semicolon character (;). */
  SEMICOLON("SEMICOLON"),

  /** Tab character. */
  TAB("TAB");

  private final String apiValue;

  ColumnDelimiter(String apiValue) {
    this.apiValue = apiValue;
  }

  String getApiValue() {
    return apiValue;
  }
}
