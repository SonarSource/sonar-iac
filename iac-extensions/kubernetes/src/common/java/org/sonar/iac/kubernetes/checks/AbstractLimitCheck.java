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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.sonar.iac.common.api.checks.SecondaryLocation;
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

    BlockObject spec;
    if (isKindWithTemplate) {
      spec = document.block("spec").block("template").block("spec");
    } else {
      spec = document.block("spec");
    }
    onEachSpec(spec);

    Stream<BlockObject> containers = spec.blocks("containers");

    containers.filter(container -> !hasLimitDefinedGlobally(globalResources))
      .filter(this::shouldInvestigateContainer)
      .forEach(this::reportMissingLimit);
  }

  protected void reportMissingLimit(BlockObject container) {
    container.block("resources").block(getResourceManagementName())
      .attribute(getResourceName())
      .reportIfAbsent(getFirstChildElement(container), getMessage(), extraSecondaryLocations())
      .reportIfValue(TreePredicates.isSet().negate(), getMessage(), extraSecondaryLocations());
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

  /**
   * Allow to execute a code on each spec block. Guarantee to be executed before calling any {@link #shouldInvestigateContainer}
   * @param spec The spec block that is being processed.
   */
  protected void onEachSpec(BlockObject spec) {
  }

  /**
   * Allow to specify an additional filter on the container.
   * @param container The container which should be visited or not.
   * @return True if you want to investigate the container, false otherwise.
   */
  protected boolean shouldInvestigateContainer(BlockObject container) {
    return true;
  }

  /**
   * Allow to provide extra secondary locations to the issue reported.
   * @return The list of additional secondary locations you want to attach to the reported issue.
   */
  protected List<SecondaryLocation> extraSecondaryLocations() {
    return Collections.emptyList();
  }
}
