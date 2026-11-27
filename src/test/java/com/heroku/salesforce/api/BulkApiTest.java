/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class BulkApiTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final BulkApi bulkApi =
      new BulkApi(URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  private final IngestJobReference job = IngestJobReference.of("7507Q000009f52jQAA");

  @Test
  public void testCreateIngestJob() throws DataApiException {
    IngestJobReference reference = bulkApi.createIngestJob("Asset", IngestOperation.INSERT);
    assertThat(reference, is(equalTo(job)));
  }

  @Test
  public void testCreateIngestJobWithOptions() throws DataApiException {
    IngestJobOptions options =
        bulkApi
            .newIngestJobOptionsBuilder()
            .withExternalIdFieldName("External_Id__c")
            .withAssignmentRuleId("01QJS0000000001AAA")
            .withColumnDelimiter(ColumnDelimiter.SEMICOLON)
            .withLineEnding(LineEnding.CRLF)
            .build();

    IngestJobReference reference =
        bulkApi.createIngestJob("Account", IngestOperation.UPSERT, options);
    assertThat(reference.getId(), is(equalTo("7507Q000009f52kQAA")));
  }

  @Test
  public void testCreateUpsertIngestJobWithoutExternalIdFieldName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> bulkApi.createIngestJob("Account", IngestOperation.UPSERT));
  }

  @Test
  public void testUploadJobData() throws DataApiException {
    bulkApi.uploadJobData(job, csv("Name,Employees\nFoo,12\nBar,1138\n"));
  }

  @Test
  public void testUploadJobDataRedirect() {
    wireMock.stubFor(
        put("/services/data/v53.0/jobs/ingest/7507Q000009f52lQAA/batches")
            .willReturn(aResponse().withStatus(307).withHeader("Location", "/redirected")));
    wireMock.stubFor(put("/redirected").willReturn(aResponse().withStatus(201)));

    assertThrows(
        DataApiException.class,
        () ->
            bulkApi.uploadJobData(
                IngestJobReference.of("7507Q000009f52lQAA"),
                csv("Name,Employees\nFoo,12\nBar,1138\n")));
  }

  @Test
  public void testCloseJob() throws DataApiException {
    bulkApi.closeJob(job);
  }

  @Test
  public void testAbortJob() throws DataApiException {
    bulkApi.abortJob(IngestJobReference.of("7507Q000009f4YjQAI"));
  }

  @Test
  public void testAbortNonexistentJob() {
    DataApiException exception =
        assertThrows(
            DataApiException.class,
            () -> bulkApi.abortJob(IngestJobReference.of("7507Q000009f4YjXXX")));

    assertThat(exception.getDataApiErrors().size(), is(1));
    assertThat(exception.getDataApiErrors().get(0).getErrorCode(), is(equalTo("NOT_FOUND")));
  }

  @Test
  public void testDeleteJob() throws DataApiException {
    bulkApi.deleteJob(job);
  }

  @Test
  public void testGetJobInfo() throws DataApiException {
    IngestJobInfo info = bulkApi.getJobInfo(job);
    assertThat(info.getReference(), is(equalTo(job)));
    assertThat(info.getId(), is(equalTo("7507Q000009f52jQAA")));
    assertThat(info.getObjectType(), is(equalTo("Account")));
    assertThat(info.getOperation(), is(IngestOperation.INSERT));
    assertThat(info.getState(), is(IngestJobState.FAILED));
    assertThat(info.getExternalIdFieldName(), is(emptyOptional()));
    assertThat(info.getColumnDelimiter(), is(ColumnDelimiter.COMMA));
    assertThat(info.getLineEnding(), is(LineEnding.LF));
    assertThat(info.getNumberRecordsProcessed(), is(0L));
    assertThat(info.getNumberRecordsFailed(), is(0L));
    assertThat(info.getRetries(), is(0));
    assertThat(
        info.getErrorMessage(),
        is(
            optionalWithValue(
                equalTo("InvalidBatch : InvalidBatch : Field name not found : Employees"))));
    assertThat(info.getCreatedById(), is(equalTo("0057Q0000037T8QQAU")));
    assertThat(
        info.getCreatedDate(),
        is(equalTo(OffsetDateTime.of(2023, 1, 26, 10, 9, 27, 0, ZoneOffset.UTC))));
    assertThat(
        info.getSystemModstamp(),
        is(equalTo(OffsetDateTime.of(2023, 1, 26, 10, 9, 49, 0, ZoneOffset.UTC))));
    assertThat(info.getTotalProcessingTime(), is(Duration.ZERO));
    assertThat(info.getApiActiveProcessingTime(), is(Duration.ZERO));
    assertThat(info.getApexProcessingTime(), is(Duration.ZERO));
  }

  @Test
  public void testGetSuccessfulRecordResults() throws DataApiException, IOException {
    try (InputStream results = bulkApi.getSuccessfulRecordResults(job)) {
      assertThat(read(results), is(equalTo("\"sf__Id\",\"sf__Created\",\"Name\",\"Employees\"\n")));
    }
  }

  @Test
  public void testGetFailedRecordResults() throws DataApiException, IOException {
    try (InputStream results = bulkApi.getFailedRecordResults(job)) {
      assertThat(read(results), is(equalTo("\"sf__Id\",\"sf__Error\",\"Name\",\"Employees\"\n")));
    }
  }

  @Test
  public void testGetUnprocessedRecords() throws DataApiException, IOException {
    try (InputStream results = bulkApi.getUnprocessedRecords(job)) {
      assertThat(
          read(results).lines().toList(),
          contains("\"Name\",\"Employees\"", "\"Foo\",12", "\"Bar\",1138"));
    }
  }

  @Test
  public void testIngestJobOptionsDefaults() {
    IngestJobOptions options = bulkApi.newIngestJobOptionsBuilder().build();
    assertThat(options.getExternalIdFieldName(), is(emptyOptional()));
    assertThat(options.getAssignmentRuleId(), is(emptyOptional()));
    assertThat(options.getColumnDelimiter(), is(ColumnDelimiter.COMMA));
    assertThat(options.getLineEnding(), is(LineEnding.LF));
  }

  private final QueryJobReference queryJob = QueryJobReference.of("7507Q000009f8jvQAA");

  @Test
  public void testCreateQueryJob() throws DataApiException {
    QueryJobReference reference =
        bulkApi.createQueryJob("SELECT Name, Type, Industry FROM Account", QueryOperation.QUERY);
    assertThat(reference, is(equalTo(queryJob)));
  }

  @Test
  public void testGetQueryJobInfo() throws DataApiException {
    QueryJobInfo info = bulkApi.getJobInfo(queryJob);
    assertThat(info.getReference(), is(equalTo(queryJob)));
    assertThat(info.getObjectType(), is(equalTo("Account")));
    assertThat(info.getOperation(), is(QueryOperation.QUERY));
    assertThat(info.getState(), is(QueryJobState.JOB_COMPLETE));
    assertThat(info.getColumnDelimiter(), is(ColumnDelimiter.COMMA));
    assertThat(info.getLineEnding(), is(LineEnding.LF));
    assertThat(info.getNumberRecordsProcessed(), is(13L));
    assertThat(info.getRetries(), is(0));
    assertThat(info.getErrorMessage(), is(emptyOptional()));
    assertThat(
        info.getSystemModstamp(),
        is(equalTo(OffsetDateTime.of(2023, 1, 26, 14, 50, 12, 0, ZoneOffset.UTC))));
    assertThat(info.getTotalProcessingTime(), is(Duration.ofMillis(176)));
  }

  @Test
  public void testGetQueryJobResults() throws DataApiException, IOException {
    try (QueryJobResult result = bulkApi.getQueryJobResults(queryJob)) {
      assertThat(result.getNumberOfRecords(), is(13L));
      assertThat(result.isDone(), is(true));

      List<String> lines = read(result.getData()).lines().toList();
      assertThat(lines, hasSize(14));
      assertThat(lines.get(0), is(equalTo("\"Name\",\"Type\",\"Industry\"")));
      assertThat(lines.get(1), is(equalTo("\"Beispielaccount für Ansprüche\",\"\",\"\"")));
    }
  }

  @Test
  public void testGetMoreQueryJobResults() throws DataApiException, IOException {
    QueryJobReference job = QueryJobReference.of("7507Q000009f9tHQAQ");

    try (QueryJobResult first = bulkApi.getQueryJobResults(job, 10)) {
      assertThat(first.getNumberOfRecords(), is(10L));
      assertThat(first.isDone(), is(false));
      assertThat(read(first.getData()).lines().count(), is(11L));

      try (QueryJobResult second = bulkApi.getMoreQueryJobResults(first)) {
        assertThat(second.getNumberOfRecords(), is(3L));
        assertThat(second.isDone(), is(true));
        assertThat(
            read(second.getData()).lines().toList(),
            contains(
                "\"Name\",\"Type\",\"Industry\"",
                "\"United Oil & Gas, Singapore\",\"Customer - Direct\",\"Energy\"",
                "\"GenePoint\",\"Customer - Channel\",\"Biotechnology\"",
                "\"sForce\",\"\",\"\""));

        try (QueryJobResult third = bulkApi.getMoreQueryJobResults(second)) {
          assertThat(third.getNumberOfRecords(), is(0L));
          assertThat(third.isDone(), is(true));
          assertThat(read(third.getData()), is(equalTo("")));
        }
      }
    }
  }

  @Test
  public void testAbortQueryJob() throws DataApiException {
    bulkApi.abortJob(queryJob);
  }

  @Test
  public void testDeleteQueryJob() throws DataApiException {
    bulkApi.deleteJob(queryJob);
  }

  @Test
  public void testQueryJobOptionsDefaults() {
    QueryJobOptions options = bulkApi.newQueryJobOptionsBuilder().build();
    assertThat(options.getColumnDelimiter(), is(ColumnDelimiter.COMMA));
    assertThat(options.getLineEnding(), is(LineEnding.LF));
  }

  private static InputStream csv(String csv) {
    return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
  }

  private static String read(InputStream inputStream) throws IOException {
    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
  }
}
