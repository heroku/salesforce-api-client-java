/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.google.gson.JsonElement;
import com.heroku.salesforce.api.internal.CompositeGraphRestApiRequest;
import com.heroku.salesforce.api.internal.CreateRecordRestApiRequest;
import com.heroku.salesforce.api.internal.DeleteRecordRestApiRequest;
import com.heroku.salesforce.api.internal.JsonRestApiRequest;
import com.heroku.salesforce.api.internal.ModifyRecordResult;
import com.heroku.salesforce.api.internal.QueryNextRecordsRestApiRequest;
import com.heroku.salesforce.api.internal.QueryRecordRestApiRequest;
import com.heroku.salesforce.api.internal.QueryRecordResult;
import com.heroku.salesforce.api.internal.RestApi;
import com.heroku.salesforce.api.internal.RestApiErrorsException;
import com.heroku.salesforce.api.internal.RestApiException;
import com.heroku.salesforce.api.internal.UpdateRecordRestApiRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/** Data API client to interact with data in a Salesforce org. */
public final class DataApi {
  private final RestApi restApi;

  /**
   * Creates a new data API client.
   *
   * @param orgDomainUrl The domain URL of the Salesforce org, e.g. {@code
   *     https://example.my.salesforce.com}.
   * @param apiVersion The Salesforce API version to use, e.g. {@code 62.0}.
   * @param accessToken The access token used to authenticate with the Salesforce org.
   */
  public DataApi(URI orgDomainUrl, String apiVersion, String accessToken) {
    this.restApi = new RestApi(orgDomainUrl, apiVersion, accessToken);
  }

  /**
   * Queries for records with a given SOQL string.
   *
   * @param soql The SOQL string.
   * @return a {@link RecordQueryResult} that contains the queried data.
   * @throws DataApiException If error occurred during the query.
   * @see <a
   *     href="https://developer.salesforce.com/docs/atlas.en-us.soql_sosl.meta/soql_sosl/sforce_api_calls_soql.htm">Salesforce
   *     Object Query Language (SOQL)</a>
   */
  @Nonnull
  public RecordQueryResult query(String soql) throws DataApiException {
    return convertQueryRecordResult(executeRequest(new QueryRecordRestApiRequest(soql)));
  }

  /**
   * Queries for more records, based on the given {@link RecordQueryResult}.
   *
   * @param queryResult The query result to query more data for.
   * @return A new {@link RecordQueryResult} with additional data or an empty one if the given
   *     RecordQueryResult was already in the done state.
   * @throws DataApiException If error occurred during the query.
   * @see RecordQueryResult#isDone()
   */
  @Nonnull
  public RecordQueryResult queryMore(RecordQueryResult queryResult) throws DataApiException {
    if (queryResult.getNextRecordsPath().isPresent()) {
      return convertQueryRecordResult(
          executeRequest(
              new QueryNextRecordsRestApiRequest(queryResult.getNextRecordsPath().get())));
    }

    return new RecordQueryResult(queryResult.isDone(), queryResult.getTotalSize());
  }

  /**
   * Creates a new record of the given type with the given fields.
   *
   * @param record The record to create.
   * @return A {@link RecordModificationResult} for this operation.
   * @throws DataApiException If an API error occurred during record creation.
   */
  @Nonnull
  public RecordModificationResult create(Record record) throws DataApiException {
    return new RecordModificationResult(executeRequest(apiRequestForCreate(record)).getId());
  }

  /**
   * Updates an existing record of the given type and id with the given fields.
   *
   * @param record The record to update.
   * @return A {@link RecordModificationResult} for this operation.
   * @throws DataApiException If an API error occurred during record update.
   * @throws IllegalArgumentException If the {@link Record} does not have an Id field.
   */
  @Nonnull
  public RecordModificationResult update(Record record) throws DataApiException {
    return new RecordModificationResult(executeRequest(apiRequestForUpdate(record)).getId());
  }

  /**
   * Deletes an existing record of the given type and id.
   *
   * @param type The object type of the record to delete.
   * @param id The id of the record to delete.
   * @return A {@link RecordModificationResult} for this operation.
   * @throws DataApiException If an API error occurred during record deletion.
   */
  @Nonnull
  public RecordModificationResult delete(String type, String id) throws DataApiException {
    return new RecordModificationResult(
        executeRequest(new DeleteRecordRestApiRequest(type, id)).getId());
  }

  /**
   * Creates a new and empty RecordBuilder that can be used to build a {@link Record} object for use
   * with the {@link #update(Record)} and {@link #create(Record)} methods.
   *
   * @param type The type of the record to build.
   * @return A new and empty RecordBuilder.
   */
  @Nonnull
  public RecordBuilder newRecordBuilder(String type) {
    return new RecordBuilder(type);
  }

  /**
   * Creates a new RecordBuilder, pre-initialized from the given {@link Record}. The type and all
   * fields present in that Record will be set on the returned RecordBuilder.
   *
   * @param record The {@link Record} to copy the type and fields from.
   * @return A new RecordBuilder, pre-initialized from the given {@link Record}.
   */
  @Nonnull
  public RecordBuilder newRecordBuilder(Record record) {
    return new RecordBuilder(record.getType(), record.getFieldValues());
  }

  /**
   * Creates a new and empty UnitOfWorkBuilder that can be used to build a {@link UnitOfWork} object
   * for the {@link #commitUnitOfWork(UnitOfWork)} method.
   *
   * @return A new and empty FieldsBuilder.
   */
  @Nonnull
  public UnitOfWorkBuilder newUnitOfWorkBuilder() {
    return new UnitOfWorkBuilder();
  }

  /**
   * Commits a {@link UnitOfWork}, executing all operations registered with it. If any of these
   * operations fail, the whole unit is rolled back. To examine results for a single operation,
   * inspect the returned map (which is keyed with {@link ReferenceId} returned from {@link
   * UnitOfWorkBuilder#registerCreate(Record)}, {@link UnitOfWorkBuilder#registerUpdate(Record)},
   * and {@link UnitOfWorkBuilder#registerDelete(String, String)}.
   *
   * @param unitOfWork The {@link UnitOfWork} to commit.
   * @return A map of {@link RecordModificationResult}s, indexed by their {@link ReferenceId}s.
   * @throws DataApiException If an error occurred while committing the UnitOfWork.
   * @see #newUnitOfWorkBuilder()
   */
  @Nonnull
  public Map<ReferenceId, RecordModificationResult> commitUnitOfWork(UnitOfWork unitOfWork)
      throws DataApiException {

    CompositeGraphRestApiRequest<ModifyRecordResult> request =
        new CompositeGraphRestApiRequest<>(
            restApi.getOrgDomainUrl(), restApi.getApiVersion(), unitOfWork.getSubrequests());

    Map<String, ModifyRecordResult> result = executeRequest(request);

    Map<ReferenceId, RecordModificationResult> actualResult = new HashMap<>();
    for (Map.Entry<String, ModifyRecordResult> entry : result.entrySet()) {
      actualResult.put(
          new ReferenceId(entry.getKey()), new RecordModificationResult(entry.getValue().getId()));
    }

    return actualResult;
  }

  /**
   * Returns the access token used by this API client. Can be used to initialize a third-party API
   * client or to perform custom API calls with a HTTP library.
   *
   * @return the access token used by this API client.
   */
  @Nonnull
  public String getAccessToken() {
    return restApi.getAccessToken();
  }

  static JsonRestApiRequest<ModifyRecordResult> apiRequestForUpdate(Record record) {
    String id =
        record
            .getStringField("id")
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Given Record does not have an Id field and therefore cannot be updated."));

    Map<String, JsonElement> fieldValues = BinaryFieldUtil.convert(record.getFieldValues());
    fieldValues.remove("id");

    return new UpdateRecordRestApiRequest(id, record.getType(), fieldValues);
  }

  static JsonRestApiRequest<ModifyRecordResult> apiRequestForCreate(Record record) {
    return new CreateRecordRestApiRequest(
        record.getType(), BinaryFieldUtil.convert(record.getFieldValues()));
  }

  private RecordQueryResult convertQueryRecordResult(QueryRecordResult queryRecordResult)
      throws DataApiException {
    try {
      return BinaryFieldUtil.convert(queryRecordResult, restApi);
    } catch (URISyntaxException | IOException e) {
      throw new DataApiException("Could not process API response!", e);
    }
  }

  private <T> T executeRequest(JsonRestApiRequest<T> request) throws DataApiException {
    try {
      return restApi.execute(request);
    } catch (RestApiErrorsException restApiException) {
      throw mapException(restApiException);
    } catch (RestApiException e) {
      throw new DataApiException("Exception while executing API request!", e);
    } catch (IOException e) {
      throw new DataApiException("IOException while executing API request!", e);
    }
  }

  private static DataApiException mapException(RestApiErrorsException exception) {
    StringBuilder builder = new StringBuilder("One or more API errors occurred:\n");
    exception
        .getApiErrors()
        .forEach(error -> {
          builder.append("\n");

          builder.append("Code: ");
          builder.append(error.getErrorCode());
          builder.append("\n");

          builder.append("Message: ");
          builder.append(error.getMessage());
          builder.append("\n");

          builder.append("Fields: ");
          builder.append(String.join(", ", error.getFields()));
          builder.append("\n");
        });

    return new DataApiException(
        builder.toString(),
        exception.getApiErrors().stream()
            .map(error ->
                new DataApiError(error.getMessage(), error.getErrorCode(), error.getFields()))
            .collect(Collectors.toList()));
  }
}
