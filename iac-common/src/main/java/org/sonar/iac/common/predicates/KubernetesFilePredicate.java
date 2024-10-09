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
package org.sonar.iac.common.predicates;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.YamlIdentifierFilePredicate;

public class KubernetesFilePredicate extends YamlIdentifierFilePredicate {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesFilePredicate.class);

  // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
  private static final Set<String> IDENTIFIER_PATTERNS = Set.of("^apiVersion", "^kind", "^metadata");
  private final boolean isDebugEnabled;

  public KubernetesFilePredicate(boolean isDebugEnabled) {
    super(IDENTIFIER_PATTERNS);
    this.isDebugEnabled = isDebugEnabled;
  }

  @Override
  public boolean apply(InputFile inputFile) {
    var result = super.apply(inputFile);
    if (!result && isDebugEnabled) {
      LOG.debug("File without Kubernetes identifier: {}", inputFile);
    }
    return result;
  }
}
