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
package org.sonar.iac.helm.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.helm.checks.HelmCheckList;
import org.sonar.iac.helm.parser.HelmParser;

public class HelmSensor extends YamlSensor {

  public HelmSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, HelmLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, HelmCheckList.checks());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(YAML_LANGUAGE_KEY)
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected String repositoryKey() {
    return HelmExtension.REPOSITORY_KEY;
  }

  @Override
  protected String getActivationSettingKey() {
    return HelmSettings.ACTIVATION_KEY;
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.and(
      predicates.hasLanguage(YAML_LANGUAGE_KEY),
      predicates.hasType(InputFile.Type.MAIN),
      customFilePredicate(sensorContext));
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext) {
    return new HelmFilePredicate();
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new HelmParser();
  }

  static class HelmFilePredicate implements FilePredicate {

    private static final Logger LOG = LoggerFactory.getLogger(HelmFilePredicate.class);

    @Override
    public boolean apply(InputFile inputFile) {
      return isHelmFile(inputFile);
    }

    private static boolean isHelmFile(InputFile inputFile) {
      // TODO: Actual FilePredicate
      return false;
    }
  }
}
