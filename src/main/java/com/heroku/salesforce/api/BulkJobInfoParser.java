/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

final class BulkJobInfoParser {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private BulkJobInfoParser() {}

  static IngestJobReference parseIngestJobReference(JsonObject json) throws DataApiException {
    return IngestJobReference.of(getString(json, "id"));
  }

  static IngestJobInfo parseIngestJobInfo(JsonObject json) throws DataApiException {
    return new IngestJobInfo(
        parseIngestJobReference(json),
        getString(json, "object"),
        getEnum(json, "operation", IngestOperation.values(), IngestOperation::getApiValue),
        getEnum(json, "state", IngestJobState.values(), IngestJobState::getApiValue),
        getOptionalString(json, "externalIdFieldName"),
        getEnum(json, "columnDelimiter", ColumnDelimiter.values(), ColumnDelimiter::getApiValue),
        getEnum(json, "lineEnding", LineEnding.values(), LineEnding::getApiValue),
        getLong(json, "numberRecordsProcessed"),
        getLong(json, "numberRecordsFailed"),
        (int) getLong(json, "retries"),
        getOptionalString(json, "errorMessage"),
        getString(json, "createdById"),
        getDateTime(json, "createdDate"),
        getDateTime(json, "systemModstamp"),
        getDuration(json, "totalProcessingTime"),
        getDuration(json, "apiActiveProcessingTime"),
        getDuration(json, "apexProcessingTime"));
  }

  static QueryJobReference parseQueryJobReference(JsonObject json) throws DataApiException {
    return QueryJobReference.of(getString(json, "id"));
  }

  static QueryJobInfo parseQueryJobInfo(JsonObject json) throws DataApiException {
    return new QueryJobInfo(
        parseQueryJobReference(json),
        getString(json, "object"),
        getEnum(json, "operation", QueryOperation.values(), QueryOperation::getApiValue),
        getEnum(json, "state", QueryJobState.values(), QueryJobState::getApiValue),
        getEnum(json, "columnDelimiter", ColumnDelimiter.values(), ColumnDelimiter::getApiValue),
        getEnum(json, "lineEnding", LineEnding.values(), LineEnding::getApiValue),
        getLong(json, "numberRecordsProcessed"),
        (int) getLong(json, "retries"),
        getOptionalString(json, "errorMessage"),
        getString(json, "createdById"),
        getDateTime(json, "createdDate"),
        getDateTime(json, "systemModstamp"),
        getDuration(json, "totalProcessingTime"));
  }

  private static String getString(JsonObject json, String name) throws DataApiException {
    String value = getOptionalString(json, name);
    if (value == null) {
      throw new DataApiException("Bulk job info is missing the '" + name + "' field!");
    }

    return value;
  }

  private static String getOptionalString(JsonObject json, String name) throws DataApiException {
    JsonElement element = json.get(name);
    if (element == null || element.isJsonNull()) {
      return null;
    }

    if (!element.isJsonPrimitive()) {
      throw new DataApiException("Bulk job info field '" + name + "' is not a string!");
    }

    return element.getAsString();
  }

  private static long getLong(JsonObject json, String name) throws DataApiException {
    JsonElement element = json.get(name);
    if (element == null || element.isJsonNull()) {
      throw new DataApiException("Bulk job info is missing the '" + name + "' field!");
    }

    try {
      return element.getAsLong();
    } catch (UnsupportedOperationException | NumberFormatException | IllegalStateException e) {
      throw new DataApiException("Bulk job info field '" + name + "' is not a number!", e);
    }
  }

  private static Duration getDuration(JsonObject json, String name) throws DataApiException {
    return Duration.ofMillis(getLong(json, name));
  }

  private static OffsetDateTime getDateTime(JsonObject json, String name) throws DataApiException {
    String value = getString(json, name);
    try {
      return OffsetDateTime.parse(value, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new DataApiException(
          "Bulk job info field '" + name + "' has an invalid date: " + value, e);
    }
  }

  private static <E extends Enum<E>> E getEnum(
      JsonObject json, String name, E[] values, Function<E, String> apiValue)
      throws DataApiException {
    String value = getString(json, name);
    for (E candidate : values) {
      if (apiValue.apply(candidate).equals(value)) {
        return candidate;
      }
    }

    throw new DataApiException("Bulk job info field '" + name + "' has an unknown value: " + value);
  }
}
