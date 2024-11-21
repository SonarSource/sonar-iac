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
package org.sonar.iac.arm.tree.impl.json;

import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class BooleanLiteralImpl extends ExpressionImpl implements BooleanLiteral {

  private final boolean value;

  public BooleanLiteralImpl(boolean value, YamlTreeMetadata metadata) {
    super(metadata);
    this.value = value;
  }

  @Override
  public boolean value() {
    return value;
  }
}
