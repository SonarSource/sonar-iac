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
package org.sonar.iac.kubernetes.plugin;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;

/**
 * Parser for kustomization.yaml files that extracts referenced resources and patches.
 * This parser handles the following kustomization fields:
 * - resources: list of resource files or directories
 * - patches: list of patches (with path field)
 * - patchesStrategicMerge: legacy list of patch files
 * - patchesJson6902: list of JSON patches (with path field)
 *
 * Paths are resolved relative to the kustomization file location and returned as URIs.
 */
public class KustomizationParser {
  private static final Logger LOG = LoggerFactory.getLogger(KustomizationParser.class);
  private final YamlParser yamlParser;

  public KustomizationParser(YamlParser yamlParser) {
    this.yamlParser = yamlParser;
  }

  /**
   * Parses a kustomization file and collects all referenced files.
   *
   * @param sensorContext the sensor context
   * @param inputFile the input file for the kustomization file
   * @return set of URIs referenced in the kustomization
   */
  public Set<URI> parse(SensorContext sensorContext, InputFile inputFile) {
    try {
      var kustomizationFileUri = inputFile.uri();
      var kustomizationFilePath = Path.of(kustomizationFileUri).toAbsolutePath();
      var parentDirPath = kustomizationFilePath.getParent();
      if (parentDirPath == null) {
        LOG.debug("Cannot determine parent directory for kustomization file: {}", kustomizationFilePath);
        return Set.of();
      }

      var inputFileContext = new InputFileContext(sensorContext, inputFile);
      var content = inputFile.contents();
      var tree = yamlParser.parse(content, inputFileContext);
      LOG.debug("Extracting referenced files from the file: {}", kustomizationFilePath);
      return extractReferencedFiles(tree, parentDirPath);
    } catch (IOException e) {
      LOG.debug("Failed to parse kustomization file {}: {}", inputFile, e.getMessage());
      return Set.of();
    }
  }

  /**
   * Extracts all file URIs referenced in a kustomization file and resolves them to absolute URIs.
   * This includes resources and all types of patches.
   *
   * @param fileTree the parsed kustomization file
   * @param parentDirPath the absolute path to the kustomization file parent (used for resolving relative paths)
   * @return set of URIs referenced in the kustomization
   */
  private static Set<URI> extractReferencedFiles(FileTree fileTree, Path parentDirPath) {
    return fileTree.documents().stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .flatMap(document -> Stream.of(
        extractResources(document),
        extractPatches(document),
        extractPatchesStrategicMerge(document),
        extractPatchesJson6902(document)))
      .flatMap(stream -> stream)
      .map(relativePath -> resolvePath(parentDirPath, relativePath))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  /**
   * Resolves a relative path from a kustomization file to an absolute normalized URI.
   *
   * @param parentDirPath the absolute path to the kustomization file parent
   * @param relativePath the relative path from the kustomization file
   * @return normalized absolute URI, or null if resolution fails
   */
  private static URI resolvePath(Path parentDirPath, String relativePath) {
    try {
      var resolvedPath = parentDirPath.resolve(relativePath).normalize();
      var uri = resolvedPath.toUri();
      LOG.debug("Resolved kustomization reference: {}", relativePath);
      return uri;
    } catch (Exception e) {
      LOG.debug("Failed to resolve path '{}' relative to '{}': {}", relativePath, parentDirPath, e.getMessage());
      return null;
    }
  }

  /**
   * Extracts paths from the 'resources' field.
   * Resources can be:
   * - Individual files (e.g., "deployment.yaml")
   * - Directories (e.g., "../base")
   * - Remote URLs (ignored)
   *
   * @param document the kustomization document
   * @return stream of local resource paths
   */
  private static Stream<String> extractResources(MappingTree document) {
    return extractScalarsFromSequence(document, "resources");
  }

  /**
   * Extracts paths from the 'patches' field.
   * Patches can be:
   * - Objects with 'path' field pointing to a file
   * - Inline patches (ignored, no file reference)
   *
   * @param document the kustomization document
   * @return stream of local patch paths
   */
  private static Stream<String> extractPatches(MappingTree document) {
    return extractMappingTreePaths(document, "patches");
  }

  /**
   * Extracts paths from the 'patchesStrategicMerge' field (legacy).
   * These are simple file paths in a list.
   *
   * @param document the kustomization document
   * @return stream of local strategic merge patch paths
   */
  private static Stream<String> extractPatchesStrategicMerge(MappingTree document) {
    return extractScalarsFromSequence(document, "patchesStrategicMerge");
  }

  /**
   * Extracts paths from the 'patchesJson6902' field.
   * These are objects with 'path' field pointing to JSON patch files.
   *
   * @param document the kustomization document
   * @return stream of local JSON patch paths
   */
  private static Stream<String> extractPatchesJson6902(MappingTree document) {
    return extractMappingTreePaths(document, "patchesJson6902");
  }

  /**
   * Checks if a path is a local file path (not a remote URL).
   * Remote resources (URLs) are ignored as they cannot be analyzed locally.
   */
  private static boolean isLocalPath(String path) {
    return !path.startsWith("http://") && !path.startsWith("https://");
  }

  @Nonnull
  private static Stream<String> extractMappingTreePaths(MappingTree document, String key) {
    return PropertyUtils.value(document, key)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .stream()
      .flatMap(patches -> patches.elements().stream())
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .flatMap(patchMapping -> PropertyUtils.value(patchMapping, "path")
        .filter(ScalarTree.class::isInstance)
        .map(ScalarTree.class::cast)
        .map(ScalarTree::value)
        .filter(KustomizationParser::isLocalPath)
        .stream());
  }

  @Nonnull
  private static Stream<String> extractScalarsFromSequence(MappingTree document, String key) {
    return PropertyUtils.value(document, key)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .stream()
      .flatMap(resources -> resources.elements().stream())
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value)
      .filter(KustomizationParser::isLocalPath);
  }
}
