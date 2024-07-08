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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.kubernetes.model.ConfigMap;
import org.sonar.iac.kubernetes.model.MapResource;
import org.sonar.iac.kubernetes.model.Secret;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

@Rule(key = "S6907")
public class DuplicatedEnvironmentVariablesCheck extends AbstractResourceManagementCheck<MapResource> {

  private static final List<String> KIND_WITH_TEMPLATE = List.of(
    "DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private final List<Container> containers = new ArrayList<>();

  @Override
  void registerObjectCheck() {
    register("Pod",
      pod -> pod
        .block("spec")
        .blocks("containers")
        .forEach(containerBlock -> this.checkContainer(pod, containerBlock)));

    register(KIND_WITH_TEMPLATE,
      template -> template
        .block("spec")
        .block("template")
        .block("spec")
        .blocks("containers")
        .forEach(containerBlock -> this.checkContainer(template, containerBlock)));
  }

  private void checkContainer(BlockObject root, BlockObject containerBlock) {
    var container = new Container(new HashMap<>());
    containers.add(container);
    containerBlock.blocks("env").forEach(env -> checkEnvironmentVariable(env, container));
    containerBlock.blocks("envFrom").forEach(envFrom -> checkEnvironmentVariableFrom(root, envFrom, container));
  }

  private static void checkEnvironmentVariable(BlockObject env, Container container) {
    var attribute = env.attribute("name");
    if (attribute.tree != null) {
      var tree = attribute.tree.value();
      if (tree instanceof ScalarTree scalarTree) {
        var name = scalarTree.value();
        container.variables.computeIfAbsent(name, key -> new ArrayList<>()).add(new EnvVariable(scalarTree));
      }
    }
  }

  private void checkEnvironmentVariableFrom(BlockObject root, BlockObject envFrom, Container container) {
    checkMapResource(root, envFrom.block("configMapRef"), container, ConfigMap.class, ConfigMapVariable::new);
    checkMapResource(root, envFrom.block("secretRef"), container, Secret.class, SecretVariable::new);
  }

  /**
   * Fill the provided {@link Container} with {@link AbstractVariable} collected from the related global resources.
   * @param root The document object, required to retrieve global resources.
   * @param mapRef The current block being processed ({@code configMapRef} or {@code secretRef})
   * @param container The container in which we fill the {@link AbstractVariable}
   * @param clazz The class of global resource we are looking for, either {@link ConfigMap} or {@link Secret}
   * @param creator A function used to wrap the resulting {@link YamlTree} into the corresponding {@link AbstractVariable} class instance.
   */
  private void checkMapResource(BlockObject root, BlockObject mapRef, Container container, Class<? extends MapResource> clazz,
    Function<YamlTree, ? extends AbstractVariable> creator) {
    retrieveMapRefTree(mapRef).ifPresent((ScalarTree mapRefNameValueTree) -> {
      var mapName = mapRefNameValueTree.value();
      getGlobalResources(root).stream()
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .filter(mapResource -> mapName.equals(mapResource.name()))
        .flatMap(mapResource -> mapResource.values().entrySet().stream())
        .forEach((Map.Entry<String, TupleTree> entryConfigMap) -> {
          var name = entryConfigMap.getKey();
          container.variables.computeIfAbsent(name, key -> new ArrayList<>()).add(creator.apply(mapRefNameValueTree));
        });
    });
  }

  private static Optional<ScalarTree> retrieveMapRefTree(BlockObject mapRef) {
    var attribute = mapRef.attribute("name");
    if (attribute.tree != null) {
      var mapRefNameValueTree = attribute.tree.value();
      if (mapRefNameValueTree instanceof ScalarTree scalarTree) {
        return Optional.of(scalarTree);
      }
    }
    return Optional.empty();
  }

  @Override
  void visitDocumentOnEnd(MappingTree documentTree, CheckContext ctx) {
    containers.forEach(container -> container.variables.entrySet().stream()
      .filter(entry -> entry.getValue().size() > 1)
      .forEach(entry -> reportIssue(ctx, entry.getKey(), entry.getValue())));
    containers.clear();
  }

  private static void reportIssue(CheckContext ctx, String variableName, List<AbstractVariable> envVariableReferences) {
    // Sort collected variables by line number
    Collections.sort(envVariableReferences);
    var secondaryLocations = new ArrayList<>(envVariableReferences.stream()
      .limit(envVariableReferences.size() - 1L)
      .map(envVariableReference -> computeSecondaryLocationFromEnvVariableReference(variableName, envVariableReference))
      .toList());
    var lastEnvVariableReference = envVariableReferences.get(envVariableReferences.size() - 1);
    String message = lastEnvVariableReference.primaryMessage(variableName);
    ctx.reportIssue(lastEnvVariableReference.tree, message, secondaryLocations);
  }

  private static SecondaryLocation computeSecondaryLocationFromEnvVariableReference(String variableName, AbstractVariable envVariableReference) {
    String message = envVariableReference.secondaryMessage(variableName);
    return new SecondaryLocation(envVariableReference.tree, message);
  }

  @Override
  void initializeCheck(KubernetesCheckContext ctx) {
    ctx.setShouldReportSecondaryInValues(true);
  }

  @Override
  Class<MapResource> getGlobalResourceType() {
    return MapResource.class;
  }

  // Container contains a map of environment variables including those from ConfigMap and Secret
  record Container(Map<String, List<AbstractVariable>> variables) {
  }

  abstract static class AbstractVariable implements Comparable<AbstractVariable> {
    private final YamlTree tree;

    protected AbstractVariable(YamlTree tree) {
      this.tree = tree;
    }

    @Override
    public int compareTo(AbstractVariable o) {
      return tree.textRange().start().line() - o.tree.textRange().start().line();
    }

    abstract String primaryMessage(String variableName);

    abstract String secondaryMessage(String variableName);
  }

  static class EnvVariable extends AbstractVariable {
    public EnvVariable(YamlTree tree) {
      super(tree);
    }

    @Override
    String primaryMessage(String variableName) {
      return "Resolve the duplication of this environment variable.";
    }

    @Override
    String secondaryMessage(String variableName) {
      return "Duplicate environment variable without any effect.";
    }
  }

  static class ConfigMapVariable extends AbstractVariable {
    public ConfigMapVariable(YamlTree tree) {
      super(tree);
    }

    @Override
    String primaryMessage(String variableName) {
      return "Resolve the duplication of the environment variable '%s' in this ConfigMap.".formatted(variableName);
    }

    @Override
    String secondaryMessage(String variableName) {
      return "ConfigMap that contain the duplicate environment variable '%s' without any effect.".formatted(variableName);
    }
  }

  static class SecretVariable extends AbstractVariable {
    public SecretVariable(YamlTree tree) {
      super(tree);
    }

    @Override
    String primaryMessage(String variableName) {
      return "Resolve the duplication of the environment variable '%s' in this Secret.".formatted(variableName);
    }

    @Override
    String secondaryMessage(String variableName) {
      return "Secret that contain the duplicate environment variable '%s' without any effect.".formatted(variableName);
    }
  }
}
