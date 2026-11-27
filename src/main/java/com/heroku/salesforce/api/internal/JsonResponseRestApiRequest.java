/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public abstract class JsonResponseRestApiRequest<T, A extends RestApiRequestBody>
    implements RestApiRequest<T, A, JsonElement> {
  @Override
  public JsonElement parseBody(byte[] body) throws BodyParsingException {
    try {
      return new Gson()
          .fromJson(new InputStreamReader(new ByteArrayInputStream(body)), JsonElement.class);
    } catch (JsonSyntaxException e) {
      throw new BodyParsingException(e);
    }
  }
}
