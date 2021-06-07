/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.IacSensor;
import org.sonar.iac.common.InputFileContext;
import org.sonar.iac.common.TreeParser;
import org.sonar.iac.common.checks.api.IacCheck;
import org.sonar.iac.common.visitors.TreeVisitor;
import org.sonar.iac.terraform.checks.TerraformCheckList;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.visitors.MetricsVisitor;
import org.sonar.iac.terraform.visitors.SyntaxHighlightingVisitor;

public class TerraformSensor extends IacSensor {

  private final Checks<IacCheck> checks;

  public TerraformSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter, TerraformLanguage language) {
    super(fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(TerraformExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable<?>) TerraformCheckList.checks());
  }

  @Override
  protected TreeParser treeParser() {
    return new HclParser();
  }

  @Override
  protected String repositoryKey() {
    return TerraformExtension.REPOSITORY_KEY;
  }

  public List<TreeVisitor<InputFileContext>> languageSpecificVisitors(SensorContext sensorContext) {
    List<TreeVisitor<InputFileContext>> languageSpecificTreeVisitors = new ArrayList<>();
    // non sonar lint context visitors
    if (sensorContext.runtime().getProduct() != SonarProduct.SONARLINT) {
      languageSpecificTreeVisitors.addAll(Arrays.asList(
        new MetricsVisitor(fileLinesContextFactory, noSonarFilter),
        new SyntaxHighlightingVisitor()
      ));
    }
    return languageSpecificTreeVisitors;
  }

  @Override
  protected Checks<IacCheck> checks() {
    return checks;
  }
}
