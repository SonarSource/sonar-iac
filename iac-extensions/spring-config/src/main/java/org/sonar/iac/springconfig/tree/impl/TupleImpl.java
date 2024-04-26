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
package org.sonar.iac.springconfig.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.springconfig.tree.api.Scalar;
import org.sonar.iac.springconfig.tree.api.Tuple;

public class TupleImpl extends SpringConfigImpl implements Tuple {
  private final Scalar key;
  private final Scalar value;

  public TupleImpl(Scalar key, Scalar value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(key, value);
  }

  @Override
  public Scalar key() {
    return key;
  }

  @Override
  public Scalar value() {
    return value;
  }
}
