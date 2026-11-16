/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class RestApi {
  private final URI orgDomainUrl;
  private final String apiVersion;
  private final String accessToken;
  private final String clientVersion;
  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

  public RestApi(URI orgDomainUrl, String apiVersion, String accessToken) {
    this.orgDomainUrl = orgDomainUrl;
    this.apiVersion = apiVersion;
    this.accessToken = accessToken;
    this.clientVersion = readVersionStringFromProperties().orElse("?.?.?-unknown");
  }

  public URI getOrgDomainUrl() {
    return orgDomainUrl;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public <T, A extends RestApiRequestBody, B> T execute(RestApiRequest<T, A, B> apiRequest)
      throws RestApiErrorsException, RestApiException, IOException {
    URI uri;
    try {
      uri = apiRequest.createUri(orgDomainUrl, apiVersion);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unexpected URISyntaxException!", e);
    }

    HttpRequest request =
        createBaseHttpRequest(apiRequest.getHttpMethod(), uri, apiRequest.getBody());

    HttpResponse<byte[]> response = send(request);

    Map<String, String> headers = new HashMap<>();
    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
      // This will drop all but the last value for duplicate headers. This is intentional and
      // consistent with the composite API that does not support multiple headers with the same
      // name.
      List<String> values = header.getValue();
      if (!values.isEmpty()) {
        headers.put(header.getKey(), values.get(values.size() - 1));
      }
    }

    byte[] bodyBytes = response.body();
    if (bodyBytes == null || bodyBytes.length == 0) {
      return apiRequest.processResponse(response.statusCode(), headers, null);
    }

    try {
      return apiRequest.processResponse(
          response.statusCode(), headers, apiRequest.parseBody(bodyBytes));
    } catch (BodyParsingException e) {
      throw new RestApiException("Could not parse API response!\n" + Arrays.toString(bodyBytes), e);
    }
  }

  public ByteBuffer downloadFile(String relativeUrl) throws URISyntaxException, IOException {
    URI uri = UriUtil.withPath(orgDomainUrl, relativeUrl);

    HttpRequest request = createBaseHttpRequest(HttpMethod.GET, uri, Optional.empty());

    return ByteBuffer.wrap(send(request).body());
  }

  private HttpResponse<byte[]> send(HttpRequest request) throws IOException {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while executing HTTP request!", e);
    }
  }

  private <A extends RestApiRequestBody> HttpRequest createBaseHttpRequest(
      HttpMethod method, URI uri, Optional<A> optionalBody) {

    HttpRequest.Builder builder = HttpRequest.newBuilder(uri);

    HttpRequest.BodyPublisher bodyPublisher =
        optionalBody
            .map(body -> HttpRequest.BodyPublishers.ofByteArray(body.getRequestContents()))
            .orElseGet(HttpRequest.BodyPublishers::noBody);

    optionalBody.ifPresent(body -> builder.header("Content-Type", body.getContentType()));

    switch (method) {
      case GET -> builder.GET();
      case DELETE -> builder.DELETE();
      case POST -> builder.POST(bodyPublisher);
      case PATCH -> builder.method("PATCH", bodyPublisher);
    }

    builder.header("Authorization", "Bearer " + accessToken);
    builder.header("Sforce-Call-Options", "client=salesforce-api-client:" + clientVersion);
    return builder.build();
  }

  private Optional<String> readVersionStringFromProperties() {
    final Properties properties = new Properties();
    try (final InputStream stream =
        RestApi.class.getResourceAsStream("/salesforce-api-client.properties")) {
      if (stream == null) {
        return Optional.empty();
      }

      properties.load(stream);
      return Optional.ofNullable(properties.getProperty("version"));
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
