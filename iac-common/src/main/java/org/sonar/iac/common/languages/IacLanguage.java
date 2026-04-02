/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.languages;

import javax.annotation.Nullable;

public enum IacLanguage {
  ANSIBLE("ansible", "Ansible"),
  ARM("azureresourcemanager", "Azure Resource Manager"),
  AZURE_PIPELINES("azurepipelines", "Azure Pipelines"),
  CLOUDFORMATION("cloudformation", "CloudFormation"),
  DOCKER("docker", "Docker"),
  GITHUB_ACTIONS("githubactions", "GitHub Actions"),
  JAVA("java", "Java"),
  JSON("json", "JSON"),
  KUBERNETES("kubernetes", "Kubernetes"),
  SHELL("shell", "Shell"),
  TERRAFORM("terraform", "Terraform"),
  YAML("yaml", "YAML"),
  UNKNOWN("", "");

  private final String key;
  private final String name;

  IacLanguage(String key, String name) {
    this.key = key;
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  /**
   * Return enum that represent one of the Iac Language.
   * It accepts Language key or name, see {@link org.sonar.api.resources.AbstractLanguage} and the classes that extends it.
   * For {@code null} as parameter it returns {@link IacLanguage#UNKNOWN}
   * @param languageKeyOrName key or name of the language
   * @return one of the enum value or {@link IacLanguage#UNKNOWN}
   */
  public static IacLanguage createFromLanguage(@Nullable String languageKeyOrName) {
    if (languageKeyOrName == null) {
      return UNKNOWN;
    }
    for (IacLanguage language : values()) {
      if (language != UNKNOWN && (language.key.equals(languageKeyOrName) || language.name.equals(languageKeyOrName))) {
        return language;
      }
    }
    return UNKNOWN;
  }
}
