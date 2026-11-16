/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.Map;
import java.util.TreeMap;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a Salesforce record.
 *
 * <p>Instances are immutable and therefore thread-safe.
 */
@Immutable
public sealed class Record extends RecordAccessor permits RecordWithSubQueryResults {
  private final TreeMap<String, FieldValue> fieldValues =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  Record(String type, Map<String, FieldValue> fieldValues) {
    super(type);
    this.fieldValues.putAll(fieldValues);
  }

  @Override
  TreeMap<String, FieldValue> getFieldValues() {
    return fieldValues;
  }
}
