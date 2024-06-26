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
package org.sonar.iac.kubernetes.visitors;

import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.LimitRangeItem;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.model.ServiceAccount;

public final class ProjectResourceFactory {
  private ProjectResourceFactory() {
    // utility class
  }

  @CheckForNull
  public static ProjectResource createResource(MappingTree tree) {
    var kind = PropertyUtils.value(tree, "kind")
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value)
      .orElse("");
    return switch (kind) {
      case "ServiceAccount" -> createServiceAccount(tree);
      case "LimitRange" -> createLimitRange(tree);
      default -> null;
    };
  }

  @CheckForNull
  private static ProjectResource createServiceAccount(MappingTree tree) {
    var name = PropertyUtils.value(tree, "metadata", MappingTree.class)
      .flatMap(metadata -> PropertyUtils.value(metadata, "name"))
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value);
    if (name.isEmpty()) {
      return null;
    }

    var automountServiceAccountTokenTree = PropertyUtils.value(tree, "automountServiceAccountToken")
      .map(ScalarTree.class::cast);
    var automountServiceAccountToken = automountServiceAccountTokenTree
      .map(TextUtils::trileanFromTextTree)
      .orElse(Trilean.UNKNOWN);
    var valueLocation = automountServiceAccountTokenTree.map(HasTextRange::textRange).orElse(null);

    return new ServiceAccount(name.get(), automountServiceAccountToken, valueLocation);
  }

  @CheckForNull
  private static ProjectResource createLimitRange(MappingTree tree) {
    var limits = PropertyUtils.value(tree, "spec")
      .flatMap(it -> PropertyUtils.value(it, "limits"))
      .stream()
      .flatMap(it -> ((SequenceTree) it).elements().stream())
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(ProjectResourceFactory::toLimitRangeItem)
      .toList();

    return new LimitRange(limits);
  }

  private static LimitRangeItem toLimitRangeItem(MappingTree tree) {
    var map = tree.elements().stream()
      .collect(Collectors.toMap(tuple -> ((ScalarTree) tuple.key()).value(), TupleTree::value));

    return new LimitRangeItem(
      ((ScalarTree) map.get("type")).value(),
      mappingTreeToMap("default", map),
      mappingTreeToMap("defaultRequest", map),
      mappingTreeToMap("maxLimitRequestRatio", map),
      mappingTreeToMap("max", map),
      mappingTreeToMap("min", map));
  }

  private static Map<String, String> mappingTreeToMap(String key, Map<String, YamlTree> values) {
    if (!values.containsKey(key)) {
      return Map.of();
    }

    var tree = values.get(key);
    if (tree instanceof MappingTree mappingTree) {
      return mappingTree.elements().stream()
        .collect(Collectors.toMap(tuple -> ((ScalarTree) tuple.key()).value(), tuple -> ((ScalarTree) tuple.value()).value()));
    } else {
      return Map.of();
    }
  }
}
