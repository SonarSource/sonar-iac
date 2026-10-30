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
package org.sonar.iac.terraform.visitors;

import java.util.Set;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.NoSonarNextLineResolver;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.terraform.api.tree.HeredocLiteralTree;
import org.sonar.iac.terraform.plugin.TerraformExtension;

public class TerraformMetricsVisitor extends MetricsVisitor {

  private NoSonarNextLineResolver noSonarNextLineResolver = new NoSonarNextLineResolver();

  public TerraformMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetry sensorTelemetry) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetry, TerraformExtension.REPOSITORY_KEY);
  }

  @Override
  protected void languageSpecificMetrics() {
    super.languageSpecificMetrics();
    register(HeredocLiteralTree.class, (ctx, heredoc) -> noSonarNextLineResolver.addElement(heredoc.textRange()));
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    super.before(ctx, root);
    noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  /**
   * The content of a heredoc cannot hold a Terraform comment. Hence, a comment suppressing issues on the line where a heredoc
   * starts, or written alone on its line right above it, covers the whole heredoc.
   */
  @Override
  protected void after(InputFileContext ctx, Tree root) {
    noSonarNextLineResolver.addCodeLines(linesOfCode());
    noSonarLines().addAll(noSonarNextLineResolver.coveredElementLines(Set.copyOf(noSonarLines())));
    super.after(ctx, root);
  }
}
