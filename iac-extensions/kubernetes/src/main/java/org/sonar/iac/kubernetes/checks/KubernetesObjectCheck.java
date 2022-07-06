/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.kubernetes.symbols.ObjectSymbol;

public abstract class KubernetesObjectCheck implements IacCheck {

  private final Map<String, List<Consumer<ObjectSymbol>>> objectConsumer = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, fileTree) -> {
      if (fileTree.root() instanceof MappingTree) {
        MappingTree fileMap = (MappingTree) fileTree.root();
        PropertyUtils.get(fileMap, "kind")
          .flatMap(kind -> TextUtils.getValue(kind.value()))
          .filter(objectConsumer::containsKey)
          .ifPresent(kind -> PropertyUtils.get(fileMap, "spec")
            .filter(TupleTree.class::isInstance)
            .map(TupleTree.class::cast)
            .ifPresent(spec -> {
              ObjectSymbol obj = ObjectSymbol.fromPresent(ctx, spec, kind);
              objectConsumer.get(kind).forEach(consumer -> consumer.accept(obj));
            }));
      }
    });
    registerObjectCheck();
  }

  abstract void registerObjectCheck();

  protected void register(String kind, Consumer<ObjectSymbol> consumer) {
    objectConsumer.computeIfAbsent(kind, s -> new ArrayList<>()).add(consumer);
  }

  protected void register(Iterable<String> kinds, Consumer<ObjectSymbol> consumer) {
    kinds.forEach(kind -> register(kind, consumer));
  }

}
