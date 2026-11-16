/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonElement;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

final class FieldValue {
  private final JsonElement jsonData;
  private final ByteBuffer binaryData;
  private final Record recordData;

  FieldValue(@Nonnull JsonElement jsonData) {
    this.jsonData = jsonData;
    this.binaryData = null;
    this.recordData = null;
  }

  FieldValue(@Nonnull ByteBuffer binaryData) {
    this.binaryData = binaryData;
    this.jsonData = null;
    this.recordData = null;
  }

  FieldValue(@Nonnull Record recordData) {
    this.recordData = recordData;
    this.binaryData = null;
    this.jsonData = null;
  }

  boolean isBinaryData() {
    return this.binaryData != null;
  }

  boolean isJsonData() {
    return this.jsonData != null;
  }

  boolean isRecordData() {
    return this.recordData != null;
  }

  JsonElement getJsonData() {
    return jsonData;
  }

  ByteBuffer getBinaryData() {
    return binaryData;
  }

  Record getRecordData() {
    return this.recordData;
  }

  @Override
  public String toString() {
    return "FieldValue{"
        + "jsonData="
        + jsonData
        + ", binaryData="
        + binaryData
        + ", recordData="
        + recordData
        + '}';
  }
}
