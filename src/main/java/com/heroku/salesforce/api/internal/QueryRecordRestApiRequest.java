/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class QueryRecordRestApiRequest extends AbstractQueryRestApiRequest {
  private final String soql;

  public QueryRecordRestApiRequest(String soql) {
    this.soql = soql;
  }

  @Override
  public HttpMethod getHttpMethod() {
    return HttpMethod.GET;
  }

  @Override
  public URI createUri(URI baseUri, String apiVersion) throws URISyntaxException {
    return UriUtil.withPathSegmentsAndQueryParameter(
        baseUri, "q", soql, "services", "data", "v" + apiVersion, "query");
  }

  @Override
  public Optional<JsonRequestBody> getBody() {
    return Optional.empty();
  }
}
