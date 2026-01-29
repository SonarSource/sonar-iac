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

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Provider that stores information about kustomization-referenced files.
 * Files stored here are resources or patches referenced in kustomization.yaml files
 * and should not be analyzed by checks to avoid false positives on incomplete manifests.
 */
@ScannerSide
@SonarLintSide
public class KustomizationInfoProvider {
  private int kustomizationFilesCount = 0;
  private final Set<Path> kustomizationReferencedFiles = new HashSet<>();

  public void addKustomizationReferencedFiles(Collection<Path> c) {
    kustomizationReferencedFiles.addAll(c);
  }

  public Set<Path> kustomizationReferencedFiles() {
    return kustomizationReferencedFiles;
  }

  public int kustomizationReferencedFilesCount() {
    return kustomizationReferencedFiles.size();
  }

  public int kustomizationFilesCount() {
    return kustomizationFilesCount;
  }

  public void incrementKustomizationFilesCount() {
    kustomizationFilesCount++;
  }
}
