/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Builder for {@link Record}.
 *
 * @see DataApi#newRecordBuilder(String)
 */
public final class RecordBuilder extends RecordAccessor {
  private final TreeMap<String, FieldValue> fieldValues =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  RecordBuilder(String type) {
    super(type);
  }

  RecordBuilder(String type, Map<String, FieldValue> fieldValues) {
    super(type);
    this.fieldValues.putAll(fieldValues);
  }

  /**
   * Returns a new and immutable {@link Record} instance based on the information stored in this
   * builder.
   *
   * @return The new {@link Record} instance.
   */
  @Nonnull
  public Record build() {
    return new Record(getType(), fieldValues);
  }

  /**
   * Removes a field from this record builder.
   *
   * <p>It will remove the field entirely and will not set it to null. To set a field's value to
   * {@code null}, use {@link #withNullField(String)}.
   *
   * @param name The name of the field.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withoutField(String name) {
    fieldValues.remove(name);
    return this;
  }

  /**
   * Sets the value of a field to {@code null}.
   *
   * @param name The name of the field.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withNullField(String name) {
    fieldValues.put(name, new FieldValue(JsonNull.INSTANCE));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, @Nullable String value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, short value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, long value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, int value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, float value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, double value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, byte value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, boolean value) {
    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, @Nullable BigInteger value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, @Nullable BigDecimal value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new FieldValue(new JsonPrimitive(value)));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, @Nullable ReferenceId value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new FieldValue(new JsonPrimitive(value.toApiString())));
    return this;
  }

  /**
   * Sets the value of a field.
   *
   * @param name The name of the field.
   * @param value The value to set.
   * @return This {@link RecordBuilder} instance to allow method chaining.
   */
  @Nonnull
  public RecordBuilder withField(String name, @Nullable ByteBuffer value) {
    if (value == null) {
      return withNullField(name);
    }

    fieldValues.put(name, new FieldValue(value));
    return this;
  }

  @Override
  TreeMap<String, FieldValue> getFieldValues() {
    return fieldValues;
  }
}
