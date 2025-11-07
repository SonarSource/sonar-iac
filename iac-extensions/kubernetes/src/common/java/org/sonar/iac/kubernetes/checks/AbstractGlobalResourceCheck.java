/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.kubernetes.checks;

import java.util.Set;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

public abstract class AbstractGlobalResourceCheck extends AbstractKubernetesObjectCheck {
  @Override
  protected boolean shouldVisitWholeDocument() {
    return true;
  }

  protected <T extends ProjectResource> Set<T> findGlobalResources(Class<T> clazz, String namespace, BlockObject document) {
    var projectContext = ((KubernetesCheckContext) document.ctx).projectContext();
    var inputFileContext = ((KubernetesCheckContext) document.ctx).inputFileContext();
    return projectContext.getNamespaceProjectResources(namespace, inputFileContext, clazz);
  }

  protected <T extends ProjectResource> Set<T> findGlobalResources(Class<T> clazz, BlockObject document) {
    var projectContext = ((KubernetesCheckContext) document.ctx).projectContext();
    var inputFileContext = ((KubernetesCheckContext) document.ctx).inputFileContext();
    return projectContext.getProjectResources(inputFileContext, clazz);
  }
}
