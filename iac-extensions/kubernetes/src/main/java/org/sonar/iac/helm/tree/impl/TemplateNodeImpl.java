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
package org.sonar.iac.helm.tree.impl;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.TemplateNodeOrBuilder;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.PipeNode;
import org.sonar.iac.helm.tree.api.TemplateNode;

import static org.sonar.iac.helm.tree.utils.GoTemplateAstConverter.textRangeFromPb;

public class TemplateNodeImpl extends AbstractNode implements TemplateNode {
  @Nullable
  private final String name;
  @Nullable
  private final PipeNode pipe;

  public TemplateNodeImpl(Supplier<TextRange> textRangeSupplier, @Nullable String name, @Nullable PipeNode pipe) {
    super(textRangeSupplier);
    this.name = name;
    this.pipe = pipe;
  }

  public static Node fromPb(TemplateNodeOrBuilder templateNodePb, String source) {
    return new TemplateNodeImpl(
      textRangeFromPb(templateNodePb, source),
      templateNodePb.getName(),
      (PipeNode) Optional.of(templateNodePb.getPipe())
        .filter(t -> templateNodePb.hasPipe())
        .map(it -> PipeNodeImpl.fromPb(it, source))
        .orElse(null));
  }

  @CheckForNull
  public String name() {
    return name;
  }

  @CheckForNull
  public PipeNode pipe() {
    return pipe;
  }

  @Override
  public List<Tree> children() {
    if (pipe != null) {
      return List.of(pipe);
    } else {
      return List.of();
    }
  }
}
