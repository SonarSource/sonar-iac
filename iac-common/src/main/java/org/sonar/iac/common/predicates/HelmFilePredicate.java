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

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.filesystem.FileSystemUtils;
import org.sonar.iac.common.languages.IacLanguage;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

/**
 * Matches Helm project files: YAML files belonging to a Helm chart ({@code values.yaml}, {@code Chart.yaml},
 * {@code templates/**}) and Helm {@code .tpl} templates (which are not necessarily YAML). A file is matched only if it
 * belongs to a Helm project, i.e. its directory hierarchy contains a {@code Chart.yaml}. Plain Kubernetes manifests are
 * matched separately by {@link KubernetesFilePredicate}.
 */
public class HelmFilePredicate extends AbstractTimedFilePredicate implements YamlFileTypePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(HelmFilePredicate.class);
  private static final String[] HELM_YAML_PATH_PATTERNS = {"**/templates/**", "**/values.yaml", "**/values.yml", "**/Chart.yaml"};
  private static final String TPL_TEMPLATE_PATH_PATTERN = "**/templates/*.tpl";

  private final FileSystem fileSystem;
  private final FilePredicate delegate;
  private final boolean enablePredicateDebugLogs;

  public HelmFilePredicate(FileSystem fileSystem, boolean enablePredicateDebugLogs) {
    this.fileSystem = fileSystem;
    this.enablePredicateDebugLogs = enablePredicateDebugLogs;
    var predicates = fileSystem.predicates();
    // Helm YAML files are language-gated; .tpl templates are not, as they are not valid YAML and have no YAML language.
    var helmYamlFile = predicates.and(
      predicates.hasLanguages(YAML_LANGUAGE_KEY, IacLanguage.KUBERNETES.getKey()),
      predicates.matchesPathPatterns(HELM_YAML_PATH_PATTERNS));
    var tplTemplateFile = predicates.matchesPathPattern(TPL_TEMPLATE_PATH_PATTERN);
    this.delegate = predicates.and(
      predicates.or(helmYamlFile, tplTemplateFile),
      this::isHelmProjectMember);
  }

  private boolean isHelmProjectMember(InputFile inputFile) {
    return FileSystemUtils.retrieveHelmProjectFolder(Path.of(inputFile.uri()), fileSystem) != null;
  }

  @Override
  protected boolean accept(InputFile inputFile) {
    var matches = delegate.apply(inputFile);
    if (matches && enablePredicateDebugLogs) {
      LOG.debug("Identified as Helm file: {}", inputFile);
    }
    return matches;
  }

  @Override
  public FileType fileType() {
    return FileType.HELM;
  }
}
