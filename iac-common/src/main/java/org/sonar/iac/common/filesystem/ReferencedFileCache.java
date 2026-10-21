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
package org.sonar.iac.common.filesystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.filesystem.FileSystemUtils.ReferencedFile;

/**
 * Per-analysis cache for referenced-file resolution and parsed results.
 * Resolutions, including misses, use the reference as written; parsed results use the resolved canonical path and {@link ParsedFileType}.
 * Empty results remain cached as empty {@code Optional} values, so a miss is resolved or parsed only once.
 * Not thread-safe.
 */
public final class ReferencedFileCache {

  private static final Logger LOG = LoggerFactory.getLogger(ReferencedFileCache.class);

  private final Map<Reference, Optional<ReferencedFile>> resolved = new HashMap<>();
  private final Map<ParsedKey, Optional<?>> parsed = new HashMap<>();

  public <R> Optional<R> read(SensorContext sensorContext, @Nullable Path workingDirectory, String referencedPath, ParsedFileType<R> type) {
    var file = resolve(sensorContext, workingDirectory, referencedPath);
    if (file.isEmpty()) {
      return Optional.empty();
    }
    return parse(file.get(), type);
  }

  private Optional<ReferencedFile> resolve(SensorContext sensorContext, @Nullable Path workingDirectory, String referencedPath) {
    return resolved.computeIfAbsent(new Reference(workingDirectory, referencedPath),
      key -> FileSystemUtils.resolveReferencedFile(sensorContext, workingDirectory, referencedPath));
  }

  /**
   * Package-private so test fixtures can reuse production parsed-result caching.
   */
  @SuppressWarnings("unchecked")
  <R> Optional<R> parse(ReferencedFile file, ParsedFileType<R> type) {
    return (Optional<R>) parsed.computeIfAbsent(new ParsedKey(file.canonicalPath(), type),
      key -> file.contents().flatMap(content -> tryParse(file.canonicalPath(), content, type)));
  }

  /**
   * Parsers see only content, so a failure is named here, where the path is known. A parser defect surfaces the same
   * way: these reads are a best-effort side channel, and no check should fail an analysis over one.
   */
  private static <R> Optional<R> tryParse(Path canonicalPath, String content, ParsedFileType<R> type) {
    try {
      return Optional.of(type.parse(content));
    } catch (RuntimeException e) {
      LOG.debug("Could not parse referenced file {}", canonicalPath, e);
      return Optional.empty();
    }
  }

  private record Reference(@Nullable Path workingDirectory, String referencedPath) {
  }

  private record ParsedKey(Path canonicalPath, ParsedFileType<?> type) {
  }
}
