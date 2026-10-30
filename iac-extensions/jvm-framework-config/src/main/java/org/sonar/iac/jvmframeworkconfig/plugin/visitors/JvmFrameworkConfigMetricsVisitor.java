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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import java.util.Set;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.NoSonarNextLineResolver;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;
import org.sonar.iac.jvmframeworkconfig.tree.api.SyntaxToken;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigSensor.isPropertiesFile;

public class JvmFrameworkConfigMetricsVisitor extends YamlMetricsVisitor {

  private NoSonarNextLineResolver noSonarNextLineResolver = new NoSonarNextLineResolver();

  public JvmFrameworkConfigMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter,
    SensorTelemetry sensorTelemetry) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetry, JvmFrameworkConfigExtension.JAVA_REPOSITORY_KEY);
  }

  @Override
  protected boolean acceptFileForLoc(InputFileContext inputFileContext) {
    // YAML files should be processed by YamlMetricsVisitor; it will process them line-by-line.
    // Properties files will be handled by this visitor using languageSpecificMetrics() call.
    return !isPropertiesFile(inputFileContext);
  }

  @Override
  protected void languageSpecificMetrics() {
    register(SyntaxToken.class, defaultMetricsVisitor());
    register(Profile.class, (ctx, tree) -> addCommentLines(tree.comments()));
    register(Tuple.class, (ctx, tuple) -> noSonarNextLineResolver.addElement(tuple.textRange()));
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    super.before(ctx, root);
    noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    if (isPropertiesFile(ctx)) {
      // A comment of a properties file is always alone on its line, so a comment suppressing issues also covers the next property
      noSonarNextLineResolver.addCodeLines(linesOfCode());
      noSonarLines().addAll(noSonarNextLineResolver.coveredLines(Set.copyOf(noSonarLines())));
    }
    super.after(ctx, root);
  }

  @Override
  protected boolean hasContent(String commentLine) {
    // additionally, treat profile separators in properties files as valid comment lines
    return super.hasContent(commentLine) || "---".equals(commentLine);
  }
}
