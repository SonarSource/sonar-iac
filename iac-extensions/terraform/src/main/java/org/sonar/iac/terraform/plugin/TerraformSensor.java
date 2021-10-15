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

import com.sonar.sslr.api.RecognitionException;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.checks.TerraformCheckList;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.iac.terraform.visitors.TerraformHighlightingVisitor;
import org.sonar.iac.terraform.visitors.TerraformMetricsVisitor;

public class TerraformSensor extends IacSensor {

  private final Checks<IacCheck> checks;

  public TerraformSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter, TerraformLanguage language) {
    super(fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(TerraformExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable<?>) TerraformCheckList.checks());
  }

  @Override
  protected HclParser treeParser() {
    return new HclParser();
  }

  @Override
  protected String repositoryKey() {
    return TerraformExtension.REPOSITORY_KEY;
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isSonarLintContext(sensorContext)) {
      visitors.add(new TerraformMetricsVisitor(fileLinesContextFactory, noSonarFilter));
      visitors.add(new TerraformHighlightingVisitor());
    }
    visitors.add(new ChecksVisitor(checks, statistics));
    return visitors;
  }

  @Override
  protected String getActivationSettingKey() {
    return TerraformSettings.ACTIVATION_KEY;
  }

  @Override
  protected ParseException toParseException(String action, InputFile inputFile, Exception cause) {
    TextPointer position = null;
    if (cause instanceof RecognitionException) {
      position = inputFile.newPointer(((RecognitionException) cause).getLine(), 0);
    }
    return new ParseException("Cannot " + action + " '" + inputFile + "': " + cause.getMessage(), position);
  }
}
