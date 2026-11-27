/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.io.InputStream;
import java.util.Optional;

public final class BulkQueryJobResults {
  private final InputStream data;
  private final long numberOfRecords;
  private final String locator;

  BulkQueryJobResults(InputStream data, long numberOfRecords, String locator) {
    this.data = data;
    this.numberOfRecords = numberOfRecords;
    this.locator = locator;
  }

  public InputStream getData() {
    return data;
  }

  public long getNumberOfRecords() {
    return numberOfRecords;
  }

  public Optional<String> getLocator() {
    return Optional.ofNullable(locator);
  }
}
