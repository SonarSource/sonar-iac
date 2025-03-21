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
package org.sonar.iac.common.extension;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

public class FileIdentificationPredicate implements FilePredicate {

  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final Logger LOG = LoggerFactory.getLogger(FileIdentificationPredicate.class);
  private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");

  private final List<String> fileIdentifiers;
  private final boolean enablePredicateDebugLogs;

  public FileIdentificationPredicate(String fileIdentifier, boolean enablePredicateDebugLogs) {
    this(List.of(fileIdentifier), enablePredicateDebugLogs);
  }

  public FileIdentificationPredicate(List<String> fileIdentifiers, boolean enablePredicateDebugLogs) {
    this.fileIdentifiers = fileIdentifiers;
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    return hasFileIdentifier(inputFile);
  }

  private boolean hasFileIdentifier(InputFile inputFile) {
    if (hasEmptyIdentifier()) {
      return true;
    }

    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
      // Only first 8k bytes is read to avoid slow execution for big one-line files
      byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
      String text = new String(bytes, inputFile.charset());
      String[] lines = LINE_TERMINATOR.split(text);
      for (String line : lines) {
        if (containsAnyIdentifier(line)) {
          return true;
        }
      }
    } catch (IOException e) {
      LOG.warn("Unable to read file: {}.", inputFile);
      LOG.warn(e.getMessage());
    }
    if (enablePredicateDebugLogs) {
      if (fileIdentifiers.size() == 1) {
        var identifierLog = fileIdentifiers.get(0);
        LOG.debug("File without identifier '{}': {}", identifierLog, inputFile);
      } else {
        var identifierLog = Arrays.toString(fileIdentifiers.toArray());
        LOG.debug("File without any identifiers '{}': {}", identifierLog, inputFile);
      }
    }
    return false;
  }

  private boolean hasEmptyIdentifier() {
    return fileIdentifiers == null ||
      fileIdentifiers.isEmpty() ||
      (fileIdentifiers.size() == 1 && fileIdentifiers.get(0).isEmpty());
  }

  private boolean containsAnyIdentifier(String line) {
    for (String identifier : fileIdentifiers) {
      if (line.contains(identifier)) {
        return true;
      }
    }
    return false;
  }
}
