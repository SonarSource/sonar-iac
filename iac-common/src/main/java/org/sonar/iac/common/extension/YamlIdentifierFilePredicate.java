/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.extension;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

import static org.sonar.iac.common.yaml.YamlSensor.FILE_SEPARATOR;

public class YamlIdentifierFilePredicate implements FilePredicate {

  private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");
  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final Logger LOG = LoggerFactory.getLogger(YamlIdentifierFilePredicate.class);
  private final List<Predicate<String>> identifierPatterns;
  private final int requiredMatches;

  public YamlIdentifierFilePredicate(Set<String> patternsIdentifiers, int requiredMatches) {
    this.identifierPatterns = patternsIdentifiers.stream().map(pattern -> Pattern.compile(pattern).asPredicate()).toList();
    this.requiredMatches = requiredMatches;
  }

  public YamlIdentifierFilePredicate(Set<String> patternsIdentifiers) {
    this(patternsIdentifiers, patternsIdentifiers.size());
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return hasExpectedStructure(inputFile);
  }

  private boolean hasExpectedStructure(InputFile inputFile) {
    try (var bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
      // Only first 8k bytes is read to avoid slow execution for big one-line files
      byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
      var text = new String(bytes, inputFile.charset());
      return isTextMatchingRequiredIdentifiers(text);
    } catch (IOException e) {
      LOG.error("Unable to read file: {}.", inputFile);
      LOG.error(e.getMessage());
      return false;
    }
  }

  private boolean isTextMatchingRequiredIdentifiers(String text) {
    var identifierCount = 0;
    String[] lines = LINE_TERMINATOR.split(text);
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
