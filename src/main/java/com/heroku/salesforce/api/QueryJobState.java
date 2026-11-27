/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/**
 * The processing state of a query job.
 *
 * @see QueryJobInfo#getState()
 */
public enum QueryJobState {
  /** The job was created and is queued for processing. */
  UPLOAD_COMPLETE("UploadComplete"),

  /** The job is being processed. */
  IN_PROGRESS("InProgress"),

  /** The job was processed and its results are available. */
  JOB_COMPLETE("JobComplete"),

  /** The job was aborted. */
  ABORTED("Aborted"),

  /** The job failed. See {@link QueryJobInfo#getErrorMessage()} for details. */
  FAILED("Failed");

  private final String apiValue;

  QueryJobState(String apiValue) {
    this.apiValue = apiValue;
  }

  String getApiValue() {
    return apiValue;
  }
}
