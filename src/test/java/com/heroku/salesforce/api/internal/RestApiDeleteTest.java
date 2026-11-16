/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RestApiDeleteTest {
  @RegisterExtension
  static WireMockExtension wireMock =
      WireMockExtension.newInstance().options(wireMockConfig().port(8080)).build();

  private final RestApi restApi =
      new RestApi(URI.create("http://localhost:8080/"), "53.0", "00DJS0000000123!AQ0AQ.token");

  @Test
  public void delete() throws RestApiErrorsException, IOException, RestApiException {
    DeleteRecordRestApiRequest deleteApiRequest =
        new DeleteRecordRestApiRequest("Account", "001JS0000000002YAA");

    ModifyRecordResult result = restApi.execute(deleteApiRequest);

    assertThat(result.getId(), is(equalTo("001JS0000000002YAA")));
  }

  @Test
  public void deleteAlreadyDeletedTest() throws IOException, RestApiException {
    DeleteRecordRestApiRequest deleteApiRequest =
        new DeleteRecordRestApiRequest("Account", "001JS0000000001YAA");

    try {
      restApi.execute(deleteApiRequest);
    } catch (RestApiErrorsException e) {
      assertThat(
          e.getApiErrors(),
          contains(
              pojo(RestApiError.class)
                  .withProperty("message", is(equalTo("entity is deleted")))
                  .withProperty("errorCode", is(equalTo("ENTITY_IS_DELETED")))
                  .withProperty("fields", is(empty()))));

      return;
    }

    Assertions.fail("Expected RestApiErrorsException!");
  }
}
