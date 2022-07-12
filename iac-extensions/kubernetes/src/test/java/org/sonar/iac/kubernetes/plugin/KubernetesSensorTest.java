/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.iac.common.testing.AbstractYamlSensorTest;

import static org.assertj.core.api.Assertions.assertThat;

class KubernetesSensorTest extends AbstractYamlSensorTest {

  private static final String K8_IDENTIFIERS = "apiVersion: ~\nkind: ~\nmetadata: ~\nspec: ~\n";

  @Test
  void should_return_kubernetes_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Kubernetes Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml");
  }

  @Test
  void yaml_file_without_identifiers_should_not_be_parsed() {
    analyse(sensor("S2260"), inputFileWithoutIdentifiers("error.yaml", "a: b: c"));
    assertThat(context.allIssues()).isEmpty();
  }


  protected InputFile inputFileWithoutIdentifiers(String relativePath, String content) {
    return super.inputFile(relativePath, content);
  }

  @Override
  protected InputFile inputFile(String relativePath, String content) {
    return super.inputFile(relativePath, K8_IDENTIFIERS + content);
  }

  @Override
  protected String getActivationSettingKey() {
    return KubernetesSettings.ACTIVATION_KEY;
  }

  private KubernetesSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }


  @Override
  protected KubernetesSensor sensor(CheckFactory checkFactory) {
    return new KubernetesSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, language());
  }

  @Override
  protected String repositoryKey() {
    return KubernetesExtension.REPOSITORY_KEY;
  }

  @Override
  protected KubernetesLanguage language() {
    return new KubernetesLanguage();
  }

  @Override
  protected String fileLanguageKey() {
    return "yaml";
  }
}
