/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.plugin;

import com.sonar.sslr.api.RecognitionException;
import java.io.IOException;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.iac.terraform.parser.HclParser;
import org.sonar.plugins.iac.terraform.parser.ParseException;
import org.sonarsource.analyzer.commons.ProgressReport;

public class Analyzer {
  private static final Logger LOG = Loggers.get(Analyzer.class);
  private static final Pattern EMPTY_FILE_CONTENT_PATTERN = Pattern.compile("\\s*+");

  private final HclParser parser;

  private Analyzer(HclParser parser) {
    this.parser = parser;
  }

  static Analyzer create(HclParser parser) {
    return new Analyzer(parser);
  }

  boolean analyseFiles(SensorContext sensorContext, Iterable<InputFile> inputFiles, ProgressReport progressReport) {
    for (InputFile inputFile : inputFiles) {
      if (sensorContext.isCancelled()) {
        return false;
      }
      InputFileContext inputFileContext = new InputFileContext(sensorContext, inputFile);
      try {
        analyseFile(inputFileContext);
      } catch (ParseException e) {
        logParsingError(inputFile, e);
        inputFileContext.reportParseError(e.getPosition());
      }
      progressReport.nextFile();
    }
    return true;
  }

  private void analyseFile(InputFileContext inputFileContext) {
    InputFile inputFile = inputFileContext.inputFile;
    String content;
    try {
      content = inputFile.contents();
    } catch (IOException | RuntimeException e) {
      throw toParseException("read", inputFile, e);
    }

    if (EMPTY_FILE_CONTENT_PATTERN.matcher(content).matches()) {
      return;
    }

    try {
      parser.parse(content);
    } catch (RuntimeException e) {
      throw toParseException("parse", inputFile, e);
    }
  }

  private static ParseException toParseException(String action, InputFile inputFile, Exception cause) {
    TextPointer position = null;
    if (cause instanceof RecognitionException) {
      position = inputFile.newPointer(((RecognitionException) cause).getLine(), 0);
    }
    return new ParseException("Cannot " + action + " '" + inputFile + "': " + cause.getMessage(), position);
  }

  private static void logParsingError(InputFile inputFile, ParseException e) {
    TextPointer position = e.getPosition();
    String positionMessage = "";
    if (position != null) {
      positionMessage = String.format("Parse error at position %s:%s", position.line(), position.lineOffset());
    }
    LOG.error(String.format("Unable to parse file: %s. %s", inputFile.uri(), positionMessage));
    LOG.error(e.getMessage());
  }
}
