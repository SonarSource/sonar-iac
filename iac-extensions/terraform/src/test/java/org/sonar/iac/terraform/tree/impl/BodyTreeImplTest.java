/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;


class BodyTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_body_with_one_line_block() {
    BodyTree tree = parse("a {}", HclLexicalGrammar.BODY);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BODY);
  }
}
