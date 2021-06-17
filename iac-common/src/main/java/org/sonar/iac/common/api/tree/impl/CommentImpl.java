/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.api.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;

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
