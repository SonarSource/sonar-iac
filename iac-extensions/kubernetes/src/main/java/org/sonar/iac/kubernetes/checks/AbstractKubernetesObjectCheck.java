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
package org.sonar.iac.kubernetes.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.object.BlockObject;

public abstract class AbstractKubernetesObjectCheck implements IacCheck {

  private final Map<String, List<Consumer<BlockObject>>> objectConsumersByKind = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, fileTree) -> fileTree.documents().stream()
      .filter(MappingTree.class::isInstance)
      .forEach(documentTree -> visitDocument((MappingTree) documentTree, ctx)));
    registerObjectCheck();
  }

  private void visitDocument(MappingTree documentTree, CheckContext ctx) {
    PropertyUtils.get(documentTree, "kind")
      .flatMap(kind -> TextUtils.getValue(kind.value()))
      .filter(objectConsumersByKind::containsKey)
      .ifPresent(kind -> PropertyUtils.get(documentTree, "spec")
        .filter(TupleTree.class::isInstance)
        .map(TupleTree.class::cast)
        .filter(t -> t.value() instanceof MappingTree)
        .ifPresent(spec -> {
          BlockObject obj = BlockObject.fromPresent(ctx, spec.value(), kind);
          objectConsumersByKind.get(kind).forEach(consumer -> consumer.accept(obj));
        }));
  }

  abstract void registerObjectCheck();

  protected void register(String kind, Consumer<BlockObject> consumer) {
    objectConsumersByKind.computeIfAbsent(kind, s -> new ArrayList<>()).add(consumer);
  }

  protected void register(Iterable<String> kinds, Consumer<BlockObject> consumer) {
    kinds.forEach(kind -> register(kind, consumer));
  }

}
