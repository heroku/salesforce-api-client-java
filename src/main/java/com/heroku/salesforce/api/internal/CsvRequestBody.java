/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.util.concurrent.atomic.AtomicBoolean;

public class CsvRequestBody implements RestApiRequestBody {
  private final InputStream inputStream;

  public CsvRequestBody(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public String getContentType() {
    return "text/csv";
  }

  @Override
  public HttpRequest.BodyPublisher getBodyPublisher() {
    // The HTTP client asks the supplier for a fresh stream whenever it resends the body, for
    // example
    // when following a 307 or 308 redirect. The caller's stream can only be read once, so handing
    // it
    // out again would silently send an empty body. Failing the request instead makes this visible.
    AtomicBoolean supplied = new AtomicBoolean(false);
    return HttpRequest.BodyPublishers.ofInputStream(() -> {
      if (supplied.getAndSet(true)) {
        throw new UncheckedIOException(
            new IOException("CSV request body cannot be sent more than once!"));
      }
      return inputStream;
    });
  }
}
