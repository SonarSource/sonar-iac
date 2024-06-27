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
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.kubernetes.model.LimitRange;
import org.sonar.iac.kubernetes.model.LimitRangeItem;

@Rule(key = "S6864")
public class MemoryLimitCheck extends AbstractLimitCheck {
  private static final String MESSAGE = "Specify a memory limit for this container.";
  private static final String KEY = "memory";
  private static final Set<String> LIMIT_TYPES = Set.of("Pod", "Container");

  @Override
  protected boolean hasLimitDefinedGlobally(Collection<LimitRange> globalResources) {
    return globalResources.stream()
      .flatMap(limitRange -> limitRange.limits().stream())
      .anyMatch(MemoryLimitCheck::hasMemoryLimit);
  }

  @Override
  String getResourceName() {
    return KEY;
  }

  @Override
  String getMessage() {
    return MESSAGE;
  }

  private static boolean hasMemoryLimit(LimitRangeItem limitRangeItem) {
    var defaultMemoryLimit = limitRangeItem.defaultMap().get(KEY);
    return LIMIT_TYPES.contains(limitRangeItem.type()) && isValidMemory(defaultMemoryLimit);
  }

  static boolean isValidMemory(@Nullable String value) {
    return value != null && !value.isEmpty() && Character.isDigit(value.charAt(0));
  }
}
