/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension.visitors;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.error.NewAnalysisError;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.sonar.iac.common.extension.IacSensor.isFailFast;

public class InputFileContext extends TreeContext {

  private static final Logger LOG = LoggerFactory.getLogger(InputFileContext.class);
  private static final String PARSING_ERROR_RULE_KEY = "S2260";

  public final SensorContext sensorContext;
  public final InputFile inputFile;

  private final Set<RaisedIssue> raisedIssues = new HashSet<>();

  public InputFileContext(SensorContext sensorContext, InputFile inputFile) {
    this.sensorContext = sensorContext;
    this.inputFile = inputFile;
  }

  public void reportIssue(RuleKey ruleKey, @Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
    // We avoid raising an issue on text ranges on which we already raised one. This is to avoid duplicate ones which might happen, for example,
    // with Yaml anchors SONARIAC-78.
    var raisedIssue = new RaisedIssue(ruleKey, textRange, secondaryLocations);
    if (raisedIssues.add(raisedIssue)) {
      NewIssue issue = sensorContext.newIssue();
      NewIssueLocation issueLocation = issue.newLocation().on(inputFile).message(message);

      if (textRange != null && TextRanges.isValidAndNotEmpty(textRange)) {
        issueLocation.at(toInputFileRange(inputFile, textRange));
      }

      issue.forRule(ruleKey).at(issueLocation);

      secondaryLocations.stream()
        .filter(secondary -> secondary != null && TextRanges.isValidAndNotEmpty(secondary.textRange))
        .forEach(secondary -> {
          var newIssueLocation = newLocation(issue, secondary);
          if (newIssueLocation != null) {
            issue.addLocation(newIssueLocation);
          }
        });

      issue.save();
    }
  }

  public void reportParseError(String repositoryKey, @Nullable TextPointer location) {
    reportAnalysisError("Unable to parse file: " + inputFile, location);
    RuleKey parsingErrorRuleKey = RuleKey.of(repositoryKey, PARSING_ERROR_RULE_KEY);
    if (sensorContext.activeRules().find(parsingErrorRuleKey) == null) {
      return;
    }
    NewIssue parseError = sensorContext.newIssue();
    NewIssueLocation parseErrorLocation = parseError.newLocation()
      .on(inputFile)
      .message("A parsing error occurred in this file.");

    Optional.ofNullable(location)
      .map(TextPointer::line)
      .map(inputFile::selectLine)
      .ifPresent(parseErrorLocation::at);

    parseError
      .forRule(parsingErrorRuleKey)
      .at(parseErrorLocation)
      .save();
  }

  public void reportAnalysisError(String message, @Nullable TextPointer location) {
    NewAnalysisError error = sensorContext.newAnalysisError();
    error
      .message(message)
      .onFile(inputFile);

    if (location != null) {
      TextPointer pointerLocation = null;
      try {
        pointerLocation = inputFile.newPointer(location.line(), location.lineOffset());
      } catch (IllegalArgumentException e) {
        LOG.debug("Error when creating valid line offset of pointer, fallback to beginning of the file.", e);
        // to be always on safe side
        pointerLocation = inputFile.newPointer(1, 0);
      }
      error.at(pointerLocation);
    }

    error.save();
  }

  @CheckForNull
  private NewIssueLocation newLocation(NewIssue newIssue, SecondaryLocation secondaryLocation) {
    var fileToRaiseOn = retrieveFileToRaiseOn(secondaryLocation);
    if (fileToRaiseOn != null) {
      return newIssue.newLocation()
        .on(fileToRaiseOn)
        .at(toInputFileRange(fileToRaiseOn, secondaryLocation.textRange))
        .message(secondaryLocation.message);
    }
    return null;
  }

  @CheckForNull
  public InputFile retrieveFileToRaiseOn(SecondaryLocation secondaryLocation) {
    if (secondaryLocation.filePath == null) {
      return inputFile;
    }
    return sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().is(new File(secondaryLocation.filePath)));
  }

  public TextPointer newPointer(int line, int lineOffset) {
    try {
      return inputFile.newPointer(line, lineOffset);
    } catch (IllegalArgumentException e) {
      var message = "Unable to create new pointer for %s position %s:%s".formatted(inputFile, line, lineOffset);
      LOG.warn(message, e);
      if (isFailFast(sensorContext)) {
        throw new IllegalStateException(message, e);
      }
    }
    return inputFile.newPointer(1, 0);
  }

  private org.sonar.api.batch.fs.TextRange toInputFileRange(InputFile inputFile, TextRange textRange) {
    try {
      return inputFile.newRange(textRange.start().line(), textRange.start().lineOffset(), textRange.end().line(), textRange.end().lineOffset());
    } catch (IllegalArgumentException e) {
      var message = "Unable to create new range for %s and range %s".formatted(inputFile, textRange);
      LOG.warn(message, e);
      if (isFailFast(sensorContext)) {
        throw new IllegalStateException(message, e);
      }
    }
    return inputFile.newRange(1, 0, 1, 1);
  }

  record RaisedIssue(RuleKey ruleKey, @Nullable TextRange textRange, List<SecondaryLocation> secondaryLocations) {
  }
}
