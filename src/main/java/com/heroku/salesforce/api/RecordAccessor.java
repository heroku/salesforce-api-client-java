/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Allows to access Salesforce record data.
 *
 * @see Record
 * @see RecordBuilder
 */
public abstract sealed class RecordAccessor permits Record, RecordBuilder {
  private final String type;

  RecordAccessor(String type) {
    this.type = type;
  }

  abstract TreeMap<String, FieldValue> getFieldValues();

  /**
   * Returns the type of this record.
   *
   * @return The type of this record.
   */
  @Nonnull
  public String getType() {
    return type;
  }

  /**
   * Returns the names of all fields present in this record.
   *
   * <p>This includes fields that have null as their value.
   *
   * @return The names of all fields present in this record.
   */
  @Nonnull
  public Set<String> getFieldNames() {
    return Collections.unmodifiableSet(getFieldValues().keySet());
  }

  /**
   * Returns if this record has a field with the given name. The field name is compared
   * case-insensitively.
   *
   * <p>This includes fields that have null as their value.
   *
   * @param name The name of the field to check.
   * @return If this record has a field with the given name.
   */
  public boolean hasField(String name) {
    return getFieldValues().containsKey(name);
  }

  /**
   * Returns if this record has a field with the given name that has {@code null} as its value. The
   * field name is compared case-insensitively.
   *
   * <p>If there is no field with the given name, false is returned.
   *
   * @param name The name of the field to check.
   * @return If this record has a field with the given name that has {@code null} as its value.
   */
  public boolean isNullField(String name) {
    return Optional.ofNullable(getFieldValues().get(name))
        .flatMap(fieldValue -> Optional.ofNullable(fieldValue.getJsonData()))
        .map(JsonElement::isJsonNull)
        .orElse(false);
  }

  /**
   * Returns the value of a field as a {@link String}. The field name is compared
   * case-insensitively.
   *
   * <p>Field values that are not {@link String}s will be automatically converted to Strings. For
   * example, if the field contains the number 42, this method will return an {@link Optional} that
   * contains the String "42". If the field cannot be converted, a {@link FieldConversionException}
   * will be thrown.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be converted into a {@link String}
   *     value.
   * @return The value of the field as a {@link String} or empty {@link Optional} if the field is
   *     not present or null.
   */
  @Nonnull
  public Optional<String> getStringField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsString, String.class);
  }

  /**
   * Returns the value of a field as a {@link Boolean}. The field name is compared
   * case-insensitively.
   *
   * <p>If the field is not a boolean, its value is converted to a {@link String} first, and then
   * parsed with {@link Boolean#parseBoolean(String)}. Effectively, this means that any non-boolean
   * value that can be converted into a {@link String}, except the String "true" (compared
   * case-insensitively) will result in false being returned by this method. If the field cannot be
   * converted, a {@link FieldConversionException} will be thrown.
   *
   * @param name The name of the field to obtain the value from.
   * @return The value of the field as a {@link Boolean} or empty {@link Optional} if the field is
   *     not present or null.
   */
  @Nonnull
  public Optional<Boolean> getBooleanField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsBoolean, Boolean.class);
  }

  /**
   * Returns the value of a field as a {@link Byte}, which may involve rounding or truncation. The
   * field name is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. This allows
   * {@link String}s that represent numbers to be interpreted as numbers. If the value cannot be
   * parsed as a number, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as a {@link Byte} (i.e.
   * the value is too large or contains decimals), it will be converted to a {@link Byte} which may
   * involve rounding or truncation of the value.
   *
   * <p>The conversion is analogous to a narrowing primitive conversion or a widening primitive
   * conversion as defined in The Java® Language Specification for converting between primitive
   * types. Therefore, conversions may lose information about the overall magnitude of a numeric
   * value, may lose precision, and may even return a result of a different sign.
   *
   * <p>If full control over the conversion is required, {@link #getBigDecimalField(String)} can be
   * used. {@link BigDecimal} offers an extensive suite of methods to convert from a {@link
   * BigDecimal} to primitive number types.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link Byte} value.
   * @return The value of the field as a {@link Byte} or empty {@link Optional} if the field is not
   *     present or null.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html">The Java® Language
   *     Specification - Chapter 5. Conversions and Contexts</a>
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<Byte> getByteField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsByte, Byte.class);
  }

  /**
   * Returns the value of a field as a {@link Short}, which may involve rounding or truncation. The
   * field name is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. This allows
   * {@link String}s that represent numbers to be interpreted as numbers. If the value cannot be
   * parsed as a number, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as a {@link Short} (i.e.
   * the value is too large or contains decimals), it will be converted to a {@link Short} which may
   * involve rounding or truncation of the value.
   *
   * <p>The conversion is analogous to a narrowing primitive conversion or a widening primitive
   * conversion as defined in The Java® Language Specification for converting between primitive
   * types. Therefore, conversions may lose information about the overall magnitude of a numeric
   * value, may lose precision, and may even return a result of a different sign.
   *
   * <p>If full control over the conversion is required, {@link #getBigDecimalField(String)} can be
   * used. {@link BigDecimal} offers an extensive suite of methods to convert from a {@link
   * BigDecimal} to primitive number types.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link Short} value.
   * @return The value of the field as a {@link Short} or empty {@link Optional} if the field is not
   *     present or null.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html">The Java® Language
   *     Specification - Chapter 5. Conversions and Contexts</a>
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<Short> getShortField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsShort, Short.class);
  }

  /**
   * Returns the value of a field as an {@link Integer}, which may involve rounding or truncation.
   * The field name is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. This allows
   * {@link String}s that represent numbers to be interpreted as numbers. If the value cannot be
   * parsed as a number, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as an {@link Integer}
   * (i.e. the value is too large or contains decimals), it will be converted to an {@link Integer}
   * which may involve rounding or truncation of the value.
   *
   * <p>The conversion is analogous to a narrowing primitive conversion or a widening primitive
   * conversion as defined in The Java® Language Specification for converting between primitive
   * types. Therefore, conversions may lose information about the overall magnitude of a numeric
   * value, may lose precision, and may even return a result of a different sign.
   *
   * <p>If full control over the conversion is required, {@link #getBigDecimalField(String)} can be
   * used. {@link BigDecimal} offers an extensive suite of methods to convert from a {@link
   * BigDecimal} to primitive number types.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as an {@link Integer} value.
   * @return The value of the field as an {@link Integer} or empty {@link Optional} if the field is
   *     not present or null.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html">The Java® Language
   *     Specification - Chapter 5. Conversions and Contexts</a>
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<Integer> getIntField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsInt, Integer.class);
  }

  /**
   * Returns the value of a field as a {@link Long}, which may involve rounding or truncation. The
   * field name is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. This allows
   * {@link String}s that represent numbers to be interpreted as numbers. If the value cannot be
   * parsed as a number, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as a {@link Long} (i.e.
   * the value is too large or contains decimals), it will be converted to a {@link Long} which may
   * involve rounding or truncation of the value.
   *
   * <p>The conversion is analogous to a narrowing primitive conversion or a widening primitive
   * conversion as defined in The Java® Language Specification for converting between primitive
   * types. Therefore, conversions may lose information about the overall magnitude of a numeric
   * value, may lose precision, and may even return a result of a different sign.
   *
   * <p>If full control over the conversion is required, {@link #getBigDecimalField(String)} can be
   * used. {@link BigDecimal} offers an extensive suite of methods to convert from a {@link
   * BigDecimal} to primitive number types.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link Long} value.
   * @return The value of the field as a {@link Long} or empty {@link Optional} if the field is not
   *     present or null.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html">The Java® Language
   *     Specification - Chapter 5. Conversions and Contexts</a>
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<Long> getLongField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsLong, Long.class);
  }

  /**
   * Returns the value of a field as a {@link Float}, which may involve rounding or truncation. The
   * field name is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. This allows
   * {@link String}s that represent numbers to be interpreted as numbers. If the value cannot be
   * parsed as a number, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as a {@link Float} (i.e.
   * the value is too large or contains decimals), it will be converted to a {@link Float} which may
   * involve rounding or truncation of the value.
   *
   * <p>The conversion is analogous to a narrowing primitive conversion or a widening primitive
   * conversion as defined in The Java® Language Specification for converting between primitive
   * types. Therefore, conversions may lose information about the overall magnitude of a numeric
   * value, may lose precision, and may even return a result of a different sign.
   *
   * <p>If full control over the conversion is required, {@link #getBigDecimalField(String)} can be
   * used. {@link BigDecimal} offers an extensive suite of methods to convert from a {@link
   * BigDecimal} to primitive number types.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link Float} value.
   * @return The value of the field as a {@link Float} or empty {@link Optional} if the field is not
   *     present or null.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html">The Java® Language
   *     Specification - Chapter 5. Conversions and Contexts</a>
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<Float> getFloatField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsFloat, Float.class);
  }

  /**
   * Returns the value of a field as a {@link Double}, which may involve rounding or truncation. The
   * field name is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. This allows
   * {@link String}s that represent numbers to be interpreted as numbers. If the value cannot be
   * parsed as a number, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as a {@link Double} (i.e.
   * the value is too large or contains decimals), it will be converted to a {@link Double} which
   * may involve rounding or truncation of the value.
   *
   * <p>The conversion is analogous to a narrowing primitive conversion or a widening primitive
   * conversion as defined in The Java® Language Specification for converting between primitive
   * types. Therefore, conversions may lose information about the overall magnitude of a numeric
   * value, may lose precision, and may even return a result of a different sign.
   *
   * <p>If full control over the conversion is required, {@link #getBigDecimalField(String)} can be
   * used. {@link BigDecimal} offers an extensive suite of methods to convert from a {@link
   * BigDecimal} to primitive number types.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link Double} value.
   * @return The value of the field as a {@link Double} or empty {@link Optional} if the field is
   *     not present or null.
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-5.html">The Java® Language
   *     Specification - Chapter 5. Conversions and Contexts</a>
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<Double> getDoubleField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsDouble, Double.class);
  }

  /**
   * Returns the value of a field as a {@link BigInteger} which may involve rounding. The field name
   * is compared case-insensitively.
   *
   * <p>If field's value is not a number, this method will try to parse it as one. If that process
   * fails, a {@link FieldConversionException} is thrown.
   *
   * <p>If the (if necessary, parsed) value cannot be directly represented as a {@link BigInteger}
   * (i.e. the value contains decimals), it will be converted to a {@link BigInteger} which may
   * involve rounding. Use {@link #getBigDecimalField(String)} support for decimals is required.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link BigInteger}
   *     value.
   * @return The value of the field as a {@link BigInteger} or empty {@link Optional} if the field
   *     is not present or null.
   * @see #getBigDecimalField(String)
   */
  @Nonnull
  public Optional<BigInteger> getBigIntegerField(String name) {
    return getBigDecimalField(name).map(BigDecimal::toBigInteger);
  }

  /**
   * Returns the value of a field as a {@link BigDecimal}. The field name is compared
   * case-insensitively.
   *
   * <p>If the field's value is not a number, this method will try to parse it as one. If that
   * process fails, or the number cannot be represented as a {@link BigDecimal}, a {@link
   * FieldConversionException} is thrown.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value could not be parsed as a {@link BigDecimal}
   *     value.
   * @return The value of the field as a {@link BigDecimal} or empty {@link Optional} if the field
   *     is not present or null.
   */
  @Nonnull
  public Optional<BigDecimal> getBigDecimalField(String name) {
    return getJsonFieldValue(name, JsonPrimitive::getAsBigDecimal, BigDecimal.class);
  }

  /**
   * Returns the value of a binary field as a {@link ByteBuffer}. The field name is compared
   * case-insensitively.
   *
   * <p>Field values that are not binary data will not be converted to a {@link ByteBuffer}. Using
   * this method on a non-binary field will cause a {@link FieldConversionException} to be thrown.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value is not binary data.
   * @return The value of the binary field as a {@link ByteBuffer} or empty {@link Optional} if the
   *     field is not present or null.
   */
  @Nonnull
  public Optional<ByteBuffer> getBinaryField(String name) {
    Optional<FieldValue> fieldValue =
        Optional.ofNullable(getFieldValues().get(name))
            .filter(value -> !value.isJsonData() || !value.getJsonData().isJsonNull());

    if (fieldValue.isPresent() && !fieldValue.get().isBinaryData()) {
      throw new FieldConversionException(
          String.format("Field %s cannot be converted to ByteBuffer.", name));
    } else {
      return fieldValue.flatMap(value -> Optional.ofNullable(value.getBinaryData()));
    }
  }

  /**
   * Returns the value of a record field as a {@link Record}. The field name is compared
   * case-insensitively.
   *
   * <p>Field values that are not record data will not be converted to a {@link Record}. Using this
   * method on a non-record field will cause a {@link FieldConversionException} to be thrown.
   *
   * @param name The name of the field to obtain the value from.
   * @throws FieldConversionException If the value is not record data.
   * @return The value of the record field as a {@link Record} or empty {@link Optional} if the
   *     field is not present or null.
   */
  @Nonnull
  public Optional<Record> getRecordField(String name) {
    FieldValue value = getFieldValues().get(name);

    if (isNullField(name) || value == null) {
      return Optional.empty();
    }

    if (!value.isRecordData()) {
      throw new FieldConversionException(
          String.format("Field %s cannot be converted to Record.", name));
    }

    return Optional.ofNullable(value.getRecordData());
  }

  @Nonnull
  private <T> Optional<T> getJsonFieldValue(
      String fieldName, Function<JsonPrimitive, T> f, Class<T> klass)
      throws FieldConversionException {
    Optional<FieldValue> fieldValue = Optional.ofNullable(getFieldValues().get(fieldName));

    if (fieldValue.isPresent() && fieldValue.get().isBinaryData()) {
      throw new FieldConversionException(
          "Binary data fields cannot be converted to a " + klass.getName() + "!");
    } else if (fieldValue.isPresent() && fieldValue.get().isRecordData()) {
      throw new FieldConversionException(
          "Record data fields cannot be converted to a " + klass.getName() + "!");
    } else if (fieldValue.isPresent()
        && (fieldValue.get().getJsonData().isJsonObject()
            || fieldValue.get().getJsonData().isJsonArray())) {
      throw new FieldConversionException(
          "Compound or array data fields cannot be converted to a " + klass.getName() + "!");
    } else {
      try {
        return fieldValue
            .flatMap(v -> Optional.ofNullable(v.getJsonData()))
            .filter(JsonElement::isJsonPrimitive)
            .map(jsonElement -> f.apply(jsonElement.getAsJsonPrimitive()));
      } catch (NumberFormatException e) {
        throw new FieldConversionException("NumberFormatException while converting field!", e);
      }
    }
  }
}
