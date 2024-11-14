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
package org.sonar.iac.common.yaml.object;

import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.tree.YamlTree;

class YamlObject<K extends YamlTree> {

  public final CheckContext ctx;
  public final @Nullable K tree;
  public final @Nullable String key;
  public final Status status;

  protected enum Status {
    PRESENT,
    ABSENT,
    UNKNOWN
  }

  protected YamlObject(CheckContext ctx, @Nullable K tree, @Nullable String key, Status status) {
    this.ctx = ctx;
    this.tree = tree;
    this.key = key;
    this.status = status;
  }

  public boolean isPresent() {
    return status == Status.PRESENT;
  }

  public boolean isAbsent() {
    return status == Status.ABSENT;
  }
}
