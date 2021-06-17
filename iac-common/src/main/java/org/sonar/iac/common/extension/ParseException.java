/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextPointer;

public class ParseException extends RuntimeException {

  private final transient TextPointer position;

  public ParseException(String message, @Nullable TextPointer position) {
    super(message);
    this.position = position;
  }

  @CheckForNull
  public TextPointer getPosition() {
    return position;
  }
}
