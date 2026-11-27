/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

/**
 * The processing state of an ingest job.
 *
 * @see IngestJobInfo#getState()
 */
public enum IngestJobState {
  /** The job was created and is accepting data. */
  OPEN("Open"),

  /** All data was uploaded and the job is queued for processing. */
  UPLOAD_COMPLETE("UploadComplete"),

  /** The job is being processed. */
  IN_PROGRESS("InProgress"),

  /** The job was processed. Individual records might still have failed. */
  JOB_COMPLETE("JobComplete"),

  /** The job was aborted. */
  ABORTED("Aborted"),

  /** The job failed. See {@link IngestJobInfo#getErrorMessage()} for details. */
  FAILED("Failed");

  private final String apiValue;

  IngestJobState(String apiValue) {
    this.apiValue = apiValue;
  }

  String getApiValue() {
    return apiValue;
  }
}
