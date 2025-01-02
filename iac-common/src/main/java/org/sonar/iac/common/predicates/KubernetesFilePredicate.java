/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.YamlIdentifierFilePredicate;

public class KubernetesFilePredicate implements FilePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesFilePredicate.class);

  private final FilePredicate predicate;
  // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
  private static final Set<String> IDENTIFIER_PATTERNS = Set.of("^apiVersion", "^kind", "^metadata");
  private final boolean isDebugEnabled;

  public KubernetesFilePredicate(boolean isDebugEnabled) {
    predicate = new YamlIdentifierFilePredicate(IDENTIFIER_PATTERNS);
    this.isDebugEnabled = isDebugEnabled;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    var result = predicate.apply(inputFile);
    if (!result && isDebugEnabled) {
      LOG.debug("File without Kubernetes identifier: {}", inputFile);
    }
    return result;
  }
}
