/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.tree.impl;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonar.iac.helm.tree.api.GoTemplateTree;
import org.sonar.iac.kubernetes.tree.api.KubernetesFileTree;

public class HelmFileTreeImpl extends FileTreeImpl implements KubernetesFileTree {
  @Nullable
  private final GoTemplateTree goTemplateAst;

  public HelmFileTreeImpl(List<YamlTree> documents, YamlTreeMetadata metadata, @Nullable GoTemplateTree goTemplateAst) {
    super(documents, metadata);
    this.goTemplateAst = goTemplateAst;
  }

  public static HelmFileTreeImpl fromFileTree(FileTree fileTree, @Nullable GoTemplateTree goTemplateAst) {
    return new HelmFileTreeImpl(fileTree.documents(), fileTree.metadata(), goTemplateAst);
  }

  @Override
  @CheckForNull
  public GoTemplateTree getGoTemplateAst() {
    return goTemplateAst;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = super.children();
    if (goTemplateAst != null) {
      children.add(goTemplateAst);
    }
    return children;
  }
}
