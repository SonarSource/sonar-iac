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
package org.sonar.iac.common.extension.visitors;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

public class InputFileContext extends TreeContext {

  private static final Logger LOG = LoggerFactory.getLogger(InputFileContext.class);
  private static final String PARSING_ERROR_RULE_KEY = "S2260";

  public final SensorContext sensorContext;
  public final InputFile inputFile;

  private final Set<Integer> raisedIssues = new HashSet<>();

  public InputFileContext(SensorContext sensorContext, InputFile inputFile) {
    this.sensorContext = sensorContext;
    this.inputFile = inputFile;
  }

  public void reportIssue(RuleKey ruleKey, @Nullable TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
    // We avoid raising an issue on text ranges on which we already raised one. This is to avoid duplicate ones which might happen, for example,
    // with Yaml anchors SONARIAC-78.
    if (raisedIssues.add(issueHash(ruleKey, textRange, secondaryLocations))) {
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
      TextPointer pointerLocation = inputFile.newPointer(location.line(), location.lineOffset());
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

  public TextPointer newPointer(int line, int lineOffset, boolean failFast) {
    try {
      return inputFile.newPointer(line, lineOffset);
    } catch (IllegalArgumentException e) {
      var message = "Unable to create new pointer for %s position %s:%s".formatted(inputFile, line, lineOffset);
      LOG.warn(message, e);
      if (failFast) {
        throw new IllegalStateException(message, e);
      }
    }
    return inputFile.newPointer(1, 0);
  }

  private static org.sonar.api.batch.fs.TextRange toInputFileRange(InputFile inputFile, TextRange textRange) {
    return inputFile.newRange(textRange.start().line(), textRange.start().lineOffset(), textRange.end().line(), textRange.end().lineOffset());
  }

  private static int issueHash(RuleKey ruleKey, @Nullable TextRange textRange, List<SecondaryLocation> secondaryLocations) {
    return Objects.hash(ruleKey, textRange, secondaryLocations);
  }
}
