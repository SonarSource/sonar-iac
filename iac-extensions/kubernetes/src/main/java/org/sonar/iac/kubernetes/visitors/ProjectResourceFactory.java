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
package org.sonar.iac.kubernetes.visitors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.model.ClusterRoleBinding;
import org.sonar.iac.kubernetes.model.ConfigMap;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.LimitRangeItem;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.model.RoleBinding;
import org.sonar.iac.kubernetes.model.Secret;
import org.sonar.iac.kubernetes.model.ServiceAccount;
import org.sonar.iac.kubernetes.model.Subject;

public final class ProjectResourceFactory {
  private ProjectResourceFactory() {
    // utility class
  }

  @CheckForNull
  public static ProjectResource createResource(String path, MappingTree tree) {
    var kind = PropertyUtils.value(tree, "kind")
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value)
      .orElse("");
    return switch (kind) {
      case "ServiceAccount" -> createServiceAccount(path, tree);
      case "LimitRange" -> createLimitRange(tree);
      case "ConfigMap" -> createConfigMap(path, tree);
      case "Secret" -> createSecret(path, tree);
      case "RoleBinding" -> createRoleBinding(tree);
      case "ClusterRoleBinding" -> createClusterRoleBinding(tree);
      default -> null;
    };
  }

  private static ProjectResource createServiceAccount(String path, MappingTree tree) {
    var name = retrieveNameFromMetadata(tree);
    if (name == null) {
      return null;
    }
    var automountServiceAccountTokenTree = PropertyUtils.value(tree, "automountServiceAccountToken")
      .map(ScalarTree.class::cast);
    var automountServiceAccountToken = automountServiceAccountTokenTree
      .map(TextUtils::trileanFromTextTree)
      .orElse(Trilean.UNKNOWN);
    var valueLocation = automountServiceAccountTokenTree.map(HasTextRange::textRange).orElse(null);

    return new ServiceAccount(path, name, automountServiceAccountToken, valueLocation);
  }

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

  private static ProjectResource createConfigMap(String filePath, MappingTree tree) {
    var map = computeDataMap(tree);
    var name = retrieveNameFromMetadata(tree);
    return new ConfigMap(filePath, name, map);
  }

  private static ProjectResource createSecret(String filePath, MappingTree tree) {
    var map = computeDataMap(tree);
    var name = retrieveNameFromMetadata(tree);
    return new Secret(filePath, name, map);
  }

  private static ProjectResource createRoleBinding(MappingTree tree) {
    return new RoleBinding(computeSubjectList(tree));
  }

  private static ProjectResource createClusterRoleBinding(MappingTree tree) {
    return new ClusterRoleBinding(computeSubjectList(tree));
  }

  private static List<Subject> computeSubjectList(MappingTree tree) {
    return PropertyUtils.value(tree, "subjects")
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .stream()
      .flatMap(sequence -> sequence.elements().stream())
      .map(ProjectResourceFactory::computeSubject)
      .filter(Objects::nonNull)
      .toList();
  }

  @CheckForNull
  private static Subject computeSubject(YamlTree tree) {
    if (tree instanceof MappingTree mappingTree) {
      String kind = retrieveField(mappingTree, "kind");
      String name = retrieveField(mappingTree, "name");
      String namespace = retrieveField(mappingTree, "namespace");
      return new Subject(kind, name, namespace);
    }
    return null;
  }

  private static Map<String, TupleTree> computeDataMap(MappingTree tree) {
    return PropertyUtils.value(tree, "data")
      .stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .flatMap(mappingTree -> mappingTree.elements().stream())
      .filter(tupleTree -> tupleTree.key() instanceof ScalarTree)
      .collect(Collectors.toMap(tupleTree -> ((ScalarTree) tupleTree.key()).value(), tupleTree -> tupleTree));
  }

  @CheckForNull
  private static String retrieveNameFromMetadata(MappingTree tree) {
    return PropertyUtils.value(tree, "metadata")
      .flatMap(it -> PropertyUtils.value(it, "name"))
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(TextTree::value)
      .orElse(null);
  }

  @CheckForNull
  private static String retrieveField(MappingTree tree, String fieldName) {
    return PropertyUtils.value(tree, fieldName)
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(TextTree::value)
      .orElse(null);
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
