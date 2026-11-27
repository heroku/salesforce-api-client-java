/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public class UpdateBulkJobStateRestApiRequest extends JsonRestApiRequest<JsonObject> {
  private final BulkJobType jobType;
  private final String jobId;
  private final String state;

  public UpdateBulkJobStateRestApiRequest(BulkJobType jobType, String jobId, String state) {
    this.jobType = jobType;
    this.jobId = jobId;
    this.state = state;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PATCH;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return jobType.createUri(baseUri, apiVersion, jobId);
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    JsonObject body = new JsonObject();
    body.addProperty("state", state);
    return Optional.of(new JsonRequestBody(body));
  }

  @Override
  public JsonObject processResponse(
      int statusCode, Map<String, String> headers, JsonElement jsonResponseBody)
      throws RestApiErrorsException {
    if (statusCode == 200) {
      return jsonResponseBody.getAsJsonObject();
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(jsonResponseBody));
    }
  }
}
