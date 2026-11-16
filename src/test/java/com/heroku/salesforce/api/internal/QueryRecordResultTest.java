/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.heroku.salesforce.api.internal.RestApiRecordBuilder.*;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class QueryRecordResultTest {

  @Test
  public void testEqualsAndHashCode() {
    RestApiRecord red =
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000011YAA")),
            fields(field("Name", "Acme")));

    RestApiRecord blue =
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000013YAA")),
            fields(field("Name", "Sample Account for Entitlements")));

    EqualsVerifier.forClass(QueryRecordResult.class)
        .withPrefabValues(RestApiRecord.class, red, blue)
        .verify();
  }
}
