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
package org.sonar.iac.common.predicates;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.YamlIdentifierFilePredicate;

public class AzurePipelinesFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {

  private static final Logger LOG = LoggerFactory.getLogger(AzurePipelinesFilePredicate.class);
  private static final Set<String> IDENTIFIER_PATTERNS = Set.of("^trigger:", "^variables:", "^pool:", "^steps:", "^stages:", "^parameters:", "^pr:", "^resources:");
  private static final String[] FILE_PATH_PATTERNS = {
    "**/.azure-pipelines/**/*.yaml",
    "**/.azure-pipelines/**/*.yml",
    "**/.azure-pipelines.yaml",
    "**/.azure-pipelines.yml",
    "**/.azure-pipelines-*.yaml",
    "**/.azure-pipelines-*.yml",
    "**/azure-pipelines/**/*.yaml",
    "**/azure-pipelines/**/*.yml",
    "**/azure-pipelines.yaml",
    "**/azure-pipelines.yml",
    "**/azure-pipeline/**/*.yaml",
    "**/azure-pipeline/**/*.yml",
    "**/azure-pipeline.yaml",
    "**/azure-pipeline.yml",
    "**/azure-pipeline-*.yaml",
    "**/azure-pipeline-*.yml"
  };
  private final FilePredicate delegate;
  private final boolean enablePredicateDebugLogs;

  public AzurePipelinesFilePredicate(FilePredicates predicates, boolean enablePredicateDebugLogs) {
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
    this.delegate = predicates.or(
      predicates.matchesPathPatterns(FILE_PATH_PATTERNS),
      new YamlIdentifierFilePredicate(IDENTIFIER_PATTERNS, 1));
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    if (delegate.apply(inputFile)) {
      if (enablePredicateDebugLogs) {
        LOG.debug("Identified as Azure pipelines file: {}", inputFile);
      }
      return true;
    }
    return false;
  }

  @Override
  public FileType fileType() {
    return FileType.AZURE_PIPELINES;
  }
}
