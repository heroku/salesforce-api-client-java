/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.heroku.salesforce.api.internal.QueryRecordResult;
import com.heroku.salesforce.api.internal.RestApi;
import com.heroku.salesforce.api.internal.RestApiRecord;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

final class BinaryFieldUtil {
  static boolean isBinaryField(String objectType, String fieldName) {
    Map<String, List<String>> mapping = new HashMap<>();
    mapping.put("ContentVersion", Collections.singletonList("VersionData"));

    return Optional.ofNullable(mapping.get(objectType))
        .map(fieldList -> fieldList.contains(fieldName))
        .orElse(false);
  }

  static RecordQueryResult convert(QueryRecordResult queryRecordResult, RestApi restApi)
      throws URISyntaxException, IOException {
    List<RecordWithSubQueryResults> records = new ArrayList<>();
    for (RestApiRecord record : queryRecordResult.getRecords()) {
      records.add(convert(record, restApi));
    }

    return new RecordQueryResult(
        queryRecordResult.isDone(),
        queryRecordResult.getTotalSize(),
        queryRecordResult.getNextRecordsPath(),
        records);
  }

  static RecordWithSubQueryResults convert(RestApiRecord record, RestApi restApi)
      throws URISyntaxException, IOException {
    final String recordObjectType = record.getAttributes().get("type").getAsString();

    final Map<String, FieldValue> fieldValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    final Map<String, RecordQueryResult> subQueryResults = new HashMap<>();

    for (Map.Entry<String, RestApiRecord.FieldValue> fieldEntry : record.getValues().entrySet()) {
      RestApiRecord.FieldValue value = fieldEntry.getValue();

      if (value.isJsonData()) {
        if (isBinaryField(recordObjectType, fieldEntry.getKey())) {
          ByteBuffer data = restApi.downloadFile(value.getJsonData().getAsString());
          fieldValues.put(fieldEntry.getKey(), new FieldValue(data));
        } else {
          fieldValues.put(fieldEntry.getKey(), new FieldValue(value.getJsonData()));
        }
      } else if (value.isRecordData()) {
        fieldValues.put(
            fieldEntry.getKey(), new FieldValue(convert(value.getRecordData(), restApi)));
      } else if (value.isQueryRecordResult()) {
        subQueryResults.put(fieldEntry.getKey(), convert(value.getQueryRecordResult(), restApi));
      }
    }

    return new RecordWithSubQueryResults(recordObjectType, fieldValues, subQueryResults);
  }

  static Map<String, JsonElement> convert(Map<String, FieldValue> fieldValues2) {
    Map<String, JsonElement> fieldValues = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    for (Map.Entry<String, FieldValue> entry : fieldValues2.entrySet()) {
      if (entry.getValue().isBinaryData()) {
        fieldValues.put(
            entry.getKey(),
            new JsonPrimitive(
                Base64.getEncoder().encodeToString(entry.getValue().getBinaryData().array())));
      } else {
        fieldValues.put(entry.getKey(), entry.getValue().getJsonData());
      }
    }

    return fieldValues;
  }

  private BinaryFieldUtil() {}
}
