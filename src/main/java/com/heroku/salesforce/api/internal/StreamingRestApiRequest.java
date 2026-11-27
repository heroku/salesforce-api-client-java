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

/**
 * A REST API request whose successful response body is consumed as a stream instead of being read
 * into memory. Error responses are read and parsed by {@link SalesforceRestApiHttpClient} and never
 * reach {@link #processResponse(Map, InputStream)}.
 */
public interface StreamingRestApiRequest<T, A extends RestApiRequestBody> {
  HttpMethod getHttpMethod();

  URI createUri(URI baseUri, String apiVersion) throws URISyntaxException;

  Optional<A> getBody();

  /**
   * Processes a successful response. Ownership of the body stream passes to the returned value
   * unless this method throws, in which case {@link SalesforceRestApiHttpClient} closes the stream.
   */
  T processResponse(Map<String, String> headers, InputStream body) throws RestApiException;
}
