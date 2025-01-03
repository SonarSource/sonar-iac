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

import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.testing.TreeModelTest;
import org.sonar.sslr.grammar.GrammarRuleKey;

public abstract class BicepTreeModelTest extends TreeModelTest<ArmTree> {
  @Override
  protected ActionParser<ArmTree> createParser(GrammarRuleKey rootRule) {
    return BicepParser.create(rootRule);
  }

  @Override
  protected Class<? extends ArmTree> leafClass() {
    return SyntaxToken.class;
  }
}
