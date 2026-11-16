/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DataApiTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final DataApi dataApi =
      new DataApi(URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void testQuery() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT Name FROM Account");
    assertThat(result.isDone(), is(true));
    assertThat(result.getTotalSize(), is(5L));

    List<RecordWithSubQueryResults> records = result.getRecords();
    assertThat(
        records.get(0).getStringField("Name"),
        is(optionalWithValue(equalTo("An awesome test account"))));

    assertThat(
        records.get(1).getStringField("Name"), is(optionalWithValue(equalTo("Global Media"))));

    assertThat(records.get(2).getStringField("Name"), is(optionalWithValue(equalTo("Acme"))));

    assertThat(
        records.get(3).getStringField("Name"), is(optionalWithValue(equalTo("salesforce.com"))));

    assertThat(
        records.get(4).getStringField("Name"),
        is(optionalWithValue(equalTo("Sample Account for Entitlements"))));
  }

  @Test
  public void testQueryMore() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT RANDOM_1__c, RANDOM_2__c FROM Random__c");
    assertThat(result.isDone(), is(false));
    assertThat(result.getTotalSize(), is(10000L));

    RecordQueryResult result2 = dataApi.queryMore(result);
    assertThat(result2.isDone(), is(false));
    assertThat(result2.getTotalSize(), is(10000L));
  }

  @Test
  public void testQueryMoreWithDoneResult() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT Name FROM Account");
    assertThat(result.isDone(), is(true));

    RecordQueryResult result2 = dataApi.queryMore(result);
    assertThat(result2.isDone(), is(result.isDone()));
    assertThat(result2.getTotalSize(), is(result.getTotalSize()));
    assertThat(result2.getRecords(), is(empty()));
  }

  @Test
  public void testQueryWithCompoundFields() throws DataApiException {
    RecordQueryResult result =
        dataApi.query("SELECT Name, BillingAddress, Location__c FROM Account LIMIT 1");

    Record record = result.getRecords().get(0);
    assertThat(record.getStringField("Name"), is(optionalWithValue(equalTo("GenePoint"))));

    assertThat(record.hasField("BillingAddress"), is(true));
    assertThat(record.isNullField("BillingAddress"), is(false));
    assertThrows(FieldConversionException.class, () -> record.getStringField("BillingAddress"));

    assertThat(record.hasField("Location__c"), is(true));
    assertThrows(FieldConversionException.class, () -> record.getDoubleField("Location__c"));
  }

  @Test
  public void testQueryWithJunctionIdList() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT Subject, EventWhoIds FROM Event LIMIT 1");

    Record record = result.getRecords().get(0);
    assertThat(record.getStringField("Subject"), is(optionalWithValue(equalTo("Kickoff"))));

    assertThat(record.hasField("EventWhoIds"), is(true));
    assertThrows(FieldConversionException.class, () -> record.getStringField("EventWhoIds"));
  }

  @Test
  public void testQueryWithMalformedSoql() {
    try {
      dataApi.query("SELEKT Name FROM Account");
    } catch (DataApiException e) {
      assertThat(
          e.getMessage(),
          equalTo(
              "One or more API errors occurred:\n\nCode: MALFORMED_QUERY\nMessage: unexpected token: SELEKT\nFields: \n"));

      assertThat(e.getDataApiErrors(), hasSize(1));
      assertThat(e.getDataApiErrors().get(0).getErrorCode(), is(equalTo("MALFORMED_QUERY")));
      assertThat(e.getDataApiErrors().get(0).getMessage(), is(equalTo("unexpected token: SELEKT")));
      assertThat(e.getDataApiErrors().get(0).getFields(), is(empty()));
      return;
    }

    Assertions.fail("Expected Exception!");
  }

  @Test
  public void testQueryWithSubQueryResults() throws DataApiException {

    RecordQueryResult result =
        dataApi.query(
            "SELECT Account.Name, (SELECT Contact.FirstName, Contact.LastName FROM Account.Contacts) FROM Account LIMIT 5");
    assertThat(result.isDone(), is(true));
    assertThat(result.getTotalSize(), is(5L));

    List<RecordWithSubQueryResults> records = result.getRecords();
    assertThat(records.get(0).getStringField("Name"), is(optionalWithValue(equalTo("GenePoint"))));

    RecordQueryResult contactSubQueryResult = records.get(0).getSubQueryResult("Contacts").get();
    assertThat(contactSubQueryResult.getTotalSize(), is(1L));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Edna"))));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("LastName"),
        is(optionalWithValue(equalTo("Frank"))));

    assertThat(
        records.get(1).getStringField("Name"),
        is(optionalWithValue(equalTo("United Oil & Gas, UK"))));

    contactSubQueryResult = records.get(1).getSubQueryResult("Contacts").get();
    assertThat(contactSubQueryResult.getTotalSize(), is(1L));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Ashley"))));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("LastName"),
        is(optionalWithValue(equalTo("James"))));

    assertThat(
        records.get(2).getStringField("Name"),
        is(optionalWithValue(equalTo("United Oil & Gas, Singapore"))));

    contactSubQueryResult = records.get(2).getSubQueryResult("Contacts").get();
    assertThat(contactSubQueryResult.getTotalSize(), is(2L));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Tom"))));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("LastName"),
        is(optionalWithValue(equalTo("Ripley"))));
    assertThat(
        contactSubQueryResult.getRecords().get(1).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Liz"))));
    assertThat(
        contactSubQueryResult.getRecords().get(1).getStringField("LastName"),
        is(optionalWithValue(equalTo("D'Cruz"))));

    assertThat(
        records.get(3).getStringField("Name"),
        is(optionalWithValue(equalTo("Edge Communications"))));

    contactSubQueryResult = records.get(3).getSubQueryResult("Contacts").get();
    assertThat(contactSubQueryResult.getTotalSize(), is(2L));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Rose"))));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("LastName"),
        is(optionalWithValue(equalTo("Gonzalez"))));
    assertThat(
        contactSubQueryResult.getRecords().get(1).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Sean"))));
    assertThat(
        contactSubQueryResult.getRecords().get(1).getStringField("LastName"),
        is(optionalWithValue(equalTo("Forbes"))));

    assertThat(
        records.get(4).getStringField("Name"),
        is(optionalWithValue(equalTo("Burlington Textiles Corp of America"))));

    contactSubQueryResult = records.get(4).getSubQueryResult("Contacts").get();
    assertThat(contactSubQueryResult.getTotalSize(), is(1L));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("FirstName"),
        is(optionalWithValue(equalTo("Jack"))));
    assertThat(
        contactSubQueryResult.getRecords().get(0).getStringField("LastName"),
        is(optionalWithValue(equalTo("Rogers"))));
  }

  @Test
  public void testCreate() throws DataApiException {
    RecordModificationResult result =
        dataApi.create(
            dataApi
                .newRecordBuilder("Movie__c")
                .withField("Name", "Star Wars Episode V: The Empire Strikes Back")
                .withField("Rating__c", "Excellent")
                .build());

    assertThat(result.getId(), is(equalTo("a00JS0000000004YAA")));
  }

  @Test
  public void testUpdate() throws DataApiException {
    RecordModificationResult result =
        dataApi.update(
            dataApi
                .newRecordBuilder("Movie__c")
                .withField("Id", "a00JS0000000005YAA")
                .withField("ReleaseDate__c", "1980-05-21")
                .build());

    assertThat(result.getId(), is(equalTo("a00JS0000000005YAA")));
  }

  @Test
  public void testUpdateWithoutIdField() throws DataApiException {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          dataApi.update(
              dataApi
                  .newRecordBuilder("Movie__c")
                  .withField("ReleaseDate__c", "1980-05-21")
                  .build());
        });
  }

  @Test
  public void testDelete() throws DataApiException {
    RecordModificationResult result = dataApi.delete("Account", "001JS0000000002YAA");

    assertThat(result.getId(), is(equalTo("001JS0000000002YAA")));
  }

  @Test
  public void testUnitOfWork() throws DataApiException {
    UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();

    ReferenceId franchiseReference =
        unitOfWorkBuilder.registerCreate(
            dataApi.newRecordBuilder("Franchise__c").withField("Name", "Star Wars").build());

    unitOfWorkBuilder.registerCreate(
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars Episode I - A Phantom Menace")
            .withField("Franchise__c", franchiseReference)
            .build());

    unitOfWorkBuilder.registerCreate(
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars Episode II - Attack Of The Clones")
            .withField("Franchise__c", franchiseReference)
            .build());

    unitOfWorkBuilder.registerCreate(
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars Episode III - Revenge Of The Sith")
            .withField("Franchise__c", franchiseReference)
            .build());

    Map<ReferenceId, RecordModificationResult> results =
        dataApi.commitUnitOfWork(unitOfWorkBuilder.build());

    assertThat(results, is(aMapWithSize(4)));
    assertThat(results, hasKey(equalTo(franchiseReference)));
  }

  @Test
  public void testUnitOfWorkUpdate() throws DataApiException {
    UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();

    ReferenceId updateRecordReference =
        unitOfWorkBuilder.registerUpdate(
            dataApi
                .newRecordBuilder("Movie__c")
                .withField("Id", "a01JS0000000003YAA")
                .withField("ReleaseDate__c", "1980-05-21")
                .build());

    Map<ReferenceId, RecordModificationResult> result =
        dataApi.commitUnitOfWork(unitOfWorkBuilder.build());

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasKey(updateRecordReference));
  }

  @Test
  public void testUnitOfWorkDelete() throws DataApiException {
    UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();

    ReferenceId deleteRecordReference =
        unitOfWorkBuilder.registerDelete("Movie__c", "a01JS0000000002YAA");

    Map<ReferenceId, RecordModificationResult> result =
        dataApi.commitUnitOfWork(unitOfWorkBuilder.build());

    assertThat(result, is(aMapWithSize(1)));
    assertThat(result, hasKey(deleteRecordReference));
    assertThat(result.get(deleteRecordReference).getId(), is(equalTo("a01JS0000000002YAA")));
  }

  @Test
  public void testNewRecordBuilderFromRecord() {
    Record record =
        dataApi
            .newRecordBuilder("Movie__c")
            .withField("Name", "Star Wars")
            .withField("Rating__c", "Excellent")
            .build();

    Record record2 =
        dataApi
            .newRecordBuilder(record)
            .withField("Name", "Star Wars Episode VI - Return Of The Jedi")
            .build();

    assertThat(record2.getType(), is(equalTo("Movie__c")));

    assertThat(
        record2.getStringField("Rating__c"), is(equalTo(record.getStringField("Rating__c"))));

    assertThat(
        record2.getStringField("Name"),
        is(optionalWithValue(equalTo("Star Wars Episode VI - Return Of The Jedi"))));
  }

  @Test
  public void testGetAccessToken() {
    assertThat(dataApi.getAccessToken(), equalTo("00DJS0000000123!AQ0AQ.token"));
  }

  @Test
  public void testQueryWithAssociatedRecordResults() throws DataApiException {
    RecordQueryResult result = dataApi.query("SELECT Name, Owner.Name from Account LIMIT 1");
    assertThat(result.isDone(), is(true));
    assertThat(result.getTotalSize(), is(1L));

    Record record = result.getRecords().get(0);
    assertThat(record.getRecordField("Owner").get(), isA(Record.class));

    Record nestedRecord = record.getRecordField("Owner").get();
    assertThat(nestedRecord.getType(), is("User"));
    assertThat(nestedRecord.getStringField("Name").get(), is("Guy Smiley"));
  }
}
