/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A reference to a bulk query job.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see BulkApi#createQueryJob(String, QueryOperation)
 */
@Immutable
public final class QueryJobReference {
  private final String id;

  private QueryJobReference(String id) {
    this.id = id;
  }

  /**
   * Creates a reference to an existing query job, for example from an id that was stored earlier.
   *
   * @param id The id of the query job.
   * @return A reference to the query job with the given id.
   */
  @Nonnull
  public static QueryJobReference of(String id) {
    return new QueryJobReference(Objects.requireNonNull(id));
  }

  /**
   * Returns the id of the referenced query job.
   *
   * @return The id of the referenced query job.
   */
  @Nonnull
  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryJobReference that = (QueryJobReference) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "QueryJobReference{" + "id='" + id + '\'' + '}';
  }
}
