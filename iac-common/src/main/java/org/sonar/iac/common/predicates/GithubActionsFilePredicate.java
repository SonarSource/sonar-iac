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
package org.sonar.iac.common.predicates;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.AbstractTimedFilePredicate;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.YamlIdentifierFilePredicate;

public class GithubActionsFilePredicate extends AbstractTimedFilePredicate {

  private static final Logger LOG = LoggerFactory.getLogger(GithubActionsFilePredicate.class);
  private static final String[] WORKFLOW_FILE_PATTERNS = new String[] {"**/.github/workflows/*.yaml", "**/.github/workflows/*.yml"};
  private static final String[] METADATA_FILE_PATTERNS = new String[] {"**/action.yaml", "**/action.yml"};
  private static final Set<String> METADATA_FILE_IDENTIFIERS = Set.of("^name:", "^description:", "^runs:", "^\\s++using:", "^\\s++steps:");
  private static final int REQUIRED_IDENTIFIER_COUNT = 3;

  private final FilePredicate delegate;
  private final boolean enablePredicateDebugLogs;

  public GithubActionsFilePredicate(FilePredicates predicates, boolean enablePredicateDebugLogs, DurationStatistics.Timer timer) {
    super(timer);
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
    var actionFilePredicate = predicates.and(
      predicates.matchesPathPatterns(METADATA_FILE_PATTERNS),
      new YamlIdentifierFilePredicate(METADATA_FILE_IDENTIFIERS, REQUIRED_IDENTIFIER_COUNT));
    this.delegate = predicates.or(
      predicates.matchesPathPatterns(WORKFLOW_FILE_PATTERNS),
      actionFilePredicate);
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    var matches = delegate.apply(inputFile);
    if (matches && enablePredicateDebugLogs) {
      LOG.debug("Identified as Github file: {}", inputFile);
    }
    return matches;
  }
}
