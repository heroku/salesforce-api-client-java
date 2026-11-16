/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.heroku.salesforce.api.internal.JsonRestApiRequest;
import com.heroku.salesforce.api.internal.ModifyRecordResult;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a UnitOfWork. Instances can be created via {@link DataApi#newUnitOfWorkBuilder()}.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see DataApi#newUnitOfWorkBuilder()
 * @see DataApi#commitUnitOfWork(UnitOfWork)
 */
@Immutable
public final class UnitOfWork {
  // Order is important, don't replace LinkedHashMap without verifying the new implementation
  // also preserves insertion order!
  private final LinkedHashMap<String, JsonRestApiRequest<ModifyRecordResult>> subrequests;

  UnitOfWork(LinkedHashMap<String, JsonRestApiRequest<ModifyRecordResult>> subrequests) {
    this.subrequests = new LinkedHashMap<>(subrequests);
  }

  Map<String, JsonRestApiRequest<ModifyRecordResult>> getSubrequests() {
    return Collections.unmodifiableMap(subrequests);
  }
}
