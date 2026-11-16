/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecordTest {
  private Record record;

  @BeforeEach
  public void setUp() {
    Map<String, FieldValue> values = new HashMap<>();
    values.put(NUMBER_MAX_BYTE_KEY, new FieldValue(new JsonPrimitive(Byte.MAX_VALUE)));
    values.put(NUMBER_MAX_SHORT_KEY, new FieldValue(new JsonPrimitive(Short.MAX_VALUE)));
    values.put(NUMBER_MAX_INT_KEY, new FieldValue(new JsonPrimitive(Integer.MAX_VALUE)));
    values.put(NUMBER_MAX_LONG_KEY, new FieldValue(new JsonPrimitive(Long.MAX_VALUE)));
    values.put(NUMBER_MAX_FLOAT_KEY, new FieldValue(new JsonPrimitive(Float.MAX_VALUE)));
    values.put(NUMBER_MAX_DOUBLE_KEY, new FieldValue(new JsonPrimitive(Double.MAX_VALUE)));
    values.put(NUMBER_BIG_INTEGER_KEY, new FieldValue(new JsonPrimitive(BIG_INTEGER_TEST_VALUE)));
    values.put(NUMBER_BIG_DECIMAL_KEY, new FieldValue(new JsonPrimitive(BIG_DECIMAL_TEST_VALUE)));
    values.put(STRING_ABC_KEY, new FieldValue(new JsonPrimitive("abc")));
    values.put(STRING_EMPTY_KEY, new FieldValue(new JsonPrimitive("")));
    values.put(STRING_TRUE_KEY, new FieldValue(new JsonPrimitive("true")));
    values.put(STRING_TRUE_UPPERCASE_KEY, new FieldValue(new JsonPrimitive("TRUE")));
    values.put(BOOLEAN_TRUE_KEY, new FieldValue(new JsonPrimitive(true)));
    values.put(BOOLEAN_FALSE_KEY, new FieldValue(new JsonPrimitive(false)));
    values.put(NULL_KEY, new FieldValue(JsonNull.INSTANCE));
    values.put(BINARY_DATA_KEY, new FieldValue(BINARY_DATA_TEST_VALUE));
    values.put(RECORD_DATA_KEY, new FieldValue(RECORD_DATA_TEST_VALUE));
    values.put(
        COMPOUND_DATA_KEY,
        new FieldValue(JsonParser.parseString("{\"latitude\":37.4219,\"longitude\":-122.0841}")));
    values.put(ARRAY_DATA_KEY, new FieldValue(JsonParser.parseString("[\"a\",\"b\"]")));

    record = new Record("Movie__c", values);
  }

  @Test
  public void testGetType() {
    assertThat(record.getType(), is(equalTo("Movie__c")));
  }

  @Test
  public void testGetStringValue_MISSING_VALUE_KEY() {
    assertThat(record.getStringField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getStringField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo("" + Byte.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getStringField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo("" + Short.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getStringField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo("" + Integer.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getStringField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo("" + Long.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getStringField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo("" + Float.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getStringField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo("" + Double.MAX_VALUE))));
  }

  @Test
  public void testGetStringValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getStringField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo("18446744073709551614"))));
  }

  @Test
  public void testGetStringValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getStringField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo("3.5953862697246314E+308"))));
  }

  @Test
  public void testGetStringValue_STRING_ABC_KEY() {
    assertThat(record.getStringField(STRING_ABC_KEY), is(optionalWithValue(equalTo("abc"))));
  }

  @Test
  public void testGetStringValue_STRING_EMPTY_KEY() {
    assertThat(record.getStringField(STRING_EMPTY_KEY), is(optionalWithValue(equalTo(""))));
  }

  @Test
  public void testGetStringValue_STRING_TRUE_KEY() {
    assertThat(record.getStringField(STRING_TRUE_KEY), is(optionalWithValue(equalTo("true"))));
  }

  @Test
  public void testGetStringValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThat(
        record.getStringField(STRING_TRUE_UPPERCASE_KEY), is(optionalWithValue(equalTo("TRUE"))));
  }

  @Test
  public void testGetStringValue_BOOLEAN_TRUE_KEY() {
    assertThat(record.getStringField(BOOLEAN_TRUE_KEY), is(optionalWithValue(equalTo("true"))));
  }

  @Test
  public void testGetStringValue_BOOLEAN_FALSE_KEY() {
    assertThat(record.getStringField(BOOLEAN_FALSE_KEY), is(optionalWithValue(equalTo("false"))));
  }

  @Test
  public void testGetStringValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getStringField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetStringValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getStringField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetBooleanValue_MISSING_VALUE_KEY() {
    assertThat(record.getBooleanField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(record.getBooleanField(NUMBER_MAX_BYTE_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(record.getBooleanField(NUMBER_MAX_SHORT_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_INT_KEY() {
    assertThat(record.getBooleanField(NUMBER_MAX_INT_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_LONG_KEY() {
    assertThat(record.getBooleanField(NUMBER_MAX_LONG_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(record.getBooleanField(NUMBER_MAX_FLOAT_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(record.getBooleanField(NUMBER_MAX_DOUBLE_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_BIG_INT_KEY() {
    assertThat(record.getBooleanField(NUMBER_BIG_INTEGER_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(record.getBooleanField(NUMBER_BIG_DECIMAL_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_STRING_ABC_KEY() {
    assertThat(record.getBooleanField(STRING_ABC_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_STRING_EMPTY_KEY() {
    assertThat(record.getBooleanField(STRING_EMPTY_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_STRING_TRUE_KEY() {
    assertThat(record.getBooleanField(STRING_TRUE_KEY), is(optionalWithValue(is(true))));
  }

  @Test
  public void testGetBooleanValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThat(record.getBooleanField(STRING_TRUE_UPPERCASE_KEY), is(optionalWithValue(is(true))));
  }

  @Test
  public void testGetBooleanValue_BOOLEAN_TRUE_KEY() {
    assertThat(record.getBooleanField(BOOLEAN_TRUE_KEY), is(optionalWithValue(is(true))));
  }

  @Test
  public void testGetBooleanValue_BOOLEAN_FALSE_KEY() {
    assertThat(record.getBooleanField(BOOLEAN_FALSE_KEY), is(optionalWithValue(is(false))));
  }

  @Test
  public void testGetBooleanValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBooleanField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetBooleanValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBooleanField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetIntValue_MISSING_VALUE_KEY() {
    assertThat(record.getIntField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getIntField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((int) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getIntField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((int) Short.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getIntField(NUMBER_MAX_INT_KEY), is(optionalWithValue(equalTo(Integer.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getIntField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((int) Long.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getIntField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((int) Float.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getIntField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((int) Double.MAX_VALUE))));
  }

  @Test
  public void testGetIntValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getIntField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.intValue()))));
  }

  @Test
  public void testGetIntValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getIntField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.intValue()))));
  }

  @Test
  public void testGetIntValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetIntValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetIntValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetIntValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetIntValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetIntValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetIntValue_BINARY_DATA_KEY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetIntValue_RECORD_DATA_KEY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getIntField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetLongValue_MISSING_VALUE_KEY() {
    assertThat(record.getLongField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getLongField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((long) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getLongField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((long) Short.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getLongField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((long) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getLongField(NUMBER_MAX_LONG_KEY), is(optionalWithValue(equalTo(Long.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getLongField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((long) Float.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getLongField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((long) Double.MAX_VALUE))));
  }

  @Test
  public void testGetLongValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getLongField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.longValue()))));
  }

  @Test
  public void testGetLongValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getLongField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.longValue()))));
  }

  @Test
  public void testGetLongValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetLongValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetLongValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetLongValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetLongValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetLongValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetLongValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetLongValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getLongField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetFloatValue_MISSING_VALUE_KEY() {
    assertThat(record.getFloatField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getFloatField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((float) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getFloatField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((float) Short.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getFloatField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((float) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getFloatField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((float) Long.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getFloatField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo(Float.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getFloatField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((float) Double.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getFloatField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.floatValue()))));
  }

  @Test
  public void testGetFloatValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getFloatField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.floatValue()))));
  }

  @Test
  public void testGetFloatValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetFloatValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetFloatValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetFloatValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetFloatValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetFloatValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetFloatValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetFloatValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getFloatField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_MISSING_VALUE_KEY() {
    assertThat(record.getDoubleField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((double) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((double) Short.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((double) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((double) Long.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((double) Float.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(Double.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.doubleValue()))));
  }

  @Test
  public void testGetDoubleValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getDoubleField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.doubleValue()))));
  }

  @Test
  public void testGetDoubleValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetDoubleValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getDoubleField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetShortValue_MISSING_VALUE_KEY() {
    assertThat(record.getShortField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getShortField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo((short) Byte.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getShortField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(Short.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getShortField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((short) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getShortField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((short) Long.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getShortField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((short) Float.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getShortField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((short) Double.MAX_VALUE))));
  }

  @Test
  public void testGetShortValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getShortField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.shortValue()))));
  }

  @Test
  public void testGetShortValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getShortField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.shortValue()))));
  }

  @Test
  public void testGetShortValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetShortValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetShortValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetShortValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetShortValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetShortValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetShortValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetShortValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getShortField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetByteValue_MISSING_VALUE_KEY() {
    assertThat(record.getByteField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getByteField(NUMBER_MAX_BYTE_KEY), is(optionalWithValue(equalTo(Byte.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getByteField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo((byte) Short.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getByteField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo((byte) Integer.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getByteField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo((byte) Long.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getByteField(NUMBER_MAX_FLOAT_KEY),
        is(optionalWithValue(equalTo((byte) Float.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getByteField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo((byte) Double.MAX_VALUE))));
  }

  @Test
  public void testGetByteValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getByteField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE.byteValue()))));
  }

  @Test
  public void testGetByteValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getByteField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.byteValue()))));
  }

  @Test
  public void testGetByteValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetByteValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetByteValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetByteValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetByteValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetByteValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetByteValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetByteValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getByteField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_MISSING_VALUE_KEY() {
    assertThat(record.getBigIntegerField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Byte.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Short.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Integer.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo(BigInteger.valueOf(Long.MAX_VALUE)))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_MAX_FLOAT_KEY),
        // We cannot use BigDecimal.valueOf here, since it only accepts doubles, causing our
        // passed-in float to be promoted to double. This changes precision and no longer passes
        // the test.
        is(
            optionalWithValue(
                equalTo(new BigDecimal(String.valueOf(Float.MAX_VALUE)).toBigInteger()))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Double.MAX_VALUE).toBigInteger()))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE))));
  }

  @Test
  public void testGetBigIntegerValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getBigIntegerField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE.toBigInteger()))));
  }

  @Test
  public void testGetBigIntegerValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetBigIntegerValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigIntegerField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_MISSING_VALUE_KEY() {
    assertThat(record.getBigDecimalField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_BYTE_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_MAX_BYTE_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Byte.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_SHORT_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_MAX_SHORT_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Short.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_INT_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_MAX_INT_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Integer.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_LONG_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_MAX_LONG_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Long.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_FLOAT_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_MAX_FLOAT_KEY),
        // We cannot use BigDecimal.valueOf here, since it only accepts doubles, causing our
        // passed-in float to be promoted to double. This changes precision and no longer passes
        // the test.
        is(optionalWithValue(equalTo(new BigDecimal(String.valueOf(Float.MAX_VALUE))))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_MAX_DOUBLE_KEY),
        is(optionalWithValue(equalTo(BigDecimal.valueOf(Double.MAX_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_BIG_INT_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_BIG_INTEGER_KEY),
        is(optionalWithValue(equalTo(new BigDecimal(BIG_INTEGER_TEST_VALUE)))));
  }

  @Test
  public void testGetBigDecimalValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(
        record.getBigDecimalField(NUMBER_BIG_DECIMAL_KEY),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE))));
  }

  @Test
  public void testGetBigDecimalValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetBigDecimalValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBigDecimalField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_MISSING_VALUE_KEY() {
    assertThat(record.getBinaryField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBinaryValue_NUMBER_MAX_BYTE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_MAX_BYTE_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_MAX_SHORT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_MAX_SHORT_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_MAX_INT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_MAX_INT_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_MAX_LONG_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_MAX_LONG_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_MAX_FLOAT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_MAX_FLOAT_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_MAX_DOUBLE_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_BIG_INT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_BIG_INTEGER_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(NUMBER_BIG_DECIMAL_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_RECORD_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getBinaryField(RECORD_DATA_KEY);
        });
  }

  @Test
  public void testGetBinaryValue_BINARY_DATA_KEY() {
    assertThat(
        record.getBinaryField(BINARY_DATA_KEY),
        is(optionalWithValue(equalTo(BINARY_DATA_TEST_VALUE))));
  }

  @Test
  public void testGetRecordValue_MISSING_VALUE_KEY() {
    assertThat(record.getRecordField(MISSING_VALUE_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetRecordValue_NUMBER_MAX_BYTE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_MAX_BYTE_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_MAX_SHORT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_MAX_SHORT_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_MAX_INT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_MAX_INT_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_MAX_LONG_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_MAX_LONG_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_MAX_FLOAT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_MAX_FLOAT_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_MAX_DOUBLE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_MAX_DOUBLE_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_BIG_INT_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_BIG_INTEGER_KEY);
        });
  }

  @Test
  public void testGetRecordValue_NUMBER_BIG_DECIMAL_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(NUMBER_BIG_DECIMAL_KEY);
        });
  }

  @Test
  public void testGetRecordValue_STRING_ABC_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(STRING_ABC_KEY);
        });
  }

  @Test
  public void testGetRecordValue_STRING_EMPTY_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(STRING_EMPTY_KEY);
        });
  }

  @Test
  public void testGetRecordValue_STRING_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(STRING_TRUE_KEY);
        });
  }

  @Test
  public void testGetRecordValue_STRING_TRUE_UPPERCASE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(STRING_TRUE_UPPERCASE_KEY);
        });
  }

  @Test
  public void testGetRecordValue_BOOLEAN_TRUE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(BOOLEAN_TRUE_KEY);
        });
  }

  @Test
  public void testGetRecordValue_BOOLEAN_FALSE_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(BOOLEAN_FALSE_KEY);
        });
  }

  @Test
  public void testGetRecordValue_BINARY_DATA_KEY() {
    assertThrows(
        FieldConversionException.class,
        () -> {
          record.getRecordField(BINARY_DATA_KEY);
        });
  }

  @Test
  public void testGetRecordValue_RECORD_DATA_KEY() {
    assertThat(
        record.getRecordField(RECORD_DATA_KEY),
        is(optionalWithValue(equalTo(RECORD_DATA_TEST_VALUE))));
  }

  @Test
  public void testGetStringValue_NULL_KEY() {
    assertThat(record.getStringField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBooleanValue_NULL_KEY() {
    assertThat(record.getBooleanField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetIntValue_NULL_KEY() {
    assertThat(record.getIntField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetLongValue_NULL_KEY() {
    assertThat(record.getLongField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetFloatValue_NULL_KEY() {
    assertThat(record.getFloatField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetDoubleValue_NULL_KEY() {
    assertThat(record.getDoubleField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetShortValue_NULL_KEY() {
    assertThat(record.getShortField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetByteValue_NULL_KEY() {
    assertThat(record.getByteField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBigIntegerValue_NULL_KEY() {
    assertThat(record.getBigIntegerField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBigDecimalValue_NULL_KEY() {
    assertThat(record.getBigIntegerField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetBinaryValue_NULL_KEY() {
    assertThat(record.getBinaryField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testGetRecordValue_NULL_KEY() {
    assertThat(record.getRecordField(NULL_KEY), is(emptyOptional()));
  }

  @Test
  public void testIsNullField_MISSING_VALUE_KEY() {
    assertThat(record.isNullField(MISSING_VALUE_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_MAX_BYTE_KEY() {
    assertThat(record.isNullField(NUMBER_MAX_BYTE_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_MAX_SHORT_KEY() {
    assertThat(record.isNullField(NUMBER_MAX_SHORT_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_MAX_INT_KEY() {
    assertThat(record.isNullField(NUMBER_MAX_INT_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_MAX_LONG_KEY() {
    assertThat(record.isNullField(NUMBER_MAX_LONG_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_MAX_FLOAT_KEY() {
    assertThat(record.isNullField(NUMBER_MAX_FLOAT_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_MAX_DOUBLE_KEY() {
    assertThat(record.isNullField(NUMBER_MAX_DOUBLE_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_BIG_INT_KEY() {
    assertThat(record.isNullField(NUMBER_BIG_INTEGER_KEY), is(false));
  }

  @Test
  public void testIsNullField_NUMBER_BIG_DECIMAL_KEY() {
    assertThat(record.isNullField(NUMBER_BIG_DECIMAL_KEY), is(false));
  }

  @Test
  public void testIsNullField_STRING_ABC_KEY() {
    assertThat(record.isNullField(STRING_ABC_KEY), is(false));
  }

  @Test
  public void testIsNullField_STRING_EMPTY_KEY() {
    assertThat(record.isNullField(STRING_EMPTY_KEY), is(false));
  }

  @Test
  public void testIsNullField_STRING_TRUE_KEY() {
    assertThat(record.isNullField(STRING_TRUE_KEY), is(false));
  }

  @Test
  public void testIsNullField_STRING_TRUE_UPPERCASE_KEY() {
    assertThat(record.isNullField(STRING_TRUE_UPPERCASE_KEY), is(false));
  }

  @Test
  public void testIsNullField_BOOLEAN_TRUE_KEY() {
    assertThat(record.isNullField(BOOLEAN_TRUE_KEY), is(false));
  }

  @Test
  public void testIsNullField_BOOLEAN_FALSE_KEY() {
    assertThat(record.isNullField(BOOLEAN_FALSE_KEY), is(false));
  }

  @Test
  public void testIsNullField_NULL_KEY() {
    assertThat(record.isNullField(NULL_KEY), is(true));
  }

  @Test
  public void testIsNullField_BINARY_KEY() {
    assertThat(record.isNullField(BINARY_DATA_KEY), is(false));
  }

  @Test
  public void testIsNullField_RECORD_DATA_KEY() {
    assertThat(record.isNullField(RECORD_DATA_KEY), is(false));
  }

  @Test
  public void testIsNullFieldCaseSensitiveness() {
    assertThat(record.isNullField(randomizeCasing(NULL_KEY)), is(true));
  }

  @Test
  public void testGetStringValueCaseSensitiveness() {
    assertThat(
        record.getStringField(randomizeCasing(STRING_ABC_KEY)),
        is(optionalWithValue(equalTo("abc"))));
  }

  @Test
  public void testGetBooleanValueCaseSensitiveness() {
    assertThat(
        record.getBooleanField(randomizeCasing(BOOLEAN_TRUE_KEY)),
        is(optionalWithValue(equalTo(true))));
  }

  @Test
  public void testGetIntValueCaseSensitiveness() {
    assertThat(
        record.getIntField(randomizeCasing(NUMBER_MAX_INT_KEY)),
        is(optionalWithValue(equalTo(Integer.MAX_VALUE))));
  }

  @Test
  public void testGetLongValueCaseSensitiveness() {
    assertThat(
        record.getLongField(randomizeCasing(NUMBER_MAX_LONG_KEY)),
        is(optionalWithValue(equalTo(Long.MAX_VALUE))));
  }

  @Test
  public void testGetFloatValueCaseSensitiveness() {
    assertThat(
        record.getFloatField(randomizeCasing(NUMBER_MAX_FLOAT_KEY)),
        is(optionalWithValue(equalTo(Float.MAX_VALUE))));
  }

  @Test
  public void testGetDoubleValueCaseSensitiveness() {
    assertThat(
        record.getDoubleField(randomizeCasing(NUMBER_MAX_DOUBLE_KEY)),
        is(optionalWithValue(equalTo(Double.MAX_VALUE))));
  }

  @Test
  public void testGetShortValueCaseSensitiveness() {
    assertThat(
        record.getShortField(randomizeCasing(NUMBER_MAX_SHORT_KEY)),
        is(optionalWithValue(equalTo(Short.MAX_VALUE))));
  }

  @Test
  public void testGetByteValueCaseSensitiveness() {
    assertThat(
        record.getByteField(randomizeCasing(NUMBER_MAX_BYTE_KEY)),
        is(optionalWithValue(equalTo(Byte.MAX_VALUE))));
  }

  @Test
  public void testGetBigIntValueCaseSensitiveness() {
    assertThat(
        record.getBigIntegerField(randomizeCasing(NUMBER_BIG_INTEGER_KEY)),
        is(optionalWithValue(equalTo(BIG_INTEGER_TEST_VALUE))));
  }

  @Test
  public void testGetBigDecimalValueCaseSensitiveness() {
    assertThat(
        record.getBigDecimalField(randomizeCasing(NUMBER_BIG_DECIMAL_KEY)),
        is(optionalWithValue(equalTo(BIG_DECIMAL_TEST_VALUE))));
  }

  @Test
  public void testGetBinaryValueCaseSensitiveness() {
    assertThat(
        record.getBinaryField(randomizeCasing(BINARY_DATA_KEY)),
        is(optionalWithValue(equalTo(BINARY_DATA_TEST_VALUE))));
  }

  @Test
  public void testGetRecordValueCaseSensitiveness() {
    assertThat(
        record.getRecordField(randomizeCasing(RECORD_DATA_KEY)),
        is(optionalWithValue(equalTo(RECORD_DATA_TEST_VALUE))));
  }

  @Test
  public void testGetStringValue_COMPOUND_DATA_KEY() {
    assertThrows(FieldConversionException.class, () -> record.getStringField(COMPOUND_DATA_KEY));
  }

  @Test
  public void testGetBigDecimalValue_COMPOUND_DATA_KEY() {
    assertThrows(
        FieldConversionException.class, () -> record.getBigDecimalField(COMPOUND_DATA_KEY));
  }

  @Test
  public void testGetBinaryValue_COMPOUND_DATA_KEY() {
    assertThrows(FieldConversionException.class, () -> record.getBinaryField(COMPOUND_DATA_KEY));
  }

  @Test
  public void testGetRecordValue_COMPOUND_DATA_KEY() {
    assertThrows(FieldConversionException.class, () -> record.getRecordField(COMPOUND_DATA_KEY));
  }

  @Test
  public void testGetStringValue_ARRAY_DATA_KEY() {
    assertThrows(FieldConversionException.class, () -> record.getStringField(ARRAY_DATA_KEY));
  }

  @Test
  public void testGetBooleanValue_ARRAY_DATA_KEY() {
    assertThrows(FieldConversionException.class, () -> record.getBooleanField(ARRAY_DATA_KEY));
  }

  @Test
  public void testHasField_COMPOUND_AND_ARRAY_DATA_KEYS() {
    assertThat(record.hasField(COMPOUND_DATA_KEY), is(true));
    assertThat(record.isNullField(COMPOUND_DATA_KEY), is(false));
    assertThat(record.hasField(ARRAY_DATA_KEY), is(true));
    assertThat(record.isNullField(ARRAY_DATA_KEY), is(false));
  }

  private static String randomizeCasing(String s) {
    Random random = new Random(0x5FDC);
    char[] chars = new char[s.length()];
    s.getChars(0, s.length(), chars, 0);

    for (int i = 0; i < chars.length; i++) {
      if (random.nextBoolean()) {
        chars[i] = Character.toUpperCase(chars[i]);
      } else {
        chars[i] = Character.toLowerCase(chars[i]);
      }
    }

    return new String(chars);
  }

  private static final String MISSING_VALUE_KEY = "DOES_NOT_EXIST";
  private static final String NUMBER_MAX_BYTE_KEY = "NumberMaxByte__c";
  private static final String NUMBER_MAX_SHORT_KEY = "NumberMaxShort__c";
  private static final String NUMBER_MAX_INT_KEY = "NumberMaxInt__c";
  private static final String NUMBER_MAX_LONG_KEY = "NumberMaxLong__c";
  private static final String NUMBER_MAX_FLOAT_KEY = "NumberMaxFloat__c";
  private static final String NUMBER_MAX_DOUBLE_KEY = "NumberMaxDouble__c";
  private static final String NUMBER_BIG_INTEGER_KEY = "NumberBigInteger__c";
  private static final String NUMBER_BIG_DECIMAL_KEY = "NumberBigDecimal__c";
  private static final String STRING_ABC_KEY = "String__c";
  private static final String STRING_EMPTY_KEY = "StringEmpty__c";
  private static final String STRING_TRUE_KEY = "StringTrue__c";
  private static final String STRING_TRUE_UPPERCASE_KEY = "StringTrueUppercase__c";
  private static final String BOOLEAN_TRUE_KEY = "BooleanTrue__c";
  private static final String BOOLEAN_FALSE_KEY = "BooleanFalse__c";
  private static final String NULL_KEY = "Null__c";
  private static final String BINARY_DATA_KEY = "BinaryData__c";
  private static final String RECORD_DATA_KEY = "Owner";
  private static final String COMPOUND_DATA_KEY = "Location__c";
  private static final String ARRAY_DATA_KEY = "EventWhoIds";

  private static final BigInteger BIG_INTEGER_TEST_VALUE =
      BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2));
  private static final BigDecimal BIG_DECIMAL_TEST_VALUE =
      BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.valueOf(2));

  private static final ByteBuffer BINARY_DATA_TEST_VALUE =
      ByteBuffer.wrap("☁️ Salesforce Functions".getBytes(StandardCharsets.UTF_8));

  private static final Record RECORD_DATA_TEST_VALUE =
      new Record(
          "Owner",
          new HashMap<String, FieldValue>() {
            {
              put("Name", new FieldValue(new JsonPrimitive("Guy Smiley")));
            }
          });
}
