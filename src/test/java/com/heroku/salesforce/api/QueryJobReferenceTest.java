/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class QueryJobReferenceTest {

  @Test
  public void testOf() {
    assertThat(
        QueryJobReference.of("7507Q000009f8jvQAA").getId(), is(equalTo("7507Q000009f8jvQAA")));
  }

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(QueryJobReference.class).withNonnullFields("id").verify();
  }
}
