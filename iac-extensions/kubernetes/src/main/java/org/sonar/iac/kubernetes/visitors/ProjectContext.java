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
package org.sonar.iac.kubernetes.visitors;

import org.sonar.iac.common.checks.Trilean;

/**
 * Data class to provide information about the project. This allows to share cross-file knowledge to the individual checks.
 */
public final class ProjectContext {

  private Trilean hasLimitRange = Trilean.FALSE;

  private ProjectContext() {
  }

  public boolean hasNoLimitRange() {
    return hasLimitRange.isFalse();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Build pattern is used to make context fields readonly, but also allows to flexible add fields to the context without changing the constructor.
   */
  public static class Builder {

    private final ProjectContext ctx;

    public Builder() {
      this.ctx = new ProjectContext();
    }

    public Builder setLimitRange(Trilean hasLimitRange) {
      ctx.hasLimitRange = hasLimitRange;
      return this;
    }

    public ProjectContext build() {
      return ctx;
    }

  }

}
