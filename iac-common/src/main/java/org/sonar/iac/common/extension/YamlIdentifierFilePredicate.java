/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.extension;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.FILE_SEPARATOR;

public class YamlIdentifierFilePredicate implements FilePredicate {

  private static final Logger LOG = LoggerFactory.getLogger(YamlIdentifierFilePredicate.class);
  private final List<Predicate<String>> identifierPatterns;
  private final int requiredMatches;
  private final SharedFileHeadReader sharedFileHeadReader;

  public YamlIdentifierFilePredicate(Set<String> patternsIdentifiers, int requiredMatches, SharedFileHeadReader sharedFileHeadReader) {
    this.identifierPatterns = patternsIdentifiers.stream().map(pattern -> Pattern.compile(pattern).asPredicate()).toList();
    this.requiredMatches = requiredMatches;
    this.sharedFileHeadReader = sharedFileHeadReader;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return hasExpectedStructure(inputFile);
  }

  private boolean hasExpectedStructure(InputFile inputFile) {
    try {
      String[] lines = sharedFileHeadReader.readLines(inputFile);
      return isTextMatchingRequiredIdentifiers(lines);
    } catch (IOException e) {
      LOG.warn("Unable to read file: {}.", inputFile);
      LOG.warn(e.getMessage());
      return false;
    }
  }

  private boolean isTextMatchingRequiredIdentifiers(String[] lines) {
    var identifierCount = 0;
    for (String line : lines) {
      if (identifierPatterns.stream().anyMatch(pred -> pred.test(line))) {
        identifierCount++;
      } else if (FILE_SEPARATOR.equals(line)) {
        identifierCount = 0;
      }
      if (identifierCount == requiredMatches) {
        return true;
      }
    }
    return false;
  }
}
