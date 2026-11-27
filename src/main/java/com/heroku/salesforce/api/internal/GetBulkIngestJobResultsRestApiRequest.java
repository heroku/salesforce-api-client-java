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
import java.util.Map;
import java.util.Optional;

public class GetBulkIngestJobResultsRestApiRequest
    implements StreamingRestApiRequest<InputStream, JsonRequestBody> {
  private final String jobId;
  private final ResultType resultType;

  public GetBulkIngestJobResultsRestApiRequest(String jobId, ResultType resultType) {
    this.jobId = jobId;
    this.resultType = resultType;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return BulkJobType.INGEST.createUri(baseUri, apiVersion, jobId, resultType.pathSegment);
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }

  @Override
  public InputStream processResponse(Map<String, String> headers, InputStream body) {
    return body;
  }

  public enum ResultType {
    SUCCESSFUL("successfulResults"),
    FAILED("failedResults"),
    UNPROCESSED("unprocessedrecords");

    private final String pathSegment;

    ResultType(String pathSegment) {
      this.pathSegment = pathSegment;
    }
  }
}
