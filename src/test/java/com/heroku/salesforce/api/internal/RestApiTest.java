/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RestApiTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final RestApi restApi =
      new RestApi(URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void testUnexpectedResponse() throws IOException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Name FROM FruitVendor__c");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiException e) {
      assertThat(e.getMessage(), startsWith("Could not parse API response!"));

      return;
    }

    Assertions.fail("Expected Exception!");
  }

  @Test
  public void testApiVersionGetter() {
    assertThat(restApi.getApiVersion(), is(equalTo("53.0")));
  }

  @Test
  public void testAccessTokenGetter() {
    assertThat(restApi.getAccessToken(), is(equalTo("00DJS0000000123!AQ0AQ.token")));
  }
}
