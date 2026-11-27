/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SalesforceRestApiHttpClientStreamingTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final SalesforceRestApiHttpClient httpClient =
      new SalesforceRestApiHttpClient(
          URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void testStreamedResponseBody()
      throws IOException, RestApiErrorsException, RestApiException {
    StreamingRestApiRequest<InputStream, JsonRequestBody> request =
        new TestStreamingRestApiRequest(HttpMethod.GET, "query") {
          @Override
          public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
            return UriUtil.withPathSegmentsAndQueryParameter(
                baseUri,
                "q",
                "SELECT Name FROM Account",
                "services",
                "data",
                "v" + apiVersion,
                "query");
          }
        };

    try (InputStream body = httpClient.executeStreaming(request)) {
      String content = new String(body.readAllBytes(), StandardCharsets.UTF_8);
      assertThat(content, startsWith("{\"totalSize\":5,\"done\":true,"));
    }
  }

  @Test
  public void testErrorResponse() throws IOException, RestApiException {
    StreamingRestApiRequest<InputStream, JsonRequestBody> request =
        new TestStreamingRestApiRequest(
            HttpMethod.DELETE, "sobjects", "Account", "001JS0000000001YAA");

    try {
      httpClient.executeStreaming(request).close();
    } catch (RestApiErrorsException e) {
      assertThat(
          e.getApiErrors(),
          contains(
              pojo(RestApiError.class)
                  .withProperty("message", is(equalTo("entity is deleted")))
                  .withProperty("errorCode", is(equalTo("ENTITY_IS_DELETED")))
                  .withProperty("fields", is(empty()))));

      return;
    }

    Assertions.fail("Expected RestApiErrorsException!");
  }

  private static class TestStreamingRestApiRequest
      implements StreamingRestApiRequest<InputStream, JsonRequestBody> {
    private final HttpMethod httpMethod;
    private final String[] pathSegments;

    TestStreamingRestApiRequest(HttpMethod httpMethod, String... pathSegments) {
      this.httpMethod = httpMethod;
      this.pathSegments = pathSegments;
    }

    @Override
    public HttpMethod getHttpMethod() {
      return httpMethod;
    }

    @Override
    public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
      String[] segments = new String[pathSegments.length + 3];
      segments[0] = "services";
      segments[1] = "data";
      segments[2] = "v" + apiVersion;
      System.arraycopy(pathSegments, 0, segments, 3, pathSegments.length);
      return UriUtil.withPathSegments(baseUri, segments);
    }

    @Override
    public Optional<JsonRequestBody> getBody() {
      return Optional.empty();
    }

    @Override
    public InputStream processResponse(Map<String, String> headers, InputStream body) {
      return body;
    }
  }
}
