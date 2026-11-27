/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

public final class SalesforceRestApiHttpClient {
  private final URI orgDomainUrl;
  private final String apiVersion;
  private final String accessToken;
  private final String clientVersion;
  private final HttpClient httpClient =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

  public SalesforceRestApiHttpClient(URI orgDomainUrl, String apiVersion, String accessToken) {
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

    HttpResponse<byte[]> response = send(request, HttpResponse.BodyHandlers.ofByteArray());

    Map<String, String> headers = singleValueHeaders(response);

    byte[] bodyBytes = response.body();
    if (response.statusCode() / 100 != 2) {
      throw new RestApiErrorsException(
          parseErrorResponse(bodyBytes == null ? new byte[0] : bodyBytes));
    }

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

    return ByteBuffer.wrap(send(request, HttpResponse.BodyHandlers.ofByteArray()).body());
  }

  public <T, A extends RestApiRequestBody> T executeStreaming(
      StreamingRestApiRequest<T, A> apiRequest)
      throws RestApiErrorsException, RestApiException, IOException {
    URI uri;
    try {
      uri = apiRequest.createUri(orgDomainUrl, apiVersion);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Unexpected URISyntaxException!", e);
    }

    HttpRequest request =
        createBaseHttpRequest(apiRequest.getHttpMethod(), uri, apiRequest.getBody());

    HttpResponse<InputStream> response = send(request, HttpResponse.BodyHandlers.ofInputStream());

    if (response.statusCode() / 100 != 2) {
      byte[] bodyBytes;
      try (InputStream body = response.body()) {
        bodyBytes = body.readAllBytes();
      }

      throw new RestApiErrorsException(parseErrorResponse(bodyBytes));
    }

    try {
      return apiRequest.processResponse(singleValueHeaders(response), response.body());
    } catch (RestApiException | RuntimeException e) {
      response.body().close();
      throw e;
    }
  }

  private static List<RestApiError> parseErrorResponse(byte[] bodyBytes) throws RestApiException {
    String body = new String(bodyBytes, StandardCharsets.UTF_8);

    JsonElement json;
    try {
      json = JsonParser.parseString(body);
    } catch (JsonParseException e) {
      throw new RestApiException("Could not parse API error response!\n" + body, e);
    }

    if (!json.isJsonArray()) {
      throw new RestApiException("Unexpected API error response!\n" + body, null);
    }

    // The parser assumes the documented error shape and throws unchecked exceptions for anything
    // else, such as entries without a message or error code.
    try {
      return ErrorResponseParser.parse(json);
    } catch (IllegalStateException | NullPointerException | UnsupportedOperationException e) {
      throw new RestApiException("Unexpected API error response!\n" + body, e);
    }
  }

  private static Map<String, String> singleValueHeaders(HttpResponse<?> response) {
    // HTTP header names are case-insensitive and HTTP/2 transmits them in lowercase. Lookups must
    // work regardless of the protocol version and the casing used by the server.
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
      // This will drop all but the last value for duplicate headers. This is intentional and
      // consistent with the composite API that does not support multiple headers with the same
      // name.
      List<String> values = header.getValue();
      if (!values.isEmpty()) {
        headers.put(header.getKey(), values.get(values.size() - 1));
      }
    }

    return headers;
  }

  private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler)
      throws IOException {
    try {
      return httpClient.send(request, bodyHandler);
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
            .map(RestApiRequestBody::getBodyPublisher)
            .orElseGet(HttpRequest.BodyPublishers::noBody);

    optionalBody.ifPresent(body -> builder.header("Content-Type", body.getContentType()));

    // Streamed bodies, such as Bulk API CSV uploads, have no known length. Over http://, the JDK
    // client sends the first request as an HTTP/1.1 request with an "Upgrade: h2c" header, and
    // servers such as Jetty reset the stream when that request carries a body of unknown length.
    // Over https://, HTTP/2 is negotiated during the TLS handshake and this problem does not occur.
    // This applies to every scheme anyway, since HTTP/1.1 sends the body chunked and works with
    // any server. The cost is that uploads to Salesforce do not use HTTP/2.
    if (bodyPublisher.contentLength() < 0) {
      builder.version(HttpClient.Version.HTTP_1_1);
    }

    switch (method) {
      case GET -> builder.GET();
      case DELETE -> builder.DELETE();
      case POST -> builder.POST(bodyPublisher);
      case PUT -> builder.PUT(bodyPublisher);
      case PATCH -> builder.method("PATCH", bodyPublisher);
    }

    builder.header("Authorization", "Bearer " + accessToken);
    builder.header("Sforce-Call-Options", "client=salesforce-api-client:" + clientVersion);
    return builder.build();
  }

  private Optional<String> readVersionStringFromProperties() {
    final Properties properties = new Properties();
    try (final InputStream stream =
        SalesforceRestApiHttpClient.class.getResourceAsStream(
            "/salesforce-api-client.properties")) {
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
