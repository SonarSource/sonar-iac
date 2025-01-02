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
package org.sonar.iac.terraform.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.TupleTree;

import static org.assertj.core.api.Assertions.assertThat;

class ListSymbolTest extends AbstractSymbolTest {

  @Test
  void report_fromPresent() {
    AttributeTree tree = parseAttribute("my_list = [\"my_item\"]");
    ListSymbol list = ListSymbol.fromPresent(ctx, tree, parentBlock);
    list.report("message");
    assertIssueReported(tree.key(), "message");
  }

  @Test
  void report_fromAbsent() {
    ListSymbol list = ListSymbol.fromAbsent(ctx, "my_list", parentBlock);
    list.report("message");
    assertNoIssueReported();
  }

  @Test
  void reportItemIf_fromPresent() {
    AttributeTree tree = parseAttribute("my_list = [\"my_item\"]");
    ListSymbol list = ListSymbol.fromPresent(ctx, tree, parentBlock);
    list.reportItemIf(e -> true, "message");
    assertIssueReported(((TupleTree) tree.value()).elements().trees().get(0), "message");
  }

  @Test
  void reportItemIf_fromAbsent() {
    ListSymbol list = ListSymbol.fromAbsent(ctx, "my_list", parentBlock);
    list.reportItemIf(e -> true, "message");
    assertNoIssueReported();
  }

  @Test
  void reportItemIf_fromInvalid() {
    AttributeTree tree = parseAttribute("my_list = not_a_list");
    ListSymbol list = ListSymbol.fromPresent(ctx, tree, parentBlock);
    assertThat(list.isPresent()).isFalse();
    list.reportItemIf(e -> true, "message");
    assertNoIssueReported();
  }

}
