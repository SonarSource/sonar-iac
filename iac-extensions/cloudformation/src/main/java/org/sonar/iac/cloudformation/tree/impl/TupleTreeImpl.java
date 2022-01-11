/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.tree.Tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TupleTreeImpl extends CloudformationTreeImpl implements TupleTree {
  private final CloudformationTree key;
  private final CloudformationTree value;

  public TupleTreeImpl(CloudformationTree key, CloudformationTree value, TextRange textRange) {
    // Comments are attached to the key and value trees separately
    super(textRange, Collections.emptyList());
    this.key = key;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(key, value);
  }

  @Override
  public CloudformationTree key() {
    return key;
  }

  @Override
  public CloudformationTree value() {
    return value;
  }

  @Override
  public String tag() {
    return "TUPLE";
  }
}
