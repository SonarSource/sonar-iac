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
package org.sonar.iac.common.extension.visitors;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.error.NewAnalysisError;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.SecondaryLocation;

public class InputFileContext extends TreeContext {

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

      if (textRange != null) {
        issueLocation.at(textRange);
      }

      issue.forRule(ruleKey).at(issueLocation);

      secondaryLocations.stream()
        .filter(Objects::nonNull)
        .forEach(secondary -> issue.addLocation(
          issue.newLocation()
            .on(inputFile)
            .at(secondary.textRange)
            .message(secondary.message)));

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

  private static int issueHash(RuleKey ruleKey, @Nullable TextRange textRange, List<SecondaryLocation> secondaryLocations) {
    return Objects.hash(ruleKey, textRange, secondaryLocations);
  }
}
