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
import java.io.File;

public class TestsSetup {

  static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";

  public static final FileLocation IAC_PLUGIN_LOCATION = FileLocation.byWildcardMavenFilename(new File("../../sonar-iac-plugin/target"), "sonar-iac-plugin-*.jar");

  static OrchestratorExtension ORCHESTRATOR = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(IAC_PLUGIN_LOCATION)
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/nosonar-terraform.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/aws-provider-terraform.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/no_rules-docker.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/no_rules-json.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/no_rules-yaml.xml"))
    .restoreProfileAtStartup(FileLocation.of("src/test/resources/no_rules-cloudformation.xml"))
    .build();
}
