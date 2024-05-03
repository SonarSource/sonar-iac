/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.springconfig.parser.properties;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ExceptionUtils;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public class ErrorListener extends BaseErrorListener {
  private static final Logger LOG = LoggerFactory.getLogger(ErrorListener.class);

  @Nullable
  private InputFileContext inputFileContext;

  public ErrorListener(@Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
  }

  @Override
  public void syntaxError(
    Recognizer<?, ?> recognizer,
    Object offendingSymbol,
    int line,
    int charPositionInLine,
    String msg,
    @Nullable RecognitionException e) {

    TextPointer textPointer = new BasicTextPointer(line, charPositionInLine);
    var message = "Cannot parse, " + msg;
    LOG.debug(message);
    if (e != null) {
      var stackTrace = ExceptionUtils.getStackTrace(e);
      LOG.debug(stackTrace);
    }
    throw ParseException.createParseException(message, inputFileContext, textPointer);
  }
}
