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
package org.sonar.iac.helm.tree.utils;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class GoTemplateAstHelperTest {
  @Disabled("TODO fix me")
  @Test
  void shouldFindValuesByTextRange() {
    String text = code("apiVersion: apps/v1 #1",
      "hostIPC: {{ .Values.hostIPC }} #2 #3");
    var textNode1 = new TextNodeImpl(0, "apiVersion: apps/v1 #1\nhostIPC: ");
    var textNode2 = new TextNodeImpl(53, " #2\n #3");
    var fieldNode = new FieldNodeImpl(42, List.of("Values", "hostIPC"));
    var command = new CommandNodeImpl(35, List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(35, List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(35, pipeNode);
    ListNodeImpl root = new ListNodeImpl(0, List.of(textNode1, actionNode, textNode2));
    var goTemplateTree = new GoTemplateTreeImpl("name", "name", 0, root);

    var actual = GoTemplateAstHelper.findNodes(
      goTemplateTree,
      TextRanges.range(2, 0, 2, 36),
      text);

    assertThat(actual).contains(List.of("Values", "hostIPC"));
  }
}
