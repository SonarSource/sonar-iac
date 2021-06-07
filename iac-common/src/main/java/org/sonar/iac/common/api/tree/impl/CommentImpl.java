/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.common.api.tree.impl;

import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.TextRange;

public class CommentImpl implements Comment {

  private final String value;
  private final String contentText;
  private final TextRange textRange;

  public CommentImpl(String value, String contentText, TextRange textRange) {
    this.value = value;
    this.contentText = contentText;
    this.textRange = textRange;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String contentText() {
    return contentText;
  }

  @Override
  public TextRange textRange() {
    return textRange;
  }
}
