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
package org.sonar.iac.kubernetes.checks;

import java.util.Set;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

public abstract class AbstractGlobalResourceCheck extends AbstractKubernetesObjectCheck {
  @Override
  boolean shouldVisitWholeDocument() {
    return true;
  }

  protected <T extends ProjectResource> Set<T> findGlobalResources(Class<T> clazz, String namespace, BlockObject document) {
    var projectContext = ((KubernetesCheckContext) document.ctx).projectContext();
    var inputFileContext = ((KubernetesCheckContext) document.ctx).inputFileContext();
    return projectContext.getProjectResources(namespace, inputFileContext, clazz);
  }
}
