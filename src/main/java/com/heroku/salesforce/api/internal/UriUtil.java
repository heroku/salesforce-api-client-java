/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

final class UriUtil {

  static URI withPathSegments(URI baseUri, String... segments) throws URISyntaxException {
    return withPath(baseUri, encodedPath(segments));
  }

  static URI withPathSegmentsAndQueryParameter(
      URI baseUri, String parameterName, String parameterValue, String... segments)
      throws URISyntaxException {
    return build(
        baseUri, encodedPath(segments), encode(parameterName) + "=" + encode(parameterValue));
  }

  static URI withPath(URI baseUri, String rawPath) throws URISyntaxException {
    return build(baseUri, rawPath, null);
  }

  static URI relative(URI uri) throws URISyntaxException {
    StringBuilder builder = new StringBuilder(uri.getRawPath());

    if (uri.getRawQuery() != null) {
      builder.append('?').append(uri.getRawQuery());
    }

    if (uri.getRawFragment() != null) {
      builder.append('#').append(uri.getRawFragment());
    }

    return new URI(builder.toString());
  }

  private static URI build(URI baseUri, String rawPath, String rawQuery) throws URISyntaxException {
    StringBuilder builder = new StringBuilder();
    builder.append(baseUri.getScheme()).append("://").append(baseUri.getRawAuthority());

    if (!rawPath.startsWith("/")) {
      builder.append('/');
    }
    builder.append(rawPath);

    if (rawQuery != null) {
      builder.append('?').append(rawQuery);
    }

    return new URI(builder.toString());
  }

  private static String encodedPath(String... segments) {
    return Arrays.stream(segments).map(UriUtil::encode).collect(Collectors.joining("/", "/", ""));
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private UriUtil() {}
}
