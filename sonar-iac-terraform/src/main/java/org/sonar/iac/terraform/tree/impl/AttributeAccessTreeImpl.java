/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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

import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

import java.util.Arrays;
import java.util.List;

public class AttributeAccessTreeImpl extends TerraformTreeImpl implements AttributeAccessTree {
  private final ExpressionTree object;
  private final SyntaxToken accessToken;
  private final SyntaxToken attribute;

  public AttributeAccessTreeImpl(ExpressionTree object, SyntaxToken accessToken, SyntaxToken attribute) {
    this.object = object;
    this.accessToken = accessToken;
    this.attribute = attribute;
  }

  @Override
  public List<TerraformTree> children() {
    return Arrays.asList(object, accessToken, attribute);
  }

  @Override
  public ExpressionTree object() {
    return object;
  }

  @Override
  public SyntaxToken attribute() {
    return attribute;
  }

  @Override
  public SyntaxToken accessToken() {
    return accessToken;
  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE_ACCESS;
  }
}
