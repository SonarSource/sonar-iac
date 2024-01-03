/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
