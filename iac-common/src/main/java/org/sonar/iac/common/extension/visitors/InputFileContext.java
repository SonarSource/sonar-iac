/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
    // We avoid raising an issue on text ranges on which we already raised one. This is to avoid duplicate ones which might happen, for example, with Yaml anchors SONARIAC-78.
    // Once we'll need to introduce a secondary locations mechanism, a more sophisticated mechanism has to be used to detect duplicates.
    if (raisedIssues.add(issueHash(ruleKey, textRange))) {
      NewIssue issue = sensorContext.newIssue();
      NewIssueLocation issueLocation = issue.newLocation().on(inputFile).message(message);

      if (textRange != null) {
        issueLocation.at(textRange);
      }

      issue.forRule(ruleKey).at(issueLocation);

      secondaryLocations.forEach(secondary -> issue.addLocation(
        issue.newLocation()
          .on(inputFile)
          .at(secondary.textRange)
          .message(secondary.message)
      ));

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

  private static int issueHash(RuleKey ruleKey, @Nullable TextRange textRange) {
    return Objects.hash(ruleKey, textRange);
  }
}
