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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.kubernetes.model.ProjectResource;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;

import static org.sonar.iac.common.yaml.TreePredicates.isSetString;

public abstract class AbstractResourceManagementCheck<T extends ProjectResource> extends AbstractKubernetesObjectCheck {
  protected static final String KIND_POD = "Pod";
  protected static final List<String> KIND_WITH_TEMPLATE = List.of("DaemonSet", "Deployment", "Job", "ReplicaSet", "ReplicationController", "StatefulSet", "CronJob");
  protected static final Set<String> LIMIT_RANGE_LIMIT_TYPES = Set.of("Pod", "Container");
  private String namespace;

  @Override
  protected boolean shouldVisitWholeDocument() {
    return true;
  }

  @Override
  public void initialize(InitContext init) {
    register(KIND_POD, this::computeNamespace);
    register(KIND_WITH_TEMPLATE, this::computeNamespace);
    super.initialize(init);
  }

  private void computeNamespace(BlockObject document) {
    this.namespace = CheckUtils.retrieveNamespace(document);
  }

  @Nullable
  static HasTextRange getFirstChildElement(BlockObject blockObject) {
    if (blockObject.tree != null) {
      return blockObject.tree.elements().get(0).key();
    }
    return null;
  }

  abstract Class<T> getGlobalResourceType();

  protected Set<String> getLimitRangeLimitTypes() {
    return LIMIT_RANGE_LIMIT_TYPES;
  }

  protected Collection<T> getGlobalResources(BlockObject document) {
    var projectContext = ((KubernetesCheckContext) document.ctx).projectContext();
    var inputFileContext = ((KubernetesCheckContext) document.ctx).inputFileContext();
    return projectContext.getProjectResources(namespace, inputFileContext, getGlobalResourceType());
  }

  static boolean isSet(@Nullable String value) {
    return value != null && isSetString().test(value);
  }
}
