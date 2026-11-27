/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public enum BulkJobType {
  INGEST("ingest"),
  QUERY("query");

  private final String pathSegment;

  BulkJobType(String pathSegment) {
    this.pathSegment = pathSegment;
  }

  URI createUri(URI baseUri, String apiVersion, String... segments) throws URISyntaxException {
    return createUri(baseUri, apiVersion, Map.of(), segments);
  }

  URI createUri(
      URI baseUri, String apiVersion, Map<String, String> queryParameters, String... segments)
      throws URISyntaxException {
    String[] allSegments = new String[segments.length + 5];
    allSegments[0] = "services";
    allSegments[1] = "data";
    allSegments[2] = "v" + apiVersion;
    allSegments[3] = "jobs";
    allSegments[4] = pathSegment;
    System.arraycopy(segments, 0, allSegments, 5, segments.length);

    return UriUtil.withPathSegmentsAndQueryParameters(baseUri, queryParameters, allSegments);
  }
}
