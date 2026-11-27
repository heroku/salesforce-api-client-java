/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.JsonElement;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public class UploadBulkIngestJobDataRestApiRequest
    extends JsonResponseRestApiRequest<Void, CsvRequestBody> {
  private final String jobId;
  private final InputStream csv;

  public UploadBulkIngestJobDataRestApiRequest(String jobId, InputStream csv) {
    this.jobId = jobId;
    this.csv = csv;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PUT;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return BulkJobType.INGEST.createUri(baseUri, apiVersion, jobId, "batches");
  }

  @Override
  public Optional<CsvRequestBody> getBody() {
    return Optional.of(new CsvRequestBody(csv));
  }

  @Override
  public Void processResponse(
      int statusCode, Map<String, String> headers, JsonElement jsonResponseBody)
      throws RestApiErrorsException {
    if (statusCode == 201) {
      return null;
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(jsonResponseBody));
    }
  }
}
