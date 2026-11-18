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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.iac.common.languages.IacLanguage;

public class DockerComposeFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {

  private static final Logger LOG = LoggerFactory.getLogger(DockerComposeFilePredicate.class);
  // Filename globs, matched case-insensitively.
  private static final WildcardPattern[] PATTERNS = WildcardPattern.create(new String[] {
    "compose.yaml", "compose.yml",
    "docker-compose.yaml", "docker-compose.yml",
    "compose.*.yaml", "compose.*.yml",
    "docker-compose.*.yaml", "docker-compose.*.yml",
    "docker.compose*.yaml", "docker.compose*.yml",
    "docker_compose*.yaml", "docker_compose*.yml",
    "docker-compose-*.yaml", "docker-compose-*.yml",
    "*-compose.yaml", "*-compose.yml",
    "*.docker-compose.yaml", "*.docker-compose.yml"
  });
  private static final WildcardPattern[] EXCLUSIONS = WildcardPattern.create(new String[] {
    "*.j2", "*.md", "*.markdown",
    "kustomization.yaml", "kustomization.yml", "kustomization"
  });

  private final FilePredicate candidateLanguages;
  private final FilePredicate githubWorkflowPath;
  private final boolean enablePredicateDebugLogs;

  public DockerComposeFilePredicate(FilePredicates predicates, boolean enablePredicateDebugLogs) {
    candidateLanguages = predicates.hasLanguages(IacLanguage.YAML.getKey(), IacLanguage.DOCKER_COMPOSE.getKey());
    githubWorkflowPath = predicates.matchesPathPattern("**/.github/workflows/**");
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    if (!candidateLanguages.apply(inputFile)) {
      return false;
    }
    var filename = inputFile.filename().toLowerCase(Locale.ROOT);
    if (WildcardPattern.match(EXCLUSIONS, filename) || githubWorkflowPath.apply(inputFile)
      || (!IacLanguage.DOCKER_COMPOSE.getKey().equals(inputFile.language()) && !WildcardPattern.match(PATTERNS, filename))) {
      return false;
    }
    if (enablePredicateDebugLogs) {
      LOG.debug("Identified as Docker Compose file: {}", inputFile);
    }
    return true;
  }

  @Override
  public FileType fileType() {
    return FileType.DOCKER_COMPOSE;
  }
}
