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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.AbstractTimedFilePredicate;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.YamlIdentifierFilePredicate;

public class AzurePipelinesFilePredicate extends AbstractTimedFilePredicate {

  private static final Logger LOG = LoggerFactory.getLogger(AzurePipelinesFilePredicate.class);
  private static final Set<String> IDENTIFIER_PATTERNS = Set.of("^trigger:", "^variables:", "^pool:", "^steps:", "^stages:", "^parameters:", "^pr:", "^resources:");
  private final FilePredicate identifierPredicate;
  private final boolean enablePredicateDebugLogs;

  public AzurePipelinesFilePredicate(boolean enablePredicateDebugLogs, DurationStatistics.Timer timer) {
    super(timer);
    identifierPredicate = new YamlIdentifierFilePredicate(IDENTIFIER_PATTERNS, 1);
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    if (identifierPredicate.apply(inputFile)) {
      if (enablePredicateDebugLogs) {
        LOG.debug("Identified as Azure pipelines file: {}", inputFile);
      }
      return true;
    }
    return false;
  }
}
