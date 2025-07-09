/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.iac;

import org.sonar.api.Plugin;
import org.sonar.iac.arm.plugin.ArmExtension;
import org.sonar.iac.cloudformation.plugin.CloudformationExtension;
import org.sonar.iac.common.json.JsonEmptyBuiltInProfileDefinition;
import org.sonar.iac.common.json.JsonFileFilter;
import org.sonar.iac.common.json.JsonLanguage;
import org.sonar.iac.common.warnings.DefaultAnalysisWarningsWrapper;
import org.sonar.iac.common.yaml.YamlEmptyBuiltInProfileDefinition;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.docker.plugin.DockerExtension;
import org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension;
import org.sonar.iac.kubernetes.plugin.KubernetesExtension;
import org.sonar.iac.terraform.plugin.TerraformExtension;

import static org.sonar.api.SonarEdition.COMMUNITY;
import static org.sonar.api.SonarProduct.SONARLINT;
import static org.sonar.api.SonarProduct.SONARQUBE;

public class IacPlugin implements Plugin {

  @Override
  public void define(Context context) {
    TerraformExtension.define(context);
    CloudformationExtension.define(context);
    KubernetesExtension.define(context);
    DockerExtension.define(context);
    ArmExtension.define(context);
    JvmFrameworkConfigExtension.define(context);

    context.addExtension(YamlLanguage.class);
    context.addExtension(YamlLanguage.getProperty());

    context.addExtension(JsonLanguage.class);
    context.addExtension(JsonFileFilter.class);
    context.addExtension(JsonLanguage.getProperty());

    if ((context.getRuntime().getProduct() == SONARQUBE && context.getRuntime().getEdition() == COMMUNITY) || context.getRuntime().getProduct() == SONARLINT) {
      // For SonarQube Community Edition, we add built-in quality profiles for YAML and JSON languages.
      // In other editions, the YAML and JSON profiles are registered by the sonar-iac-enterprise plugin (YAML Extension).
      context.addExtension(YamlEmptyBuiltInProfileDefinition.class);
      context.addExtension(JsonEmptyBuiltInProfileDefinition.class);
    }

    context.addExtension(DefaultAnalysisWarningsWrapper.class);
  }
}
