/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.iac.common.testing.TreeModelTest;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.sslr.grammar.GrammarRuleKey;

public abstract class TerraformTreeModelTest extends TreeModelTest<TerraformTree> {
  @Override
  protected ActionParser<TerraformTree> createParser(GrammarRuleKey rootRule) {
    return new HclParser(rootRule);
  }

  @Override
  protected Class<? extends TerraformTree> leafClass() {
    return SyntaxToken.class;
  }
}
