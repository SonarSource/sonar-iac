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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.YamlIdentifierFilePredicate;
import org.sonar.iac.common.languages.IacLanguage;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

/**
 * Matches plain Kubernetes manifests: a YAML (or Kubernetes language) file whose content carries the Kubernetes
 * identifiers (apiVersion, kind, metadata). Helm project files are matched separately by {@link HelmFilePredicate}.
 */
public class KubernetesFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesFilePredicate.class);
  // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
  private static final Set<String> IDENTIFIER_PATTERNS = Set.of("^apiVersion", "^kind", "^metadata");

  private final FilePredicate identifierPredicate;
  private final FilePredicate delegate;
  private final boolean enablePredicateDebugLogs;

  public KubernetesFilePredicate(FileSystem fileSystem, boolean enablePredicateDebugLogs) {
    var predicates = fileSystem.predicates();
    this.identifierPredicate = new YamlIdentifierFilePredicate(IDENTIFIER_PATTERNS);
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
    this.delegate = predicates.and(
      predicates.hasLanguages(YAML_LANGUAGE_KEY, IacLanguage.KUBERNETES.getKey()),
      this::hasKubernetesIdentifier);
  }

  private boolean hasKubernetesIdentifier(InputFile inputFile) {
    if (identifierPredicate.apply(inputFile)) {
      return true;
    }
    if (enablePredicateDebugLogs) {
      LOG.debug("File without Kubernetes identifier: {}", inputFile);
    }
    return false;
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    return delegate.apply(inputFile);
  }

  @Override
  public FileType fileType() {
    return FileType.KUBERNETES;
  }
}
