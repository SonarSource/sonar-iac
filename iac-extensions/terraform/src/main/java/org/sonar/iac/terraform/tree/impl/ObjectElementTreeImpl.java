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
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class ObjectElementTreeImpl extends TerraformTreeImpl implements ObjectElementTree {
  private final ExpressionTree key;
  private final SyntaxToken equalOrColonSign;
  private final ExpressionTree value;

  public ObjectElementTreeImpl(ExpressionTree key, SyntaxToken equalOrColonSign, ExpressionTree value) {
    this.key = key;
    this.equalOrColonSign = equalOrColonSign;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(key, equalOrColonSign, value);
  }

  @Override
  public ExpressionTree key() {
    return key;
  }

  @Override
  public SyntaxToken equalOrColonSign() {
    return equalOrColonSign;
  }

  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_ELEMENT;
  }
}
