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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.FileIdentificationPredicate;
import org.sonar.iac.common.extension.TreeParser;
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
    return new FileIdentificationPredicate(sensorContext.config().get(ArmSettings.FILE_IDENTIFIER_KEY).orElse(""));
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new ArmParser();
  }
}
