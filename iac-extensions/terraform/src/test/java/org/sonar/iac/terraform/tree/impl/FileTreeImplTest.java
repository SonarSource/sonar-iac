/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class FileTreeImplTest extends TerraformTreeModelTest {
  
  @Test
  void empty_file() {
    FileTree tree = parse("", HclLexicalGrammar.FILE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FILE);
    assertThat(tree.properties()).isEmpty();
  }

  @Test
  void with_body() {
    FileTree tree = parse("a = 1", HclLexicalGrammar.FILE);
    assertThat(tree).isInstanceOfSatisfying(FileTreeImpl.class, f -> assertThat(f.properties()).hasSize(1));
  }
}
