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
package org.sonar.iac.kubernetes.plugin;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.analyzer.Analyzer;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.kubernetes.visitors.ProjectContextEnricherVisitor;

public class KubernetesAnalyzerFactory {

  private KubernetesAnalyzerFactory() {
    // util class
  }

  /**
   * It creates a {@link KubernetesAnalyzer} used for updating {@link ProjectContext} when files changes in SonarLint.
   * The difference between this one and created by
   * {@link KubernetesAnalyzerFactory#createAnalyzer(List, DurationStatistics, HelmProcessor, KubernetesParserStatistics, TreeVisitor, SonarLintFileListener)}
   * is that this one uses only {@link ProjectContextEnricherVisitor}.
   */
  public static Analyzer createAnalyzerForUpdatingProjectContext(
    List<TreeVisitor<InputFileContext>> visitors,
    DurationStatistics statistics,
    @Nullable HelmProcessor helmProcessor,
    SonarLintFileListener sonarLintFileListener) {

    return createAnalyzer(
      visitors,
      statistics,
      helmProcessor,
      new KubernetesParserStatistics(),
      new EmptyChecksVisitor(),
      sonarLintFileListener);
  }

  public static Analyzer createAnalyzer(
    List<TreeVisitor<InputFileContext>> visitors,
    DurationStatistics statistics,
    @Nullable HelmProcessor helmProcessor,
    KubernetesParserStatistics kubernetesParserStatistics,
    TreeVisitor<InputFileContext> checksVisitor,
    @Nullable SonarLintFileListener sonarLintFileListener) {

    return new KubernetesAnalyzer(
      KubernetesExtension.REPOSITORY_KEY,
      new YamlParser(),
      visitors,
      statistics,
      new HelmParser(helmProcessor),
      kubernetesParserStatistics,
      checksVisitor,
      sonarLintFileListener);
  }
}
