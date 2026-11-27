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
 * A reference to a bulk ingest job.
 *
 * <p>Instances are immutable and therefore thread-safe.
 *
 * @see BulkApi#createIngestJob(String, IngestOperation)
 */
@Immutable
public final class IngestJobReference {
  private final String id;

  private IngestJobReference(String id) {
    this.id = id;
  }

  /**
   * Creates a reference to an existing ingest job, for example from an id that was stored earlier.
   *
   * @param id The id of the ingest job.
   * @return A reference to the ingest job with the given id.
   */
  @Nonnull
  public static IngestJobReference of(String id) {
    return new IngestJobReference(Objects.requireNonNull(id));
  }

  /**
   * Returns the id of the referenced ingest job.
   *
   * @return The id of the referenced ingest job.
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
    IngestJobReference that = (IngestJobReference) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "IngestJobReference{" + "id='" + id + '\'' + '}';
  }
}
