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

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceSymbolTest extends AbstractSymbolTest {

  @Test
  void report() {
    BlockTree tree = parseBlock("resource \"my_type\" \"my_name\" {}");
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    resource.report("message");
    assertIssueReported(tree.labels().get(0), "message");
  }

  @Test
  void reportIfAbsent() {
    BlockTree tree = parseBlock("resource \"my_type\" \"my_name\" {}");
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    assertThatThrownBy(() -> resource.reportIfAbsent("message"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessage("Resource symbols should always exists");
  }

  @Test
  void reportIfAbsent_with_resource_as_parent() {
    BlockTree resourceTree = parseBlock("resource \"my_type\" \"my_name\" {}");
    BlockSymbol resource = ResourceSymbol.fromPresent(ctx, resourceTree);
    BlockSymbol child = BlockSymbol.fromAbsent(ctx, "missing_block", resource);
    child.reportIfAbsent("%s");
    assertIssueReported(resourceTree.labels().get(0), "missing_block");
  }

  @Test
  void create_incomplete_resource() {
    BlockTree tree = parseBlock("resource {}");
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    assertThat(resource.type).isEmpty();
    resource.report("message", Collections.emptyList());
    assertNoIssueReported();
  }
}
