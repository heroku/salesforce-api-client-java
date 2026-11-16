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

public class DeleteRecordRestApiRequest extends JsonRestApiRequest<ModifyRecordResult> {
  private final String type;
  private final String id;

  public DeleteRecordRestApiRequest(String type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.DELETE;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return UriUtil.withPathSegments(
        baseUri, "services", "data", "v" + apiVersion, "sobjects", type, id);
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
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
