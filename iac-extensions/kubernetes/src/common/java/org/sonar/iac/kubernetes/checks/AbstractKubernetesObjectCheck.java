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
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

public abstract class AbstractKubernetesObjectCheck implements IacCheck {

  private final Map<String, List<Consumer<BlockObject>>> objectConsumersByKind = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (ctx, fileTree) -> fileTree.documents().stream()
      .filter(MappingTree.class::isInstance)
      .forEach(documentTree -> visitDocument((MappingTree) documentTree, (KubernetesCheckContext) ctx)));
    registerObjectCheck();
  }

  void visitDocument(MappingTree documentTree, KubernetesCheckContext ctx) {
    initializeCheck(ctx);
    visitRoot(documentTree, ctx);
    visitDocumentOnEnd(documentTree, ctx);
  }

  private void visitRoot(MappingTree specTree, CheckContext ctx) {
    PropertyUtils.get(specTree, "kind")
      .flatMap(kind -> TextUtils.getValue(kind.value()))
      .filter(objectConsumersByKind::containsKey)
      .ifPresent((String kind) -> {
        if (shouldVisitWholeDocument()) {
          visitMappingTreeForKind(specTree, ctx, kind);
        } else {
          visitSpecTreeForKind(specTree, ctx, kind);
        }
      });
  }

  public KubernetesCheckForOtherYaml prepareForEmbeddedYaml() {
    registerObjectCheck();
    return new KubernetesCheckForOtherYaml();
  }

  public class KubernetesCheckForOtherYaml {
    public void visit(MappingTree specTree, CheckContext ctx) {
      visitRoot(specTree, ctx);
    }
  }

  void initializeCheck(KubernetesCheckContext ctx) {
    // default implementation does nothing; the rule can interact with CheckContext here.
  }

  protected boolean shouldVisitWholeDocument() {
    // the default is "false" as we normally visit only the "spec" tree of the file.
    // Overriding this to "true" will enable visitation of the whole document.
    return false;
  }

  void visitDocumentOnEnd(MappingTree documentTree, CheckContext ctx) {
    // default implementation does nothing; the rule can interact when leaving document
  }

  private void visitSpecTreeForKind(MappingTree documentTree, CheckContext ctx, String kind) {
    PropertyUtils.get(documentTree, "spec")
      .filter(TupleTree.class::isInstance)
      .map(TupleTree.class::cast)
      .filter(spec -> spec.value() instanceof MappingTree)
      .map(spec -> (MappingTree) spec.value())
      .ifPresent(specValue -> visitMappingTreeForKind(specValue, ctx, kind));
  }

  private void visitMappingTreeForKind(MappingTree mappingTree, CheckContext ctx, String kind) {
    var blockObject = BlockObject.fromPresent(ctx, mappingTree, kind);
    objectConsumersByKind.get(kind).forEach(consumer -> consumer.accept(blockObject));
  }

  protected abstract void registerObjectCheck();

  protected void register(String kind, Consumer<BlockObject> consumer) {
    objectConsumersByKind.computeIfAbsent(kind, s -> new ArrayList<>()).add(consumer);
  }

  protected void register(Iterable<String> kinds, Consumer<BlockObject> consumer) {
    kinds.forEach(kind -> register(kind, consumer));
  }

}
