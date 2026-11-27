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

public class CreateBulkJobRestApiRequest extends JsonRestApiRequest<JsonObject> {
  private final BulkJobType jobType;
  private final JsonObject body;

  public CreateBulkJobRestApiRequest(BulkJobType jobType, JsonObject body) {
    this.jobType = jobType;
    this.body = body;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.POST;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return jobType.createUri(baseUri, apiVersion);
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
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
