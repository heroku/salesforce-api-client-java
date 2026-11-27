/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RestApiUpdateTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final SalesforceRestApiHttpClient httpClient =
      new SalesforceRestApiHttpClient(
          URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void update() throws RestApiErrorsException, IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("ReleaseDate__c", new JsonPrimitive("1980-05-21"));

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest("a00JS0000000005YAA", "Movie__c", values);

    ModifyRecordResult result = httpClient.execute(request);
    assertThat(result.getId(), is(equalTo("a00JS0000000005YAA")));
  }

  @Test
  public void updateWithMalformedId() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("ReleaseDate__c", new JsonPrimitive("1980-05-21"));

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest("a00JS0000000005IB1", "Movie__c", values);

    try {
      httpClient.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(
          error.getMessage(), equalTo("Record ID: id value of incorrect type: a00JS0000000005IB1"));

      assertThat(error.getErrorCode(), is(equalTo("MALFORMED_ID")));
      assertThat(error.getFields(), contains("Id"));

      return;
    }

    Assertions.fail("Expected exception!");
  }

  @Test
  public void updateWithInvalidField() throws IOException, RestApiException {
    Map<String, JsonElement> values = new HashMap<>();
    values.put("Color__c", new JsonPrimitive("Red"));

    UpdateRecordRestApiRequest request =
        new UpdateRecordRestApiRequest("a00JS0000000005IB1", "Movie__c", values);

    try {
      httpClient.execute(request);
    } catch (RestApiErrorsException e) {
      RestApiError error = e.getApiErrors().get(0);

      assertThat(
          error.getMessage(), equalTo("No such column 'Color__c' on sobject of type Movie__c"));

      assertThat(error.getErrorCode(), is(equalTo("INVALID_FIELD")));
      assertThat(error.getFields(), is(empty()));

      return;
    }

    Assertions.fail("Expected exception!");
  }
}
