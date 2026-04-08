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
package org.sonar.iac.jvmframeworkconfig.tree.api;

import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.yaml.tree.YamlTree;

/**
 * Represents a Spring configuration file.
 */
public interface File extends JvmFrameworkConfig {
  /**
   * @return a list of profiles defined in this file.
   */
  List<Profile> profiles();

  /**
   * @return the original yaml tree of this profile, or null if it was not parsed from a yaml file.
   */
  @CheckForNull
  YamlTree originalYamlTree();
}
