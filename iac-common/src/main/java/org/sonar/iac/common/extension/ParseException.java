/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.common.extension;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public class ParseException extends RuntimeException {

  private final transient TextPointer position;
  private final transient String details;

  public static ParseException throwGeneralParseException(String action, @Nullable InputFile inputFile, Exception cause, @Nullable TextPointer position) {
    String message = String.format("Cannot %s '%s'", action, filenameAndPosition(inputFile, position));
    return new ParseException(message, position, cause.getMessage());
  }

  public static ParseException throwParseException(String message, @Nullable InputFileContext inputFileContext, @Nullable TextPointer position) {
    if (inputFileContext != null) {
      return new ParseException(message + " at " + filenameAndPosition(inputFileContext.inputFile, position), position, null);
    }
    return new ParseException(message + " at " + filenameAndPosition(null, position), position, null);
  }

  private static String filenameAndPosition(@Nullable InputFile inputFile, @Nullable TextPointer position) {
    String filename = inputFile != null ? inputFile.toString() : "null";
    if (position != null) {
      return String.format("%s:%s:%s", filename, position.line(), position.lineOffset() + 1);
    } else {
      return filename;
    }
  }

  public ParseException(String message, @Nullable TextPointer position, @Nullable String details) {
    super(message);
    this.position = position;
    this.details = details;
  }

  @CheckForNull
  public TextPointer getPosition() {
    return position;
  }

  @CheckForNull
  public String getDetails() {
    return details;
  }
}
