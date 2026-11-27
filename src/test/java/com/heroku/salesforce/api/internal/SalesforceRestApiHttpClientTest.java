/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SalesforceRestApiHttpClientTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final SalesforceRestApiHttpClient httpClient =
      new SalesforceRestApiHttpClient(
          URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void testUnexpectedResponse() throws IOException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Name FROM FruitVendor__c");

    try {
      httpClient.execute(queryRequest);
    } catch (RestApiException e) {
      assertThat(e.getMessage(), startsWith("Could not parse API error response!"));

      return;
    }

    Assertions.fail("Expected Exception!");
  }

  @Test
  public void testEmptyErrorResponse() {
    wireMock.stubFor(delete("/services/data/v53.0/sobjects/Account/001").willReturn(status(503)));

    RestApiException exception =
        Assertions.assertThrows(
            RestApiException.class,
            () -> httpClient.execute(new DeleteRecordRestApiRequest("Account", "001")));
    assertThat(exception.getMessage(), startsWith("Unexpected API error response!"));
  }

  @Test
  public void testNonArrayErrorResponse() {
    wireMock.stubFor(
        delete("/services/data/v53.0/sobjects/Account/001")
            .willReturn(aResponse().withStatus(502).withBody("{\"error\": \"Bad Gateway\"}")));

    RestApiException exception =
        Assertions.assertThrows(
            RestApiException.class,
            () -> httpClient.execute(new DeleteRecordRestApiRequest("Account", "001")));
    assertThat(exception.getMessage(), startsWith("Unexpected API error response!"));
  }

  @Test
  public void testMalformedErrorResponseEntry() {
    wireMock.stubFor(
        delete("/services/data/v53.0/sobjects/Account/001")
            .willReturn(aResponse().withStatus(400).withBody("[{\"errorCode\": \"FOO\"}]")));

    RestApiException exception =
        Assertions.assertThrows(
            RestApiException.class,
            () -> httpClient.execute(new DeleteRecordRestApiRequest("Account", "001")));
    assertThat(exception.getMessage(), startsWith("Unexpected API error response!"));
  }

  @Test
  public void testHeaderLookupIsCaseInsensitive()
      throws IOException, RestApiErrorsException, RestApiException {
    JsonRestApiRequest<String> request =
        new JsonRestApiRequest<>() {
          @Override
          public HttpMethod getHttpMethod() {
            return HttpMethod.DELETE;
          }

          @Override
          public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
            return UriUtil.withPathSegments(
                baseUri,
                "services",
                "data",
                "v" + apiVersion,
                "sobjects",
                "Account",
                "001JS0000000002YAA");
          }

          @Override
          public Optional<JsonRequestBody> getBody() {
            return Optional.empty();
          }

          @Override
          public String processResponse(
              int statusCode, Map<String, String> headers, JsonElement body) {
            return headers.get("SFORCE-LIMIT-INFO");
          }
        };

    assertThat(httpClient.execute(request), is(equalTo("api-usage=17/100000")));
  }

  @Test
  public void testApiVersionGetter() {
    assertThat(httpClient.getApiVersion(), is(equalTo("53.0")));
  }

  @Test
  public void testAccessTokenGetter() {
    assertThat(httpClient.getAccessToken(), is(equalTo("00DJS0000000123!AQ0AQ.token")));
  }
}
