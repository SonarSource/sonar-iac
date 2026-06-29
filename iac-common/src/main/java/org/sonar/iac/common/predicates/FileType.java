/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.predicates;

/**
 * Describes the type of a YAML (or YAML-like) file as determined by {@link YamlFileTypeResolver}.
 * Each file is resolved to exactly one type, which is then cached so that the various YAML based sensors don't have to
 * re-evaluate the (potentially expensive) file predicates.
 */
public enum FileType {
  KUBERNETES,
  HELM,
  KUSTOMIZE,
  JVM_CONFIG,
  CLOUDFORMATION,
  GITHUB_ACTIONS,
  ANSIBLE,
  AZURE_PIPELINES,
  AZURE_RESOURCE_MANAGER,
  // This value is used when the file does not match any of the known file predicates.
  UNDETERMINED
}
