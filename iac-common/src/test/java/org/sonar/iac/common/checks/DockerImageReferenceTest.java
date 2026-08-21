/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DockerImageReferenceTest {

  @Test
  void shouldParsePlainName() {
    var ref = DockerImageReference.parse("ubuntu").orElseThrow();
    assertThat(ref.registry()).isNull();
    assertThat(ref.registryHost()).isNull();
    assertThat(ref.repository()).isEqualTo("ubuntu");
    assertThat(ref.tag()).isNull();
    assertThat(ref.digest()).isNull();
  }

  @Test
  void shouldParseNameWithTag() {
    var ref = DockerImageReference.parse("ubuntu:20.04").orElseThrow();
    assertThat(ref.registry()).isNull();
    assertThat(ref.repository()).isEqualTo("ubuntu");
    assertThat(ref.tag()).isEqualTo("20.04");
    assertThat(ref.digest()).isNull();
  }

  @Test
  void shouldParseNameWithDigest() {
    var ref = DockerImageReference.parse("ubuntu@sha256:06b5d30fabc1").orElseThrow();
    assertThat(ref.repository()).isEqualTo("ubuntu");
    assertThat(ref.tag()).isNull();
    assertThat(ref.digest()).isEqualTo("sha256:06b5d30fabc1");
  }

  @Test
  void shouldParseNameWithTagAndDigest() {
    var ref = DockerImageReference.parse("ubuntu:20.04@sha256:06b5d30fabc1").orElseThrow();
    assertThat(ref.repository()).isEqualTo("ubuntu");
    assertThat(ref.tag()).isEqualTo("20.04");
    assertThat(ref.digest()).isEqualTo("sha256:06b5d30fabc1");
  }

  @Test
  void shouldParseRegistryWithPortAndTag() {
    var ref = DockerImageReference.parse("gcr.io:443/distroless/java17-debian12:debug-nonroot").orElseThrow();
    assertThat(ref.registry()).isEqualTo("gcr.io:443");
    assertThat(ref.registryHost()).isEqualTo("gcr.io");
    assertThat(ref.repository()).isEqualTo("distroless/java17-debian12");
    assertThat(ref.tag()).isEqualTo("debug-nonroot");
    assertThat(ref.digest()).isNull();
  }

  @Test
  void shouldParseRegistryWithPortAndDigest() {
    var ref = DockerImageReference.parse("customHost:8080/repo@sha256:06b5d30fabc1").orElseThrow();
    assertThat(ref.registry()).isEqualTo("customHost:8080");
    assertThat(ref.registryHost()).isEqualTo("customHost");
    assertThat(ref.repository()).isEqualTo("repo");
    assertThat(ref.tag()).isNull();
    assertThat(ref.digest()).isEqualTo("sha256:06b5d30fabc1");
  }

  @Test
  void shouldParseRegistryWithPortTagAndDigest() {
    var ref = DockerImageReference.parse("customHost:8080/repo:1.2.3@sha256:06b5d30fabc1").orElseThrow();
    assertThat(ref.registry()).isEqualTo("customHost:8080");
    assertThat(ref.registryHost()).isEqualTo("customHost");
    assertThat(ref.repository()).isEqualTo("repo");
    assertThat(ref.tag()).isEqualTo("1.2.3");
    assertThat(ref.digest()).isEqualTo("sha256:06b5d30fabc1");
  }

  @Test
  void shouldParseRegistryWithoutPort() {
    var ref = DockerImageReference.parse("gcr.io/distroless/java17-debian12:debug-nonroot").orElseThrow();
    assertThat(ref.registry()).isEqualTo("gcr.io");
    assertThat(ref.registryHost()).isEqualTo("gcr.io");
    assertThat(ref.repository()).isEqualTo("distroless/java17-debian12");
  }

  @Test
  void shouldTreatHubNamespaceAsPartOfRepositoryNotRegistry() {
    var ref = DockerImageReference.parse("bitnami/postgresql:14.2").orElseThrow();
    assertThat(ref.registry()).isNull();
    assertThat(ref.registryHost()).isNull();
    assertThat(ref.repository()).isEqualTo("bitnami/postgresql");
    assertThat(ref.tag()).isEqualTo("14.2");
  }

  @Test
  void shouldComputeNamespaceAsAllButLastRepositorySegment() {
    assertThat(DockerImageReference.parse("ubuntu").orElseThrow().namespace()).isNull();
    assertThat(DockerImageReference.parse("bitnami/postgresql").orElseThrow().namespace()).isEqualTo("bitnami");
    assertThat(DockerImageReference.parse("registry.gitlab.com/group/subgroup/project/image").orElseThrow().namespace())
      .isEqualTo("group/subgroup/project");
  }

  @Test
  void shouldParseLocalhostAsRegistry() {
    var ref = DockerImageReference.parse("localhost/custom/dotnet").orElseThrow();
    assertThat(ref.registry()).isEqualTo("localhost");
    assertThat(ref.registryHost()).isEqualTo("localhost");
    assertThat(ref.repository()).isEqualTo("custom/dotnet");
  }

  @Test
  void shouldTreatDottedNameWithoutSlashAsRepositoryNotRegistry() {
    // Without a "/", the string is unambiguously a repository name to Docker, even if it looks host-like.
    var ref = DockerImageReference.parse("myregistry.io:5000").orElseThrow();
    assertThat(ref.registry()).isNull();
    assertThat(ref.repository()).isEqualTo("myregistry.io");
    assertThat(ref.tag()).isEqualTo("5000");
  }

  @Test
  void shouldParseRegistryWithPortAndNoTag() {
    var ref = DockerImageReference.parse("customHost:8080/custom/dotnet/aspnet/8.0").orElseThrow();
    assertThat(ref.registry()).isEqualTo("customHost:8080");
    assertThat(ref.repository()).isEqualTo("custom/dotnet/aspnet/8.0");
    assertThat(ref.tag()).isNull();
  }

  @Test
  void shouldComputeWithoutTagOrDigest() {
    assertThat(DockerImageReference.parse("ubuntu:20.04").orElseThrow().withoutTagOrDigest()).isEqualTo("ubuntu");
    assertThat(DockerImageReference.parse("customHost:8080/custom/dotnet:1.2.3").orElseThrow().withoutTagOrDigest())
      .isEqualTo("customHost:8080/custom/dotnet");
  }

  @Test
  void shouldRecognizeScratchRegardlessOfTagOrDigest() {
    assertThat(DockerImageReference.parse("scratch").orElseThrow().isScratch()).isTrue();
    assertThat(DockerImageReference.parse("scratch:1.2.3").orElseThrow().isScratch()).isTrue();
    assertThat(DockerImageReference.parse("scratch@sha256:06b5d30fabc1").orElseThrow().isScratch()).isTrue();
  }

  @Test
  void shouldNotRecognizeScratchWithARegistryOrDifferentRepository() {
    assertThat(DockerImageReference.parse("myregistry.io/scratch").orElseThrow().isScratch()).isFalse();
    assertThat(DockerImageReference.parse("ubuntu").orElseThrow().isScratch()).isFalse();
  }

  @Test
  void shouldRecognizeLatestWhenNoTagAndNoDigest() {
    assertThat(DockerImageReference.parse("ubuntu").orElseThrow().isLatest()).isTrue();
    assertThat(DockerImageReference.parse("ubuntu:latest").orElseThrow().isLatest()).isTrue();
    assertThat(DockerImageReference.parse("ubuntu:20.04").orElseThrow().isLatest()).isFalse();
  }

  @Test
  void shouldNotRecognizeLatestWhenPinnedByDigest() {
    // A digest always pins the image, even without a tag or with an explicit "latest" tag.
    assertThat(DockerImageReference.parse("ubuntu@sha256:06b5d30fabc1").orElseThrow().isLatest()).isFalse();
    assertThat(DockerImageReference.parse("ubuntu:latest@sha256:06b5d30fabc1").orElseThrow().isLatest()).isFalse();
  }

  @Test
  void shouldHaveSpecificVersionOnlyForARealNonLatestTag() {
    assertThat(DockerImageReference.parse("ubuntu:20.04").orElseThrow().hasSpecificVersion()).isTrue();
    assertThat(DockerImageReference.parse("ubuntu:latest").orElseThrow().hasSpecificVersion()).isFalse();
    assertThat(DockerImageReference.parse("ubuntu").orElseThrow().hasSpecificVersion()).isFalse();
    assertThat(DockerImageReference.parse("ubuntu:").orElseThrow().hasSpecificVersion()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", ":", ":bar"})
  void shouldRejectReferencesWithoutAnImageName(String malformed) {
    assertThat(DockerImageReference.parse(malformed)).isEmpty();
  }

  @Test
  void shouldParseBlankTagAndDigestAsPresentButEmpty() {
    // A trailing separator with nothing after it isn't a well-formed reference, but it still carries a
    // (blank) tag/digest rather than none at all - consumers decide for themselves whether that counts
    // as "present". This also matches what an unresolved ${VAR} collapses to between separators.
    var withBlankTag = DockerImageReference.parse("foo:").orElseThrow();
    assertThat(withBlankTag.repository()).isEqualTo("foo");
    assertThat(withBlankTag.tag()).isEmpty();
    assertThat(withBlankTag.digest()).isNull();

    var withBlankDigest = DockerImageReference.parse("foo@").orElseThrow();
    assertThat(withBlankDigest.repository()).isEqualTo("foo");
    assertThat(withBlankDigest.tag()).isNull();
    assertThat(withBlankDigest.digest()).isEmpty();
  }
}
