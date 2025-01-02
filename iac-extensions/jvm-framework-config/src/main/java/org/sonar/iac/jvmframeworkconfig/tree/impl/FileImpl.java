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
package org.sonar.iac.jvmframeworkconfig.tree.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.jvmframeworkconfig.tree.api.File;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;

public class FileImpl extends AbstractJvmFrameworkConfigImpl implements File {
  private final List<Profile> profiles;
  private final YamlTree originalTree;

  public FileImpl(List<Profile> profiles, @Nullable YamlTree originalTree) {
    this.profiles = profiles;
    this.originalTree = originalTree;
  }

  @Override
  public List<Profile> profiles() {
    return profiles;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(profiles);
  }

  @CheckForNull
  @Override
  public YamlTree originalYamlTree() {
    return originalTree;
  }
}
