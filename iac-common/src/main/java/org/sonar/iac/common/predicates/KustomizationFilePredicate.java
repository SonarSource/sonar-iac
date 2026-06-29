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

import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;

/**
 * Matches <a href="https://kustomize.io/">Kustomize</a> configuration files, identified solely by their file name
 * ({@code kustomization.yaml} or {@code kustomization.yml}, case-insensitive). Resolving them to a dedicated
 * {@link FileType#KUSTOMIZE} keeps them out of the content based YAML sensors (Azure Pipelines, CloudFormation, ...),
 * which would otherwise mistake a Kustomization for one of their own files (for example because of its
 * {@code resources:} key).
 */
public class KustomizationFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {

  private static final Logger LOG = LoggerFactory.getLogger(KustomizationFilePredicate.class);
  private static final Set<String> KUSTOMIZATION_FILE_NAMES = Set.of("kustomization.yaml", "kustomization.yml");

  private final boolean enablePredicateDebugLogs;

  public KustomizationFilePredicate(boolean enablePredicateDebugLogs) {
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    if (KUSTOMIZATION_FILE_NAMES.contains(inputFile.filename().toLowerCase(Locale.ROOT))) {
      if (enablePredicateDebugLogs) {
        LOG.debug("Identified as Kustomization file: {}", inputFile);
      }
      return true;
    }
    return false;
  }

  @Override
  public FileType fileType() {
    return FileType.KUSTOMIZE;
  }
}
