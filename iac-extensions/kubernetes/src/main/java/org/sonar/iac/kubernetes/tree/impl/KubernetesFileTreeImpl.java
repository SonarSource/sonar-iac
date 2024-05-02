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
package org.sonar.iac.kubernetes.tree.impl;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.kubernetes.tree.api.KubernetesFileTree;

public class KubernetesFileTreeImpl extends FileTreeImpl implements KubernetesFileTree {
  @Nullable
  private final GoTemplateTree goTemplateAst;

  public KubernetesFileTreeImpl(List<YamlTree> documents, YamlTreeMetadata metadata, @Nullable GoTemplateTree goTemplateAst) {
    super(documents, metadata);
    this.goTemplateAst = goTemplateAst;
  }

  public static KubernetesFileTreeImpl fromFileTree(FileTree fileTree, @Nullable GoTemplateTree goTemplateAst) {
    return new KubernetesFileTreeImpl(fileTree.documents(), fileTree.metadata(), goTemplateAst);
  }

  @Override
  @CheckForNull
  public GoTemplateTree getGoTemplateAst() {
    return goTemplateAst;
  }
}
