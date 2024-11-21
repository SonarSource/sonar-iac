/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
