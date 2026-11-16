/*
 * Copyright (c) 2026, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.heroku.salesforce.api.internal;

public class BodyParsingException extends Exception {
  public BodyParsingException() {
    super();
  }

  public BodyParsingException(String message) {
    super(message);
  }

  public BodyParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public BodyParsingException(Throwable cause) {
    super(cause);
  }
}
