/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.kubernetes.visitors;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.kubernetes.model.ConfigMap;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.MapResource;
import org.sonar.iac.kubernetes.model.Secret;
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

    var serviceAccount = (ServiceAccount) ProjectResourceFactory.createResource("serviceAccount.yaml", tree);

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

    var serviceAccount = ProjectResourceFactory.createResource("serviceAccount.yaml", tree);

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

    var serviceAccount = ProjectResourceFactory.createResource("serviceAccount.yaml", tree);

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

    var memoryLimit = (LimitRange) ProjectResourceFactory.createResource("limitRange.yaml", tree);

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

    var memoryLimit = (LimitRange) ProjectResourceFactory.createResource("limitRange.yaml", tree);

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

    var memoryLimit = (LimitRange) ProjectResourceFactory.createResource("limitRange.yaml", tree);

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

    var limitRange = (LimitRange) ProjectResourceFactory.createResource("limitRange.yaml", tree);

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

    var serviceAccount = ProjectResourceFactory.createResource("serviceAccount.yaml", tree);
    assertThat(serviceAccount).isNull();

    var limitRange = ProjectResourceFactory.createResource("limitRange.yaml", tree);
    assertThat(limitRange).isNull();
  }

  private static Stream<Arguments> provideMapResource() {
    return Stream.of(
      Arguments.of("ConfigMap", ConfigMap.class),
      Arguments.of("Secret", Secret.class));
  }

  @ParameterizedTest
  @MethodSource("provideMapResource")
  void shouldCreateMapResource(String kindName, Class<? extends MapResource> clazz) {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: %s
      metadata:
        name: my-data
        namespace: my-namespace
      data:
        key1: "value1"
        key2: "value2"
      """.formatted(kindName);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var mapResource = (MapResource) ProjectResourceFactory.createResource("mapResource.yaml", tree);

    assertThat(mapResource).isInstanceOf(clazz);
    assertThat(mapResource.filePath()).isEqualTo("mapResource.yaml");
    assertThat(mapResource.name()).isEqualTo("my-data");
    assertThat(mapResource.values()).containsKeys("key1", "key2");

    assertThat(mapResource.values().get("key1")).isInstanceOf(TupleTree.class);
    var tuple1 = (TupleTree) mapResource.values().get("key1");
    assertThat(tuple1.key())
      .isInstanceOf(ScalarTree.class)
      .extracting(s -> ((ScalarTree) s).value())
      .isEqualTo("key1");
    assertThat(tuple1.value())
      .isInstanceOf(ScalarTree.class)
      .extracting(s -> ((ScalarTree) s).value())
      .isEqualTo("value1");

    var tuple2 = (TupleTree) mapResource.values().get("key2");
    assertThat(tuple2.key())
      .isInstanceOf(ScalarTree.class)
      .extracting(s -> ((ScalarTree) s).value())
      .isEqualTo("key2");
    assertThat(tuple2.value())
      .isInstanceOf(ScalarTree.class)
      .extracting(s -> ((ScalarTree) s).value())
      .isEqualTo("value2");
  }

  @ParameterizedTest
  @MethodSource("provideMapResource")
  void shouldParseMapResourceWithComplexValue(String kindName, Class<? extends MapResource> clazz) {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: %s
      metadata:
        name: my-data
        namespace: my-namespace
      data:
        key1:
          subkey:
            "value1"
      """.formatted(kindName);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var mapResource = (MapResource) ProjectResourceFactory.createResource("mapResource.yaml", tree);

    assertThat(mapResource).isInstanceOf(clazz);
    assertThat(mapResource.filePath()).isEqualTo("mapResource.yaml");
    assertThat(mapResource.name()).isEqualTo("my-data");
    assertThat(mapResource.values()).containsKeys("key1");
    assertThat(mapResource.values().get("key1"))
      .isInstanceOf(TupleTree.class);
    var tuple = (TupleTree) mapResource.values().get("key1");
    assertThat(tuple.value()).isInstanceOf(MappingTree.class);
  }

  @ParameterizedTest
  @MethodSource("provideMapResource")
  void shouldStayEmptyForIncorrectMapResourceDataFormat(String kindName, Class<? extends MapResource> clazz) {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: %s
      metadata:
        name: my-data
        namespace: my-namespace
      data:
      - key1: "value1"
      """.formatted(kindName);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var mapResource = (MapResource) ProjectResourceFactory.createResource("mapResource.yaml", tree);

    assertThat(mapResource).isInstanceOf(clazz);
    assertThat(mapResource.filePath()).isEqualTo("mapResource.yaml");
    assertThat(mapResource.name()).isEqualTo("my-data");
    assertThat(mapResource.values()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("provideMapResource")
  void shouldHandleKeyNotScalarInMapResource(String kindName, Class<? extends MapResource> clazz) {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: %s
      metadata:
        name: my-data
        namespace: my-namespace
      data:
        ? name: John
        : "value"
      """.formatted(kindName);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var mapResource = (MapResource) ProjectResourceFactory.createResource("mapResource.yaml", tree);

    assertThat(mapResource).isInstanceOf(clazz);
    assertThat(mapResource.filePath()).isEqualTo("mapResource.yaml");
    assertThat(mapResource.name()).isEqualTo("my-data");
    assertThat(mapResource.values()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("provideMapResource")
  void shouldCreateMapResourceWithoutData(String kindName, Class<? extends MapResource> clazz) {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: %s
      metadata:
        name: my-data
        namespace: my-namespace
      """.formatted(kindName);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var mapResource = (MapResource) ProjectResourceFactory.createResource("mapResource.yaml", tree);

    assertThat(mapResource).isInstanceOf(clazz);
    assertThat(mapResource.filePath()).isEqualTo("mapResource.yaml");
    assertThat(mapResource.name()).isEqualTo("my-data");
    assertThat(mapResource.values()).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("provideMapResource")
  void shouldCreateMapResourceWithoutName(String kindName, Class<? extends MapResource> clazz) {
    // language=yaml
    var code = """
      apiVersion: v1
      kind: %s
      metadata:
        namespace: my-namespace
      data:
      - key1: "value1"
      """.formatted(kindName);
    var tree = (MappingTree) PARSER.parse(code, null).documents().get(0);

    var mapResource = (MapResource) ProjectResourceFactory.createResource("mapResource.yaml", tree);

    assertThat(mapResource).isInstanceOf(clazz);
    assertThat(mapResource.filePath()).isEqualTo("mapResource.yaml");
    assertThat(mapResource.name()).isNull();
    assertThat(mapResource.values()).isEmpty();
  }
}
