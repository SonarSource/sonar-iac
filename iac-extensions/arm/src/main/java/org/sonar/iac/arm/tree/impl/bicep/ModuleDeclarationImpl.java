/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.api.tree.Tree;

public class ModuleDeclarationImpl extends AbstractDeclarationImpl<Expression> implements ModuleDeclaration {
  private final List<Decorator> decorators;
  private final InterpolatedString type;

  public ModuleDeclarationImpl(List<Decorator> decorators, SyntaxToken keyword, Identifier name, InterpolatedString type, SyntaxToken equalsSign, Expression value) {
    super(keyword, name, equalsSign, value);
    this.decorators = decorators;
    this.type = type;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(decorators);
    children.add(keyword);
    children.add(name);
    children.add(type);
    children.add(equalsSign);
    children.add(body);
    return children;
  }

  @Override
  public InterpolatedString type() {
    return type;
  }

  @Override
  public List<Decorator> decorators() {
    return decorators;
  }
}
