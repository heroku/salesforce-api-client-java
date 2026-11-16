/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.heroku.salesforce.api.internal.RestApiRecordBuilder.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.Map;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class RestApiRecordTest {

  @Test
  public void testValuesAndAttributesAreUnmodified() {
    Map<String, JsonPrimitive> values = new HashMap<>();
    values.put("foo", new JsonPrimitive("bar"));
    values.put("bar", new JsonPrimitive("baz"));

    RestApiRecord record =
        new RestApiRecord(attributes(), fields(field("foo", "bar"), field("bar", "baz")));
    assertThat(record.getAttributes().size(), is(equalTo(0)));
    assertThat(record.getValues().get("foo").getJsonData().getAsString(), is(equalTo("bar")));
    assertThat(record.getValues().get("bar").getJsonData().getAsString(), is(equalTo("baz")));
  }

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

    EqualsVerifier.forClass(RestApiRecord.class)
        .withPrefabValues(RestApiRecord.class, red, blue)
        .verify();
  }
}
