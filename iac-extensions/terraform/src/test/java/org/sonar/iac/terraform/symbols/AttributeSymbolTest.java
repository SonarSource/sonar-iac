/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

class AttributeSymbolTest extends AbstractSymbolTest{

  @Test
  void report_fromPresent() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.report("message");
    assertIssueReported(tree, "message");
  }

  @Test
  void report_fromAbsent() {
    AttributeSymbol attribute = AttributeSymbol.fromAbsent(ctx, "my_attribute", parentBlock);
    attribute.report("message");
    assertNoIssueReported();
  }

  @Test
  void reportIfAbsent_fromAbsent() {
    AttributeSymbol attribute = AttributeSymbol.fromAbsent(ctx, "my_attribute", parentBlock);
    attribute.reportIfAbsent("%s");
    assertIssueReported(parentBlock.tree.key(), "my_attribute");
  }

  @Test
  void reportIfAbsent_fromPresent() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.reportIfAbsent("%s");
    assertNoIssueReported();
  }

  @Test
  void reportIf_fromPresent() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.reportIf(e -> true, "message");
    assertIssueReported(tree, "message");
  }

  @Test
  void reportIf_fromAbsent() {
    AttributeSymbol attribute = AttributeSymbol.fromAbsent(ctx, "my_attribute", parentBlock);
    attribute.reportIf(e -> true, "message");
    assertNoIssueReported();
  }

  @Test
  void reportIf_not_matching_predicate() {
    AttributeTree tree = parseAttribute("my_attribute = 1");
    AttributeSymbol attribute = AttributeSymbol.fromPresent(ctx, tree, parentBlock);
    attribute.reportIf(e -> false, "message");
    assertNoIssueReported();
  }
}
