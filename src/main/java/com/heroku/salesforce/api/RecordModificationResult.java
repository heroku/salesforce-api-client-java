/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Represents the result of a record modification such as a create, delete, or insert.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see DataApi#create(Record)
 * @see DataApi#update(Record)
 * @see DataApi#delete(String, String)
 * @see DataApi#commitUnitOfWork(UnitOfWork)
 */
@Immutable
public final class RecordModificationResult {
  private final String id;

  RecordModificationResult(String id) {
    this.id = id;
  }

  /**
   * Returns the id of the modified record.
   *
   * @return The id of the modified record.
   */
  @Nonnull
  public String getId() {
    return id;
  }
}
