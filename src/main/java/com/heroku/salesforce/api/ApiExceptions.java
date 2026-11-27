/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api;

import com.heroku.salesforce.api.internal.RestApiErrorsException;
import com.heroku.salesforce.api.internal.RestApiException;
import java.io.IOException;
import java.util.stream.Collectors;

final class ApiExceptions {
  @FunctionalInterface
  interface ClientCall<T> {
    T call() throws RestApiErrorsException, RestApiException, IOException;
  }

  static <T> T translate(ClientCall<T> call) throws DataApiException {
    try {
      return call.call();
    } catch (RestApiErrorsException e) {
      throw fromApiErrors(e);
    } catch (RestApiException e) {
      throw new DataApiException("Exception while executing API request!", e);
    } catch (IOException e) {
      throw new DataApiException("IOException while executing API request!", e);
    }
  }

  private static DataApiException fromApiErrors(RestApiErrorsException exception) {
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

  private ApiExceptions() {}
}
