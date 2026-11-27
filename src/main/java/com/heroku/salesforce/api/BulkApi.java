/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonObject;
import com.heroku.salesforce.api.internal.BulkJobType;
import com.heroku.salesforce.api.internal.BulkQueryJobResults;
import com.heroku.salesforce.api.internal.CreateBulkJobRestApiRequest;
import com.heroku.salesforce.api.internal.DeleteBulkJobRestApiRequest;
import com.heroku.salesforce.api.internal.GetBulkIngestJobResultsRestApiRequest;
import com.heroku.salesforce.api.internal.GetBulkIngestJobResultsRestApiRequest.ResultType;
import com.heroku.salesforce.api.internal.GetBulkJobInfoRestApiRequest;
import com.heroku.salesforce.api.internal.GetBulkQueryJobResultsRestApiRequest;
import com.heroku.salesforce.api.internal.JsonResponseRestApiRequest;
import com.heroku.salesforce.api.internal.RestApiRequestBody;
import com.heroku.salesforce.api.internal.SalesforceRestApiHttpClient;
import com.heroku.salesforce.api.internal.StreamingRestApiRequest;
import com.heroku.salesforce.api.internal.UpdateBulkJobStateRestApiRequest;
import com.heroku.salesforce.api.internal.UploadBulkIngestJobDataRestApiRequest;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Bulk API 2.0 client to load or query large amounts of data in a Salesforce org.
 *
 * <p>Job data and results are CSV. This client passes CSV through as streams without parsing or
 * generating it, so any CSV library can be used to produce and consume it.
 *
 * @see <a
 *     href="https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/bulk_api_2_0.htm">Bulk
 *     API 2.0</a>
 */
public final class BulkApi {
  private final SalesforceRestApiHttpClient httpClient;

  /**
   * Creates a new Bulk API client.
   *
   * @param orgDomainUrl The domain URL of the Salesforce org, e.g. {@code
   *     https://example.my.salesforce.com}.
   * @param apiVersion The Salesforce API version to use, e.g. {@code 62.0}.
   * @param accessToken The access token used to authenticate with the Salesforce org.
   */
  public BulkApi(URI orgDomainUrl, String apiVersion, String accessToken) {
    this.httpClient = new SalesforceRestApiHttpClient(orgDomainUrl, apiVersion, accessToken);
  }

  /**
   * Creates a new ingest job with default options.
   *
   * @param objectType The type of object to ingest, e.g. {@code Account}.
   * @param operation The operation to perform on the records.
   * @return A reference to the created job.
   * @throws DataApiException If an error occurred while creating the job.
   * @throws IllegalArgumentException If the operation is {@link IngestOperation#UPSERT}, which
   *     requires an external ID field name.
   */
  @Nonnull
  public IngestJobReference createIngestJob(String objectType, IngestOperation operation)
      throws DataApiException {
    return createIngestJob(objectType, operation, newIngestJobOptionsBuilder().build());
  }

  /**
   * Creates a new ingest job.
   *
   * @param objectType The type of object to ingest, e.g. {@code Account}.
   * @param operation The operation to perform on the records.
   * @param options Additional options for the job.
   * @return A reference to the created job.
   * @throws DataApiException If an error occurred while creating the job.
   * @throws IllegalArgumentException If the operation is {@link IngestOperation#UPSERT} and the
   *     options do not specify an external ID field name.
   */
  @Nonnull
  public IngestJobReference createIngestJob(
      String objectType, IngestOperation operation, IngestJobOptions options)
      throws DataApiException {
    if (operation == IngestOperation.UPSERT && options.getExternalIdFieldName().isEmpty()) {
      throw new IllegalArgumentException("Upsert jobs require an external ID field name.");
    }

    JsonObject body = new JsonObject();
    body.addProperty("object", objectType);
    body.addProperty("operation", operation.getApiValue());
    body.addProperty("contentType", "CSV");
    body.addProperty("columnDelimiter", options.getColumnDelimiter().getApiValue());
    body.addProperty("lineEnding", options.getLineEnding().getApiValue());
    options
        .getExternalIdFieldName()
        .ifPresent(name -> body.addProperty("externalIdFieldName", name));
    options.getAssignmentRuleId().ifPresent(id -> body.addProperty("assignmentRuleId", id));

    return BulkJobInfoParser.parseIngestJobReference(
        execute(new CreateBulkJobRestApiRequest(BulkJobType.INGEST, body)));
  }

  /**
   * Uploads the CSV data of an open ingest job. Data can only be uploaded once per job.
   *
   * <p>The stream is read until its end and may be closed afterwards. The caller remains
   * responsible for closing it, since it stays open if the upload fails before reaching the end.
   *
   * @param job The job to upload data for.
   * @param csv The CSV data, using the column delimiter and line ending of the job.
   * @throws DataApiException If an error occurred while uploading the data.
   * @see #closeJob(IngestJobReference)
   */
  public void uploadJobData(IngestJobReference job, InputStream csv) throws DataApiException {
    execute(new UploadBulkIngestJobDataRestApiRequest(job.getId(), csv));
  }

  /**
   * Marks the data upload of an ingest job as complete. Salesforce queues the job for processing
   * afterwards.
   *
   * @param job The job to close.
   * @throws DataApiException If an error occurred while closing the job.
   * @see #getJobInfo(IngestJobReference)
   */
  public void closeJob(IngestJobReference job) throws DataApiException {
    updateJobState(job, IngestJobState.UPLOAD_COMPLETE);
  }

  /**
   * Aborts an ingest job. Records that were already processed are not rolled back.
   *
   * @param job The job to abort.
   * @throws DataApiException If an error occurred while aborting the job.
   */
  public void abortJob(IngestJobReference job) throws DataApiException {
    updateJobState(job, IngestJobState.ABORTED);
  }

  /**
   * Deletes an ingest job. The job must be complete, aborted or failed.
   *
   * @param job The job to delete.
   * @throws DataApiException If an error occurred while deleting the job.
   */
  public void deleteJob(IngestJobReference job) throws DataApiException {
    execute(new DeleteBulkJobRestApiRequest(BulkJobType.INGEST, job.getId()));
  }

  /**
   * Returns information about an ingest job, including its processing state.
   *
   * @param job The job to get information about.
   * @return Information about the job.
   * @throws DataApiException If an error occurred while getting the job information.
   */
  @Nonnull
  public IngestJobInfo getJobInfo(IngestJobReference job) throws DataApiException {
    return BulkJobInfoParser.parseIngestJobInfo(
        execute(new GetBulkJobInfoRestApiRequest(BulkJobType.INGEST, job.getId())));
  }

  /**
   * Returns the successfully processed records of an ingest job as CSV. Each record contains the
   * {@code sf__Id} and {@code sf__Created} columns followed by the uploaded columns.
   *
   * <p>The caller must close the returned stream.
   *
   * @param job The job to get results for.
   * @return A stream of CSV data.
   * @throws DataApiException If an error occurred while requesting the results.
   */
  @Nonnull
  public InputStream getSuccessfulRecordResults(IngestJobReference job) throws DataApiException {
    return getResults(job, ResultType.SUCCESSFUL);
  }

  /**
   * Returns the records of an ingest job that failed to process as CSV. Each record contains the
   * {@code sf__Id} and {@code sf__Error} columns followed by the uploaded columns.
   *
   * <p>The caller must close the returned stream.
   *
   * @param job The job to get results for.
   * @return A stream of CSV data.
   * @throws DataApiException If an error occurred while requesting the results.
   */
  @Nonnull
  public InputStream getFailedRecordResults(IngestJobReference job) throws DataApiException {
    return getResults(job, ResultType.FAILED);
  }

  /**
   * Returns the records of an ingest job that were not processed as CSV, for example because the
   * job was aborted.
   *
   * <p>The caller must close the returned stream.
   *
   * @param job The job to get results for.
   * @return A stream of CSV data.
   * @throws DataApiException If an error occurred while requesting the records.
   */
  @Nonnull
  public InputStream getUnprocessedRecords(IngestJobReference job) throws DataApiException {
    return getResults(job, ResultType.UNPROCESSED);
  }

  /**
   * Creates a new query job with default options. Salesforce starts processing the job right away.
   *
   * @param soql The SOQL query.
   * @param operation The operation of the job.
   * @return A reference to the created job.
   * @throws DataApiException If an error occurred while creating the job.
   */
  @Nonnull
  public QueryJobReference createQueryJob(String soql, QueryOperation operation)
      throws DataApiException {
    return createQueryJob(soql, operation, newQueryJobOptionsBuilder().build());
  }

  /**
   * Creates a new query job. Salesforce starts processing the job right away.
   *
   * @param soql The SOQL query.
   * @param operation The operation of the job.
   * @param options Additional options for the job.
   * @return A reference to the created job.
   * @throws DataApiException If an error occurred while creating the job.
   */
  @Nonnull
  public QueryJobReference createQueryJob(
      String soql, QueryOperation operation, QueryJobOptions options) throws DataApiException {
    JsonObject body = new JsonObject();
    body.addProperty("operation", operation.getApiValue());
    body.addProperty("query", soql);
    body.addProperty("contentType", "CSV");
    body.addProperty("columnDelimiter", options.getColumnDelimiter().getApiValue());
    body.addProperty("lineEnding", options.getLineEnding().getApiValue());

    return BulkJobInfoParser.parseQueryJobReference(
        execute(new CreateBulkJobRestApiRequest(BulkJobType.QUERY, body)));
  }

  /**
   * Returns information about a query job, including its processing state.
   *
   * @param job The job to get information about.
   * @return Information about the job.
   * @throws DataApiException If an error occurred while getting the job information.
   */
  @Nonnull
  public QueryJobInfo getJobInfo(QueryJobReference job) throws DataApiException {
    return BulkJobInfoParser.parseQueryJobInfo(
        execute(new GetBulkJobInfoRestApiRequest(BulkJobType.QUERY, job.getId())));
  }

  /**
   * Returns the first page of results of a completed query job. Salesforce chooses the page size.
   *
   * @param job The job to get results for.
   * @return The first page of results. The caller must close it.
   * @throws DataApiException If an error occurred while requesting the results.
   * @see #getMoreQueryJobResults(QueryJobResult)
   */
  @Nonnull
  public QueryJobResult getQueryJobResults(QueryJobReference job) throws DataApiException {
    return getQueryJobResults(job, Optional.empty(), Optional.empty());
  }

  /**
   * Returns the first page of results of a completed query job.
   *
   * @param job The job to get results for.
   * @param maxRecords The maximum number of records per page. Applies to subsequent pages requested
   *     with {@link #getMoreQueryJobResults(QueryJobResult)} as well.
   * @return The first page of results. The caller must close it.
   * @throws DataApiException If an error occurred while requesting the results.
   */
  @Nonnull
  public QueryJobResult getQueryJobResults(QueryJobReference job, long maxRecords)
      throws DataApiException {
    return getQueryJobResults(job, Optional.empty(), Optional.of(maxRecords));
  }

  /**
   * Returns the next page of results of a query job, based on the given page.
   *
   * @param previous The previous page of results.
   * @return The next page of results or an empty one if the given page was already the last. The
   *     caller must close it.
   * @throws DataApiException If an error occurred while requesting the results.
   * @see QueryJobResult#isDone()
   */
  @Nonnull
  public QueryJobResult getMoreQueryJobResults(QueryJobResult previous) throws DataApiException {
    if (previous.getLocator().isEmpty()) {
      return new QueryJobResult(
          previous.getJob(),
          InputStream.nullInputStream(),
          0,
          null,
          previous.getMaxRecords().orElse(null));
    }

    return getQueryJobResults(previous.getJob(), previous.getLocator(), previous.getMaxRecords());
  }

  /**
   * Aborts a query job.
   *
   * @param job The job to abort.
   * @throws DataApiException If an error occurred while aborting the job.
   */
  public void abortJob(QueryJobReference job) throws DataApiException {
    execute(
        new UpdateBulkJobStateRestApiRequest(
            BulkJobType.QUERY, job.getId(), QueryJobState.ABORTED.getApiValue()));
  }

  /**
   * Deletes a query job and its results. The job must be complete, aborted or failed.
   *
   * @param job The job to delete.
   * @throws DataApiException If an error occurred while deleting the job.
   */
  public void deleteJob(QueryJobReference job) throws DataApiException {
    execute(new DeleteBulkJobRestApiRequest(BulkJobType.QUERY, job.getId()));
  }

  /**
   * Creates a new builder for {@link IngestJobOptions}.
   *
   * @return A new builder with default options.
   */
  @Nonnull
  public IngestJobOptionsBuilder newIngestJobOptionsBuilder() {
    return new IngestJobOptionsBuilder();
  }

  /**
   * Creates a new builder for {@link QueryJobOptions}.
   *
   * @return A new builder with default options.
   */
  @Nonnull
  public QueryJobOptionsBuilder newQueryJobOptionsBuilder() {
    return new QueryJobOptionsBuilder();
  }

  private void updateJobState(IngestJobReference job, IngestJobState state)
      throws DataApiException {
    execute(
        new UpdateBulkJobStateRestApiRequest(BulkJobType.INGEST, job.getId(), state.getApiValue()));
  }

  private InputStream getResults(IngestJobReference job, ResultType resultType)
      throws DataApiException {
    return executeStreaming(new GetBulkIngestJobResultsRestApiRequest(job.getId(), resultType));
  }

  private QueryJobResult getQueryJobResults(
      QueryJobReference job, Optional<String> locator, Optional<Long> maxRecords)
      throws DataApiException {
    BulkQueryJobResults results =
        executeStreaming(
            new GetBulkQueryJobResultsRestApiRequest(job.getId(), locator, maxRecords));

    return new QueryJobResult(
        job,
        results.getData(),
        results.getNumberOfRecords(),
        results.getLocator().orElse(null),
        maxRecords.orElse(null));
  }

  private <T, A extends RestApiRequestBody> T execute(JsonResponseRestApiRequest<T, A> request)
      throws DataApiException {
    return ApiExceptions.translate(() -> httpClient.execute(request));
  }

  private <T, A extends RestApiRequestBody> T executeStreaming(
      StreamingRestApiRequest<T, A> request) throws DataApiException {
    return ApiExceptions.translate(() -> httpClient.executeStreaming(request));
  }
}
