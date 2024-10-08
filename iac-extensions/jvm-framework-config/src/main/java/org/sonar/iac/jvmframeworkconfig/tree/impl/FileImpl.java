/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
