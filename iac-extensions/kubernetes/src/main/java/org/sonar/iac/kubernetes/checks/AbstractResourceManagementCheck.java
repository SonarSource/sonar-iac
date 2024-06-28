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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.LimitRangeItem;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

import static org.sonar.iac.common.yaml.TreePredicates.isSetString;

public abstract class AbstractResourceManagementCheck extends AbstractKubernetesObjectCheck {
  protected static final String KIND_POD = "Pod";
  protected static final List<String> KIND_WITH_TEMPLATE = List.of(
    "DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  protected static final Set<String> LIMIT_RANGE_LIMIT_TYPES = Set.of("Pod", "Container");

  @Override
  boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  void registerObjectCheck() {
    register(KIND_POD, document -> checkDocument(document, false));
    register(KIND_WITH_TEMPLATE, document -> checkDocument(document, true));
  }

  private void checkDocument(BlockObject document, boolean isKindWithTemplate) {
    var namespace = getNamespace(document);
    var globalResources = getGlobalResources(document, namespace);

    Stream<BlockObject> containers;
    if (isKindWithTemplate) {
      containers = document.block("spec").block("template").block("spec").blocks("containers");
    } else {
      containers = document.block("spec").blocks("containers");
    }
    containers.filter(container -> !hasLimitDefinedGlobally(globalResources))
      .forEach(this::reportMissingLimit);
  }

  protected void reportMissingLimit(BlockObject container) {
    container.block("resources").block(getResourceManagementName())
      .attribute(getResourceName())
      .reportIfAbsent(getFirstChildElement(container), getMessage())
      .reportIfValue(TreePredicates.isSet().negate(), getMessage());
  }

  @Nullable
  static HasTextRange getFirstChildElement(BlockObject blockObject) {
    if (blockObject.tree != null) {
      return blockObject.tree.elements().get(0).key();
    }
    return null;
  }

  private static Collection<LimitRange> getGlobalResources(BlockObject document, String namespace) {
    var projectContext = ((KubernetesCheckContext) document.ctx).projectContext();
    var inputFileContext = ((KubernetesCheckContext) document.ctx).inputFileContext();
    return projectContext.getProjectResources(namespace, inputFileContext, LimitRange.class);
  }

  protected boolean hasLimitDefinedGlobally(Collection<LimitRange> globalResources) {
    return globalResources.stream()
      .flatMap(limitRange -> limitRange.limits().stream())
      .anyMatch(this::hasDefinedLimitForResource);
  }

  protected boolean hasDefinedLimitForResource(LimitRangeItem limitRangeItem) {
    var limit = retrieveLimitRangeItemMap(limitRangeItem).get(getResourceName());
    return getLimitRangeLimitTypes().contains(limitRangeItem.type()) && isSet(limit);
  }

  protected Set<String> getLimitRangeLimitTypes() {
    return LIMIT_RANGE_LIMIT_TYPES;
  }

  abstract Map<String, String> retrieveLimitRangeItemMap(LimitRangeItem limitRangeItem);

  abstract String getResourceManagementName();

  abstract String getResourceName();

  abstract String getMessage();

  /**
   * Retrieve the namespace of the document from the `metadata.namespace` attribute.<br/>
   * If it is not set, the objects are installed in the namespace `default`. However, a namespace can be set during deployment using the
   * `--namespace [custom-name]` flag. Because of that, an empty string is returned if the namespace is not set.
   */
  private static String getNamespace(BlockObject document) {
    return Optional.ofNullable(document.block("metadata").attribute("namespace").tree)
      .map(TupleTree::value)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value)
      .orElse("");
  }

  static boolean isSet(@Nullable String value) {
    return value != null && isSetString().test(value);
  }
}
