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

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * A parsed Docker image reference. Only the repository is mandatory:
 * <pre>
 * gcr.io:443/distroless/java17-debian12:debug-nonroot@sha256:1234567890abcdef
 * ^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^
 *  registry          repository              tag              digest
 *
 * ubuntu
 * ^^^^^^
 * repository
 * </pre>
 * A Hub namespace prefix (e.g. {@code bitnami}) isn't a registry host without a dot, colon, or {@code localhost},
 * so it stays part of the repository:
 * <pre>
 * bitnami/postgresql:14.2
 * ^^^^^^^^^^^^^^^^^^ ^^^^
 *     repository     tag
 * </pre>
 * The leading part of a multi-segment repository is its namespace, retrievable via {@link #namespace()};
 * the trailing segment has no field of its own:
 * <pre>
 * registry.gitlab.com/group/subgroup/project/image
 * ^^^^^^^^^^^^^^^^^^^ ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *      registry                repository
 *                     ^^^^^^^^^^^^^^^^^^^^^^
 *                           namespace
 * </pre>
 * There is no separate "image" field: an image is the repository resolved to a tag or digest, not its own component.
 */
public record DockerImageReference(@Nullable String registry, String repository, @Nullable String tag, @Nullable String digest) {

  public static Optional<DockerImageReference> parse(String fullImageName) {
    if (fullImageName.isBlank()) {
      return Optional.empty();
    }

    String withoutDigest = fullImageName;
    String digest = null;
    int digestSeparatorIndex = fullImageName.indexOf('@');
    if (digestSeparatorIndex >= 0) {
      withoutDigest = fullImageName.substring(0, digestSeparatorIndex);
      digest = fullImageName.substring(digestSeparatorIndex + 1);
    }

    String registryAndRepository = withoutDigest;
    String tag = null;
    int tagSeparatorIndex = withoutDigest.lastIndexOf(':');
    if (tagSeparatorIndex >= 0) {
      String afterTagSeparator = withoutDigest.substring(tagSeparatorIndex + 1);
      if (!afterTagSeparator.contains("/")) {
        // Otherwise, the colon is a registry port, not a tag separator.
        tag = afterTagSeparator;
        registryAndRepository = withoutDigest.substring(0, tagSeparatorIndex);
      }
    }

    // No image name at all (e.g. ":", ":bar") is not a meaningful reference, regardless of tag/digest.
    if (registryAndRepository.isBlank()) {
      return Optional.empty();
    }

    String registry = null;
    String repository = registryAndRepository;
    int registrySeparatorIndex = registryAndRepository.indexOf('/');
    if (registrySeparatorIndex >= 0) {
      String firstSegment = registryAndRepository.substring(0, registrySeparatorIndex);
      if (looksLikeRegistryHost(firstSegment)) {
        registry = firstSegment;
        repository = registryAndRepository.substring(registrySeparatorIndex + 1);
      }
      // Otherwise, the first segment is a Docker Hub namespace (e.g. "bitnami"), not a registry host,
      // so it stays part of the repository and the registry remains implicit (docker.io).
    }

    return Optional.of(new DockerImageReference(registry, repository, tag, digest));
  }

  // Mirrors Docker's own heuristic for telling a registry host apart from a Hub namespace/organization prefix.
  private static boolean looksLikeRegistryHost(String firstSegment) {
    return firstSegment.contains(".") || firstSegment.contains(":") || "localhost".equals(firstSegment);
  }

  /**
   * The registry with any {@code :port} suffix stripped, or {@code null} if no registry segment is present.
   */
  @Nullable
  public String registryHost() {
    if (registry == null) {
      return null;
    }
    int portSeparatorIndex = registry.indexOf(':');
    return portSeparatorIndex < 0 ? registry : registry.substring(0, portSeparatorIndex);
  }

  public String withoutTagOrDigest() {
    return registry == null ? repository : (registry + "/" + repository);
  }

  /**
   * The namespace/organization/group prefix of the repository — every path segment except the last — or
   * {@code null} if the repository is a single segment.
   */
  @Nullable
  public String namespace() {
    int lastSlashIndex = repository.lastIndexOf('/');
    return lastSlashIndex < 0 ? null : repository.substring(0, lastSlashIndex);
  }

  /**
   * Whether this is the reserved {@code scratch} pseudo-image; any tag or digest is ignored.
   */
  public boolean isScratch() {
    return registry == null && "scratch".equals(repository);
  }

  /**
   * Whether Docker would resolve this to the mutable {@code latest} tag: no digest, and either no tag at all or an explicit "latest".
   * A digest always pins the image, even without a tag.
   */
  public boolean isLatest() {
    return digest == null && (tag == null || "latest".equals(tag));
  }

  public boolean hasSpecificVersion() {
    return tag != null && !tag.isBlank() && !"latest".equals(tag);
  }
}
