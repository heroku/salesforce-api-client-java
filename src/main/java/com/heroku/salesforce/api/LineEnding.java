/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/**
 * The line ending used in the CSV data of a bulk job.
 *
 * @see IngestJobOptionsBuilder#withLineEnding(LineEnding)
 * @see QueryJobOptionsBuilder#withLineEnding(LineEnding)
 */
public enum LineEnding {
  /** Linefeed character. */
  LF("LF"),

  /** Carriage return character followed by a linefeed character. */
  CRLF("CRLF");

  private final String apiValue;

  LineEnding(String apiValue) {
    this.apiValue = apiValue;
  }

  String getApiValue() {
    return apiValue;
  }
}
