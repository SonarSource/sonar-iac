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
package org.sonar.iac.helm.tree.impl;

import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.protobuf.StringNodeOrBuilder;
import org.sonar.iac.helm.tree.api.Node;
import org.sonar.iac.helm.tree.api.StringNode;
import org.sonar.iac.helm.tree.utils.GoTemplateAstConverter;

public class StringNodeImpl extends AbstractNode implements StringNode {
  private final String text;

  public StringNodeImpl(TextRange textRange, String text) {
    super(textRange);
    this.text = text;
  }

  public static Node fromPb(StringNodeOrBuilder nodePb, String source) {
    return new StringNodeImpl(GoTemplateAstConverter.textRangeFromPb(nodePb, source), nodePb.getText());
  }

  public String text() {
    return text;
  }
}
