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
package org.sonar.iac.common.predicates;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckForNull;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileListener;

/**
 * Cache of the {@link FileType} computed for each file by {@link YamlFileTypeResolver}.
 * It is injected as a singleton so that the file type of a given file is computed only once, even if several YAML based
 * sensors are interested in it.
 * <p>
 * In SonarLint this single instance lives for the whole engine session and is shared across analyses. Because several
 * predicates are content based, a file's resolved {@link FileType} can change between analyses while its URI stays the
 * same, so the cache implements {@link ModuleFileListener} to drop the affected entry whenever a file changes.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.INSTANCE)
public class YamlFileTypeCache implements ModuleFileListener {

  // Shared singleton accessed by all YAML based sensors and, in SonarLint, mutated from module file events which may be
  // delivered on a different thread than the analysis, so the backing map must be thread-safe.
  private final Map<URI, FileType> fileTypeCache = new ConcurrentHashMap<>();

  public YamlFileTypeCache() {
    // Public explicit constructor for injection
  }

  @CheckForNull
  public FileType get(URI fileUri) {
    return fileTypeCache.get(fileUri);
  }

  public void put(URI fileUri, FileType fileType) {
    fileTypeCache.put(fileUri, fileType);
  }

  /**
   * Invalidates the cached {@link FileType} of a file when it is created, modified or deleted, so that it is recomputed
   * on the next analysis instead of returning a type derived from stale content.
   */
  @Override
  public void process(ModuleFileEvent moduleFileEvent) {
    fileTypeCache.remove(moduleFileEvent.getTarget().uri());
  }
}
