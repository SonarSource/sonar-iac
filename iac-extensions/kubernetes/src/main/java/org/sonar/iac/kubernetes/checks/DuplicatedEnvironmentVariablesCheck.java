package org.sonar.iac.kubernetes.checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6907")
public class DuplicatedEnvironmentVariablesCheck extends AbstractKubernetesObjectCheck {
  private static final String MESSAGE = "Resolve the duplication of this environment variable.";
  private static final String MESSAGE_SECONDARY_LOCATION = "Resolve the duplication of this environment variable.";
  private static final List<String> KIND_WITH_TEMPLATE = List.of(
    "DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  private final List<Container> containers = new ArrayList<>();

  @Override
  void registerObjectCheck() {
    register("Pod", pod -> pod.blocks("containers").forEach(this::checkContainer));

    register(KIND_WITH_TEMPLATE,
      template -> template
        .block("template")
        .block("spec")
        .blocks("containers")
        .forEach(this::checkContainer));
  }

  private void checkContainer(BlockObject containerBlock) {
    var container = new Container(new HashMap<>());
    containers.add(container);
    containerBlock.blocks("env").forEach(env -> {
      var attribute = env.attribute("name");
      if (attribute.tree != null) {
        var tree = attribute.tree.value();
        if (tree instanceof ScalarTree scalarTree) {
          var name = scalarTree.value();

          if (container.envs.containsKey(name)) {
            container.envs.get(name).add(scalarTree);
          } else {
            var list = new ArrayList<YamlTree>();
            list.add(scalarTree);
            container.envs.put(name, list);
          }
        }
      }
    });
  }

  @Override
  void visitDocumentOnEnd(MappingTree documentTree, CheckContext ctx) {
    System.out.println("END");
    containers.forEach(container -> {
      container.envs.entrySet().stream()
        .filter(entry -> entry.getValue().size() > 1)
        .forEach(entry -> {
          var trees = entry.getValue();
          var secondaryLocations = trees.stream()
            .skip(1)
            .map(t -> new SecondaryLocation(t, MESSAGE_SECONDARY_LOCATION))
            .toList();
          ctx.reportIssue(trees.get(0), MESSAGE, secondaryLocations);
        });
    });
  }

  // Container contains a map of environment variables
  record Container(Map<String, List<YamlTree>> envs) {
  }
}
