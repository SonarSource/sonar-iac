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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.ServiceAccount;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectResourceFactoryTest {
  private static final TreeParser<FileTree> PARSER = new YamlParser();

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldCreateServiceAccount(boolean automountServiceAccountToken) {
    var code = """
      apiVersion: v1
      kind: ServiceAccount
      metadata:
        name: my-service-account
        namespace: my-namespace
      automountServiceAccountToken: %s
      """.formatted(automountServiceAccountToken);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var serviceAccount = (ServiceAccount) ProjectResourceFactory.createServiceAccount(tree);

    assertThat(serviceAccount).isNotNull();
    assertThat(serviceAccount.name()).isEqualTo("my-service-account");
    if (automountServiceAccountToken) {
      assertThat(serviceAccount.automountServiceAccountToken()).isEqualTo(Trilean.TRUE);
    } else {
      assertThat(serviceAccount.automountServiceAccountToken()).isEqualTo(Trilean.FALSE);
    }
  }

  @Test
  void shouldCreateServiceAccountWithoutAutomountServiceAccountToken() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: ServiceAccount
      metadata:
        name: my-service-account
        namespace: my-namespace
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var serviceAccount = ProjectResourceFactory.createServiceAccount(tree);

    assertThat(serviceAccount).isNotNull()
      .extracting("name", "automountServiceAccountToken")
      .containsExactly("my-service-account", Trilean.UNKNOWN);
  }

  @Test
  void shouldReturnNullIfServiceAccountHasNoName() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: ServiceAccount
      metadata:
        namespace: my-namespace
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var serviceAccount = ProjectResourceFactory.createServiceAccount(tree);

    assertThat(serviceAccount).isNull();
  }

  @Test
  void shouldCreateMemoryLimit() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: LimitRange
      metadata:
        name: name constraint
      spec:
        limits:
        - default:
            cpu: 0.5
          defaultRequest:
            memory: 50Mi\s
          maxLimitRequestRatio:
            cpu: 500m
          max:
            storage: 2Gi
          min:
            storage: 1Gi
          type: Container
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var memoryLimit = (LimitRange) ProjectResourceFactory.createLimitRange(tree);

    assertThat(memoryLimit).isNotNull();
    assertThat(memoryLimit.limits()).hasSize(1);

    var limit = memoryLimit.limits().get(0);
    assertThat(limit.defaultMap()).containsEntry("cpu", "0.5");
    assertThat(limit.defaultRequestMap()).containsEntry("memory", "50Mi");
    assertThat(limit.maxLimitRequestRatioMap()).containsEntry("cpu", "500m");
    assertThat(limit.max()).containsEntry("storage", "2Gi");
    assertThat(limit.min()).containsEntry("storage", "1Gi");
  }

  @Test
  void shouldCreateLimitRangeWithoutSomeLimits() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: LimitRange
      metadata:
        name: name constraint
      spec:
        limits:
        - default:
            cpu: 0.5
          type: Container
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var memoryLimit = (LimitRange) ProjectResourceFactory.createLimitRange(tree);

    var limit = memoryLimit.limits().get(0);
    assertThat(limit.defaultMap()).containsEntry("cpu", "0.5");
    assertThat(limit.defaultRequestMap()).isEmpty();
    assertThat(limit.maxLimitRequestRatioMap()).isEmpty();
    assertThat(limit.max()).isEmpty();
    assertThat(limit.min()).isEmpty();
  }

  @Test
  void shouldCreateLimitRangeFromMultipleLimits() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: LimitRange
      metadata:
        name: name constraint
      spec:
        limits:
        - default:
            cpu: 0.5
          type: Container
        - defaultRequest:
            memory: 50Mi
          type: Pod
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var memoryLimit = (LimitRange) ProjectResourceFactory.createLimitRange(tree);

    assertThat(memoryLimit.limits()).hasSize(2);

    var containerLimit = memoryLimit.limits().get(0);
    assertThat(containerLimit.defaultMap()).containsEntry("cpu", "0.5");
    assertThat(containerLimit.defaultRequestMap()).isEmpty();
    assertThat(containerLimit.maxLimitRequestRatioMap()).isEmpty();
    assertThat(containerLimit.max()).isEmpty();
    assertThat(containerLimit.min()).isEmpty();

    var podLimit = memoryLimit.limits().get(1);
    assertThat(podLimit.defaultMap()).isEmpty();
    assertThat(podLimit.defaultRequestMap()).containsEntry("memory", "50Mi");
    assertThat(podLimit.maxLimitRequestRatioMap()).isEmpty();
    assertThat(podLimit.max()).isEmpty();
    assertThat(podLimit.min()).isEmpty();
  }

  @Test
  void shouldPutEmptyMapIfLimitItemIsNotMapping() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: LimitRange
      metadata:
        name: name constraint
      spec:
        limits:
        - default: 0.5
          type: Container
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var limitRange = (LimitRange) ProjectResourceFactory.createLimitRange(tree);

    var limit = limitRange.limits().get(0);
    assertThat(limit.defaultMap()).isEmpty();
  }

  @Test
  void shouldReturnNullForOtherKinds() {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: Pod
      metadata:
        name: my-pod
      spec:
        containers:
        - name: my-container
          image: my-image
      """;
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var serviceAccount = ProjectResourceFactory.createServiceAccount(tree);
    assertThat(serviceAccount).isNull();

    var limitRange = ProjectResourceFactory.createLimitRange(tree);
    assertThat(limitRange).isNull();
  }
}
