/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.MappingTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AbstractKubernetesObjectCheckTest {

  Set<BlockObject> visitedObjects = new HashSet<>();
  AbstractKubernetesObjectCheck check = new AbstractKubernetesObjectCheck() {
    @Override
    protected void registerObjectCheck() {
      register(List.of("Pod", "Job"), pod -> visitedObjects.add(pod));
    }
  };

  @Test
  void shouldNoVisitInvalidObjectStructure() {
    KubernetesVerifier.verifyNoIssue("AbstractKubernetesObjectCheck/invalid_object_structure.yaml", check);
    assertThat(visitedObjects).isEmpty();
  }

  @Test
  void shouldVisitValidObjectStructure() {
    KubernetesVerifier.verifyNoIssue("AbstractKubernetesObjectCheck/valid_object_structure.yaml", check);
    assertThat(visitedObjects).hasSize(1);
  }

  @Test
  void shouldNotVisitNonMatchingObject() {
    KubernetesVerifier.verifyNoIssue("AbstractKubernetesObjectCheck/non_matching_object.yaml", check);
    assertThat(visitedObjects).isEmpty();
  }

  @Test
  void shouldVisitAllOnMultipleObjectFile() {
    KubernetesVerifier.verifyNoIssue("AbstractKubernetesObjectCheck/multiple_objects.yaml", check);
    assertThat(visitedObjects).hasSize(2);
  }

  @Test
  void shouldVisitCustomMappingTree() {
    var parser = new YamlParser();
    var mappingTree = (MappingTree) parser.parse("""
      kind: Pod
      spec:
        attr: something
      """, null).documents().get(0);

    var kubernetesCheckForOtherYaml = check.prepareForEmbeddedYaml();
    kubernetesCheckForOtherYaml.visit(mappingTree, mock());
    assertThat(visitedObjects).hasSize(1);
  }
}
