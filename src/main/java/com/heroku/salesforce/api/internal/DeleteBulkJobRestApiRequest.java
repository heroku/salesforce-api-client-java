/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.JsonElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public class DeleteBulkJobRestApiRequest extends JsonRestApiRequest<Void> {
  private final BulkJobType jobType;
  private final String jobId;

  public DeleteBulkJobRestApiRequest(BulkJobType jobType, String jobId) {
    this.jobType = jobType;
    this.jobId = jobId;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.DELETE;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return jobType.createUri(baseUri, apiVersion, jobId);
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }

  @Override
  public Void processResponse(
      int statusCode, Map<String, String> headers, JsonElement jsonResponseBody)
      throws RestApiErrorsException {
    if (statusCode == 204) {
      return null;
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(jsonResponseBody));
    }
  }
}
