/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GetBulkQueryJobResultsRestApiRequest
    implements StreamingRestApiRequest<BulkQueryJobResults, JsonRequestBody> {
  private final String jobId;
  private final Optional<String> locator;
  private final Optional<Long> maxRecords;

  public GetBulkQueryJobResultsRestApiRequest(
      String jobId, Optional<String> locator, Optional<Long> maxRecords) {
    this.jobId = jobId;
    this.locator = locator;
    this.maxRecords = maxRecords;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    Map<String, String> queryParameters = new LinkedHashMap<>();
    locator.ifPresent(value -> queryParameters.put("locator", value));
    maxRecords.ifPresent(value -> queryParameters.put("maxRecords", value.toString()));

    return BulkJobType.QUERY.createUri(baseUri, apiVersion, queryParameters, jobId, "results");
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }

  @Override
  public BulkQueryJobResults processResponse(Map<String, String> headers, InputStream body)
      throws RestApiException {
    String numberOfRecords = headers.get("Sforce-NumberOfRecords");
    if (numberOfRecords == null) {
      throw new RestApiException("Bulk query results are missing the record count!", null);
    }

    // The API signals the last page with the literal string "null" instead of omitting the header.
    String locator = headers.get("Sforce-Locator");
    if ("null".equals(locator)) {
      locator = null;
    }

    try {
      return new BulkQueryJobResults(body, Long.parseLong(numberOfRecords), locator);
    } catch (NumberFormatException e) {
      throw new RestApiException("Bulk query results have an invalid record count!", e);
    }
  }
}
