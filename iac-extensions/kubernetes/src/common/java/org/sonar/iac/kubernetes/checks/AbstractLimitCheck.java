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
import java.util.Map;
import java.util.stream.Stream;
import org.sonar.iac.common.yaml.TreePredicates;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.LimitRangeItem;

public abstract class AbstractLimitCheck extends AbstractResourceManagementCheck<LimitRange> {

  private static final String RESOURCE_MANAGEMENT_TYPE = "limits";

  @Override
  protected void registerObjectCheck() {
    register(KIND_POD, document -> checkDocument(document, false));
    register(KIND_WITH_TEMPLATE, document -> checkDocument(document, true));
  }

  private void checkDocument(BlockObject document, boolean isKindWithTemplate) {
    var globalResources = getGlobalResources(document);

    Stream<BlockObject> containers;
    if (isKindWithTemplate) {
      containers = document.block("spec").block("template").block("spec").blocks("containers");
    } else {
      containers = document.block("spec").blocks("containers");
    }
    containers.filter(container -> !hasLimitDefinedGlobally(globalResources))
      .forEach(this::reportMissingLimit);
  }

  protected void reportMissingLimit(BlockObject container) {
    container.block("resources").block(getResourceManagementName())
      .attribute(getResourceName())
      .reportIfAbsent(getFirstChildElement(container), getMessage())
      .reportIfValue(TreePredicates.isSet().negate(), getMessage());
  }

  protected boolean hasLimitDefinedGlobally(Collection<LimitRange> globalResources) {
    return globalResources.stream()
      .flatMap(limitRange -> limitRange.limits().stream())
      .anyMatch(this::hasDefinedLimitForResource);
  }

  protected boolean hasDefinedLimitForResource(LimitRangeItem limitRangeItem) {
    var limit = retrieveLimitRangeItemMap(limitRangeItem).get(getResourceName());
    return getLimitRangeLimitTypes().contains(limitRangeItem.type()) && isSet(limit);
  }

  @Override
  Class<LimitRange> getGlobalResourceType() {
    return LimitRange.class;
  }

  String getResourceManagementName() {
    return RESOURCE_MANAGEMENT_TYPE;
  }

  abstract String getResourceName();

  abstract String getMessage();

  protected Map<String, String> retrieveLimitRangeItemMap(LimitRangeItem limitRangeItem) {
    return limitRangeItem.defaultMap();
  }
}
