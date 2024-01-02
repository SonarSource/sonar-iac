/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.docker.tree.api;

import org.assertj.core.api.AbstractAssert;

public abstract class DockerTreeAssert<SELF extends DockerTreeAssert<SELF, ACTUAL>, ACTUAL extends DockerTree> extends AbstractAssert<SELF, ACTUAL> {

  protected DockerTreeAssert(ACTUAL actual, Class<?> selfType) {
    super(actual, selfType);
  }

  public SELF hasKind(DockerTree.Kind kind) {
    isNotNull();
    if (actual.getKind() != kind) {
      failWithMessage("Expected Docker Tree kind to be <%s> but was <%s>", kind, actual.getKind());
    }
    return (SELF) this;
  }
}
