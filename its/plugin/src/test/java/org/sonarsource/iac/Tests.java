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
package org.sonarsource.iac;

import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.io.File;

@Suite
@SelectClasses({
  CfnLintReportTest.class,
  HadolintReportTest.class,
  MeasuresTest.class,
  NoSonarTest.class,
  PropertiesTest.class,
  TFLintReportTest.class
})
public class Tests {

  static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  static final String SONAR_CONFIG_VERSION = "DEV";

  public static final FileLocation IAC_PLUGIN_LOCATION = FileLocation.byWildcardMavenFilename(new File("../../sonar-iac-plugin/target"), "sonar-iac-plugin-*.jar");

  static OrchestratorExtension ORCHESTRATOR = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(IAC_PLUGIN_LOCATION)
    .addPlugin(MavenLocation.of("org.sonarsource.config", "sonar-config-plugin", SONAR_CONFIG_VERSION))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/nosonar-terraform.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/aws-provider-terraform.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/no_rules-docker.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/no_rules-cloudformation.xml"))
    .build();
}
