/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

public class PropertyImpl extends AbstractArmTreeImpl implements Property {

  private final TextTree key;
  private final SyntaxToken colon;
  private final Expression value;

  public PropertyImpl(TextTree key, SyntaxToken colon, Expression value) {
    this.key = key;
    this.colon = colon;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return List.of(key, colon, value);
  }

  @Override
  public TextTree key() {
    return key;
  }

  @Override
  public Expression value() {
    return value;
  }

  @Override
  public String toString() {
    return key + ": " + value;
  }
}
