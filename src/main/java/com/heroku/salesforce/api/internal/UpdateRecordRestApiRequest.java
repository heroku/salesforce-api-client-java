/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpdateRecordRestApiRequest extends JsonRestApiRequest<ModifyRecordResult> {
  private final String id;
  private final String type;
  private final Map<String, JsonElement> values;

  public UpdateRecordRestApiRequest(String id, String type, Map<String, JsonElement> values) {
    this.id = id;
    this.type = type;
    this.values = new HashMap<>(values);
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.PATCH;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return UriUtil.withPathSegments(
        baseUri, "services", "data", "v" + apiVersion, "sobjects", this.type, this.id);
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.of(new JsonRequestBody(new Gson().toJsonTree(values)));
  }

  @Override
  public ModifyRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement jsonRequestBody)
      throws RestApiErrorsException {
    if (statusCode == 204) {
      return new ModifyRecordResult(id);
    } else {
      throw new RestApiErrorsException(ErrorResponseParser.parse(jsonRequestBody));
    }
  }
}
