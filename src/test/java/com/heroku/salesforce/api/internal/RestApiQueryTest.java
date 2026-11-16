/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.heroku.salesforce.api.internal.RestApiRecordBuilder.*;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RestApiQueryTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final RestApi restApi =
      new RestApi(URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void queryWithNextRecordsUrl()
      throws IOException, RestApiErrorsException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(result.isDone(), is(false));
    assertThat(result.getTotalSize(), is(10000L));
    assertThat(result.getRecords(), hasSize(2000));

    assertThat(
        result.getNextRecordsPath(),
        is(optionalWithValue(equalTo("/services/data/v53.0/query/01gJS0000000001YAA-2000"))));
  }

  @Test
  public void queryMoreRecords() throws IOException, RestApiErrorsException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    QueryRecordResult result = restApi.execute(queryRequest);

    QueryNextRecordsRestApiRequest queryMoreRequest =
        new QueryNextRecordsRestApiRequest(result.getNextRecordsPath().get());
    QueryRecordResult result2 = restApi.execute(queryMoreRequest);

    assertThat(result2.getRecords(), hasSize(2000));
  }

  @Test
  public void queryWithUnknownColumn() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Bacon__c FROM Account LIMIT 2");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiErrorsException e) {
      assertThat(e.getApiErrors(), hasSize(1));

      RestApiError apiError = e.getApiErrors().get(0);

      assertThat(
          apiError.getMessage(),
          is(
              equalTo(
                  "\nSELECT Bacon__c FROM Account LIMIT 2\n       ^\nERROR at Row:1:Column:8\nNo such column 'Bacon__c' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names.")));

      assertThat(apiError.getErrorCode(), is(equalTo("INVALID_FIELD")));
      return;
    }

    Assertions.fail("Expected exception!");
  }

  @Test
  public void queryWithMalformedSoql() throws IOException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELEKT Name FROM Account");

    try {
      restApi.execute(queryRequest);
    } catch (RestApiErrorsException e) {
      assertThat(e.getApiErrors(), hasSize(1));

      RestApiError apiError = e.getApiErrors().get(0);

      assertThat(apiError.getMessage(), is(equalTo("unexpected token: SELEKT")));

      assertThat(apiError.getErrorCode(), is(equalTo("MALFORMED_QUERY")));
      return;
    }

    Assertions.fail("Expected exception!");
  }

  @Test
  public void queryAccountNames() throws IOException, RestApiErrorsException, RestApiException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Name FROM Account");
    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(result.getTotalSize(), is(5L));
    assertThat(result.isDone(), is(true));
    assertThat(result.getNextRecordsPath(), equalTo(Optional.empty()));

    List<RestApiRecord> expectedRecords = new ArrayList<>();
    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000009YAA")),
            fields(field("Name", "An awesome test account"))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000010YAA")),
            fields(field("Name", "Global Media"))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000011YAA")),
            fields(field("Name", "Acme"))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000012YAA")),
            fields(field("Name", "salesforce.com"))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000013YAA")),
            fields(field("Name", "Sample Account for Entitlements"))));

    assertThat(result.getRecords(), is(equalTo(expectedRecords)));
  }

  @Test
  public void queryWithSubQuery() throws IOException, RestApiException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest(
            "SELECT Account.Name, (SELECT Contact.FirstName, Contact.LastName FROM Account.Contacts) FROM Account LIMIT 5");

    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(result.isDone(), is(true));

    List<RestApiRecord> expectedRecords = new ArrayList<>();
    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000004YAA")),
            fields(
                field("Name", "GenePoint"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        1,
                        true,
                        Collections.singletonList(
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000001YAA")),
                                fields(field("FirstName", "Edna"), field("LastName", "Frank")))),
                        null)))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000005YAA")),
            fields(
                field("Name", "United Oil & Gas, UK"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        1,
                        true,
                        Collections.singletonList(
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000002YAA")),
                                fields(field("FirstName", "Ashley"), field("LastName", "James")))),
                        null)))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000006YAA")),
            fields(
                field("Name", "United Oil & Gas, Singapore"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        2,
                        true,
                        Arrays.asList(
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000003YAA")),
                                fields(field("FirstName", "Tom"), field("LastName", "Ripley"))),
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000004YAA")),
                                fields(field("FirstName", "Liz"), field("LastName", "D'Cruz")))),
                        null)))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000007YAA")),
            fields(
                field("Name", "Edge Communications"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        2,
                        true,
                        Arrays.asList(
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000005YAA")),
                                fields(field("FirstName", "Rose"), field("LastName", "Gonzalez"))),
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000006YAA")),
                                fields(field("FirstName", "Sean"), field("LastName", "Forbes")))),
                        null)))));

    expectedRecords.add(
        new RestApiRecord(
            attributes(
                attribute("type", "Account"),
                attribute("url", "/services/data/v53.0/sobjects/Account/001JS0000000008YAA")),
            fields(
                field("Name", "Burlington Textiles Corp of America"),
                field(
                    "Contacts",
                    new QueryRecordResult(
                        1,
                        true,
                        Collections.singletonList(
                            new RestApiRecord(
                                attributes(
                                    attribute("type", "Contact"),
                                    attribute(
                                        "url",
                                        "/services/data/v53.0/sobjects/Contact/003JS0000000007YAA")),
                                fields(field("FirstName", "Jack"), field("LastName", "Rogers")))),
                        null)))));

    assertThat(result.getRecords(), is(equalTo(expectedRecords)));
  }

  @Test
  public void queryWithCompoundFields()
      throws IOException, RestApiException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest(
            "SELECT Name, BillingAddress, Location__c FROM Account LIMIT 1");

    QueryRecordResult result = restApi.execute(queryRequest);

    Map<String, RestApiRecord.FieldValue> values = result.getRecords().get(0).getValues();
    assertThat(
        values.get("BillingAddress"),
        is(
            equalTo(
                new RestApiRecord.FieldValue(
                    JsonParser.parseString(
                        "{\"city\":\"Mountain View\",\"country\":\"United States\",\"countryCode\":\"US\",\"geocodeAccuracy\":null,\"latitude\":null,\"longitude\":null,\"postalCode\":\"94043\",\"state\":\"CA\",\"stateCode\":\"CA\",\"street\":\"345 Shoreline Park\"}")))));

    assertThat(
        values.get("Location__c"),
        is(
            equalTo(
                new RestApiRecord.FieldValue(
                    JsonParser.parseString("{\"latitude\":37.4219,\"longitude\":-122.0841}")))));
  }

  @Test
  public void queryWithJunctionIdList()
      throws IOException, RestApiException, RestApiErrorsException {
    QueryRecordRestApiRequest queryRequest =
        new QueryRecordRestApiRequest("SELECT Subject, EventWhoIds FROM Event LIMIT 1");

    QueryRecordResult result = restApi.execute(queryRequest);

    assertThat(
        result.getRecords().get(0).getValues().get("EventWhoIds"),
        is(
            equalTo(
                new RestApiRecord.FieldValue(
                    JsonParser.parseString("[\"003JS0000000001YAA\",\"003JS0000000002YAA\"]")))));
  }
}
