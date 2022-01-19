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

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FunctionCallTree;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;

public class FunctionCallTreeImpl extends CloudformationTreeImpl implements FunctionCallTree {

  private String name;
  private Style style;
  private List<CloudformationTree> arguments;

  public FunctionCallTreeImpl(String name, Style style, List<CloudformationTree> arguments, TextRange textRange, List<Comment> comments) {
    super(textRange, comments);
    this.name = name;
    this.style = style;
    this.arguments = arguments;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(arguments);
  }

  @Override
  public String tag() {
    return "FUNCTION_CALL";
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Style style() {
    return style;
  }

  @Override
  public List<CloudformationTree> arguments() {
    return arguments;
  }
}
