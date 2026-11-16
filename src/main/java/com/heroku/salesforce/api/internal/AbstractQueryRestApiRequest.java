/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractQueryRestApiRequest extends JsonRestApiRequest<QueryRecordResult> {
  private final Gson gson = new Gson();

  @Override
  public final QueryRecordResult processResponse(
      int statusCode, Map<String, String> headers, JsonElement jsonRequestBody)
      throws RestApiErrorsException {

    if (statusCode != 200) {
      throw new RestApiErrorsException(ErrorResponseParser.parse(jsonRequestBody));
    } else {
      return parseQueryResult(jsonRequestBody.getAsJsonObject());
    }
  }

  private QueryRecordResult parseQueryResult(JsonObject body) {
    final boolean done = body.get("done").getAsBoolean();
    final long totalSize = body.get("totalSize").getAsLong();
    final List<RestApiRecord> records = new ArrayList<>();

    final String nextRecordsPath;
    if (body.get("nextRecordsUrl") == null || body.get("nextRecordsUrl").isJsonNull()) {
      nextRecordsPath = null;
    } else {
      nextRecordsPath = body.get("nextRecordsUrl").getAsString();
    }

    for (JsonElement jsonElement : body.get("records").getAsJsonArray()) {
      records.add(parseQueryRecord(jsonElement.getAsJsonObject()));
    }

    return new QueryRecordResult(totalSize, done, records, nextRecordsPath);
  }

  private RestApiRecord parseQueryRecord(JsonObject data) {
    final JsonObject attributesObject = data.get("attributes").getAsJsonObject();
    final Map<String, JsonPrimitive> attributes =
        gson.fromJson(attributesObject, new TypeToken<Map<String, JsonPrimitive>>() {}.getType());

    final Map<String, RestApiRecord.FieldValue> values = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
      if (entry.getKey().equals("attributes")) {
        continue;
      }

      if (entry.getValue().isJsonObject()) {
        JsonObject nestedJsonObject = entry.getValue().getAsJsonObject();
        if (nestedJsonObject.has("attributes")) {
          values.put(
              entry.getKey(), new RestApiRecord.FieldValue(parseQueryRecord(nestedJsonObject)));
        } else if (isQueryResult(nestedJsonObject)) {
          values.put(
              entry.getKey(), new RestApiRecord.FieldValue(parseQueryResult(nestedJsonObject)));
        } else {
          values.put(entry.getKey(), new RestApiRecord.FieldValue(nestedJsonObject));
        }
      } else if (!entry.getValue().isJsonNull()) {
        values.put(entry.getKey(), new RestApiRecord.FieldValue(entry.getValue()));
      }
    }

    return new RestApiRecord(attributes, values);
  }

  private static boolean isQueryResult(JsonObject object) {
    return object.has("records") && object.has("done") && object.has("totalSize");
  }
}
