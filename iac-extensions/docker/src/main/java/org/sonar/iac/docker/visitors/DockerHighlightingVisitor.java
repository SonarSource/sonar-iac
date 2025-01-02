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
package org.sonar.iac.docker.visitors;

import java.util.Optional;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.tree.api.Instruction;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;

public class DockerHighlightingVisitor extends SyntaxHighlightingVisitor {

  @Override
  protected void languageSpecificHighlighting() {
    register(Instruction.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(FromInstruction.class, (ctx, tree) -> Optional.ofNullable(tree.alias()).ifPresent(alias -> highlight(alias.keyword(), KEYWORD)));
  }
}
