/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.visitors;

import java.util.Optional;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.InstructionTree;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;

public class DockerHighlightingVisitor extends SyntaxHighlightingVisitor {

  @Override
  protected void languageSpecificHighlighting() {
    register(InstructionTree.class, (ctx, tree) -> highlight(tree.keyword(), KEYWORD));
    register(FromTree.class, (ctx, tree) -> Optional.ofNullable(tree.alias()).ifPresent(alias -> highlight(alias.keyword(), KEYWORD)));
  }



}
