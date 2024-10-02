/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.arm.checks.ArmCheckList;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.visitors.ArmHighlightingVisitor;
import org.sonar.iac.arm.visitors.ArmSymbolVisitor;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.FileIdentificationPredicate;
import org.sonar.iac.common.extension.analyzer.SingleFileAnalyzer;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlSensor;

public class ArmSensor extends YamlSensor {

  public ArmSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, ArmLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, ArmCheckList.checks());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(JSON_LANGUAGE_KEY, language.getKey())
      .processesFilesIndependently()
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected String repositoryKey() {
    return ArmExtension.REPOSITORY_KEY;
  }

  @Override
  protected String getActivationSettingKey() {
    return ArmSettings.ACTIVATION_KEY;
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FileSystem fileSystem = sensorContext.fileSystem();
    FilePredicates predicates = fileSystem.predicates();

    // Allow files which are main bicep file, or a main json file which contains the expected file identifier
    return predicates.and(predicates.hasType(InputFile.Type.MAIN),
      predicates.or(predicates.hasLanguage(ArmLanguage.KEY),
        predicates.and(predicates.hasLanguage(JSON_LANGUAGE_KEY),
          customFilePredicate(sensorContext))));
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext) {
    return new FileIdentificationPredicate(sensorContext.config().get(ArmSettings.FILE_IDENTIFIER_KEY).orElse(""), true);
  }

  @Override
  protected SingleFileAnalyzer createAnalyzer(SensorContext sensorContext, DurationStatistics statistics) {
    return new SingleFileAnalyzer(repositoryKey(), new ArmParser(), visitors(sensorContext, statistics), statistics);
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    visitors.add(new ArmSymbolVisitor());
    visitors.add(new ChecksVisitor(checks, statistics));
    if (isNotSonarLintContext(sensorContext)) {
      visitors.add(new ArmMetricsVisitor(fileLinesContextFactory, noSonarFilter));
      visitors.add(new ArmHighlightingVisitor());
    }
    return visitors;
  }

  public static boolean isBicepFile(InputFileContext inputFileContext) {
    return ArmLanguage.KEY.equals(inputFileContext.inputFile.language());
  }
}
