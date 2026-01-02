/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;
import org.sonar.iac.jvmframeworkconfig.tree.api.SyntaxToken;

import static org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigSensor.isPropertiesFile;

public class JvmFrameworkConfigMetricsVisitor extends YamlMetricsVisitor {
  public JvmFrameworkConfigMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetry sensorTelemetry) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetry);
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
  }

  @Override
  protected boolean hasContent(String commentLine) {
    // additionally, treat profile separators in properties files as valid comment lines
    return super.hasContent(commentLine) || "---".equals(commentLine);
  }
}
