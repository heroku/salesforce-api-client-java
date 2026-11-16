/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * References a record that will be created, deleted or modified in the future. It can be used to
 * insert records that reference other records that are be created in the same {@link UnitOfWork}
 * transaction.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see UnitOfWorkBuilder#registerCreate(Record)
 * @see UnitOfWorkBuilder#registerUpdate(Record)
 * @see UnitOfWorkBuilder#registerDelete(String, String)
 */
@Immutable
public final class ReferenceId {
  private final String id;

  ReferenceId(String id) {
    this.id = id;
  }

  String toApiString() {
    return String.format("@{%s.id}", id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReferenceId that = (ReferenceId) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
