/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class JsonRequestBody implements RestApiRequestBody {
  private final JsonElement jsonElement;

  public JsonRequestBody(JsonElement jsonElement) {
    this.jsonElement = jsonElement;
  }

  public JsonElement getJsonElement() {
    return jsonElement;
  }

  @Override
  public String getContentType() {
    return "application/json; charset=UTF-8";
  }

  @Override
  public HttpRequest.BodyPublisher getBodyPublisher() {
    return HttpRequest.BodyPublishers.ofByteArray(
        new Gson().toJson(this.jsonElement).getBytes(StandardCharsets.UTF_8));
  }
}
