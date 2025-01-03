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

import java.util.List;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.parser.HclParser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public abstract class AbstractSymbolTest {

  private final HclParser parser = new HclParser();

  CheckContext ctx = mock(CheckContext.class);
  BlockSymbol parentBlock = BlockSymbol.fromPresent(ctx, parseBlock("parent_block {}"), null);

  protected FileTree parse(String source) {
    return (FileTree) parser.parse(source);
  }

  protected BlockTree parseBlock(String source) {
    return parse(source).properties().stream().map(BlockTree.class::cast).findFirst().orElseThrow();
  }

  protected AttributeTree parseAttribute(String source) {
    return parse(source).properties().stream().map(AttributeTree.class::cast).findFirst().orElseThrow();
  }

  protected void assertNoIssueReported() {
    verify(ctx, never()).reportIssue(any(HasTextRange.class), anyString(), anyList());
  }

  protected void assertIssueReported(HasTextRange hasTextRange, String message, SecondaryLocation... secondaries) {
    verify(ctx).reportIssue(hasTextRange, message, List.of(secondaries));
  }
}
