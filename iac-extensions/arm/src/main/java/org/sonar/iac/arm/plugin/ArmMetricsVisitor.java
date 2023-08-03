/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.plugin;

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;

import static org.sonar.iac.arm.plugin.ArmSensor.isBicepFile;

public class ArmMetricsVisitor extends YamlMetricsVisitor {
  protected ArmMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter) {
    super(fileLinesContextFactory, noSonarFilter);
  }

  @Override
  protected boolean acceptFileForLoc(InputFileContext inputFileContext) {
    return !isBicepFile(inputFileContext);
  }

  @Override
  protected void languageSpecificMetrics() {
    register(SyntaxToken.class, defaultMetricsVisitor());
  }
}
