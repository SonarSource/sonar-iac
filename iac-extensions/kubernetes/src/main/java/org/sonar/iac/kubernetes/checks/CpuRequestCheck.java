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
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.LimitRangeItem;

@Rule(key = "S6892")
public class CpuRequestCheck extends AbstractRequestCheck {
  private static final String MESSAGE = "Specify a CPU request for this container.";
  private static final String KEY = "cpu";

  private static final Set<String> LIMIT_TYPES = Set.of("Pod", "Container");

  @Override
  protected boolean hasLimitDefinedGlobally(Collection<LimitRange> globalResources) {
    return globalResources.stream()
      .flatMap(limitRange -> limitRange.limits().stream())
      .anyMatch(CpuRequestCheck::hasCpuLimit);
  }

  @Override
  String getResourceName() {
    return KEY;
  }

  @Override
  String getMessage() {
    return MESSAGE;
  }

  private static boolean hasCpuLimit(LimitRangeItem limitRangeItem) {
    var defaultCpuRequest = limitRangeItem.defaultRequestMap().get(KEY);
    return LIMIT_TYPES.contains(limitRangeItem.type()) && startsWithDigit(defaultCpuRequest);
  }
}
