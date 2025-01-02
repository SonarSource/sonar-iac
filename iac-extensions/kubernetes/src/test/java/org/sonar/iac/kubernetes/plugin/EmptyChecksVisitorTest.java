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
package org.sonar.iac.kubernetes.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.TupleTreeImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class EmptyChecksVisitorTest {

  @Test
  void shouldNotInteractWithContext() {
    var visitor = new EmptyChecksVisitor();
    InputFileContext ctx = mock(InputFileContext.class);
    Tree root = new TupleTreeImpl(null, null, null);
    visitor.scan(ctx, root);
    verifyNoInteractions(ctx);
  }
}
