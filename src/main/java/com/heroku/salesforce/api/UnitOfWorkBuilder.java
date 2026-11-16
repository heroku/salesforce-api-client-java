/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.heroku.salesforce.api.internal.DeleteRecordRestApiRequest;
import com.heroku.salesforce.api.internal.JsonRestApiRequest;
import com.heroku.salesforce.api.internal.ModifyRecordResult;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/**
 * Builder for {@link UnitOfWork}.
 *
 * @see DataApi#newUnitOfWorkBuilder()
 */
public final class UnitOfWorkBuilder {
  // Order is important, don't replace LinkedHashMap without verifying the new implementation
  // also preserves insertion order!
  private final LinkedHashMap<String, JsonRestApiRequest<ModifyRecordResult>> subrequests =
      new LinkedHashMap<>();

  private final AtomicInteger nextReferenceId = new AtomicInteger(0);

  UnitOfWorkBuilder() {}

  /**
   * Registers a record creation for the {@link UnitOfWork} and returns a {@link ReferenceId} that
   * can be used to refer to the created record in subsequent operations in this UnitOfWork.
   *
   * @param record The record to create.
   * @return The ReferenceId for the created record.
   */
  @Nonnull
  public ReferenceId registerCreate(Record record) {
    String referenceId = nextReferenceId();
    subrequests.put(referenceId, DataApi.apiRequestForCreate(record));
    return new ReferenceId(referenceId);
  }

  /**
   * Registers a record update for the {@link UnitOfWork} and returns a {@link ReferenceId} that can
   * be used to refer to the updated record in subsequent operations in this UnitOfWork.
   *
   * @param record The record to update.
   * @throws IllegalArgumentException If the {@link Record} does not have an Id field.
   * @return The ReferenceId for the updated record.
   */
  @Nonnull
  public ReferenceId registerUpdate(Record record) {
    String referenceId = nextReferenceId();
    subrequests.put(referenceId, DataApi.apiRequestForUpdate(record));
    return new ReferenceId(referenceId);
  }

  /**
   * Registers a deletion of an existing record of the given type and id.
   *
   * @param type The object type of the record to delete.
   * @param id The id of the record to delete.
   * @return The ReferenceId for the deleted record.
   */
  @Nonnull
  public ReferenceId registerDelete(String type, String id) {
    String referenceId = nextReferenceId();
    subrequests.put(referenceId, new DeleteRecordRestApiRequest(type, id));
    return new ReferenceId(referenceId);
  }

  /**
   * Returns a new and immutable {@link UnitOfWork} instance based on the information stored in this
   * builder.
   *
   * @return The new {@link UnitOfWork} instance.
   */
  @Nonnull
  public UnitOfWork build() {
    return new UnitOfWork(subrequests);
  }

  private String nextReferenceId() {
    return "referenceId" + nextReferenceId.getAndIncrement();
  }
}
