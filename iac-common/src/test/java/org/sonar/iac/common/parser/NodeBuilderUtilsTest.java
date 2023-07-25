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
package org.sonar.iac.common.parser;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.parser.NodeBuilderUtils.createComments;
import static org.sonar.iac.common.parser.NodeBuilderUtils.tokenRange;

class NodeBuilderUtilsTest {

  @Test
  void testCreateComments() {
    Trivia trivia1 = mockTrivia("// my comment", 1, 1);
    Trivia trivia2 = mockTrivia("# my comment", 2, 1);
    Trivia trivia3 = mockTrivia("/* my comment */", 3, 1);
    List<Comment> comments = createComments(List.of(trivia1, trivia2, trivia3));
    assertThat(comments.get(0).value()).isEqualTo("// my comment");
    assertThat(comments.get(0).contentText()).isEqualTo(" my comment");
    assertRange(comments.get(0).textRange(), 1, 1, 1, 14);
    assertThat(comments.get(1).value()).isEqualTo("# my comment");
    assertThat(comments.get(1).contentText()).isEqualTo(" my comment");
    assertRange(comments.get(1).textRange(), 2, 1, 2, 13);
    assertThat(comments.get(2).value()).isEqualTo("/* my comment */");
    assertThat(comments.get(2).contentText()).isEqualTo(" my comment ");
    assertRange(comments.get(2).textRange(), 3, 1, 3, 17);
  }

  @Test
  void testTokenRangeWithByteOrder() {
    Input input = mockInput("\uFEFFmy file content", 4, 1, 4);
    TextRange range = tokenRange(input, 4, "file content");
    assertRange(range, 1, 2, 1, 14);
  }

  @Test
  void testTokenRangeWithoutByteOrder() {
    Input input = mockInput("my file content", 3, 1, 3);
    TextRange range = tokenRange(input, 3, "file content");
    assertRange(range, 1, 2, 1, 14);
  }

  @Test
  void testTokenRangeEmptyFile() {
    Input input = mockInput("", 1, 1, 1);
    TextRange range = tokenRange(input, 1, "");
    assertRange(range, 1, 0, 1, 0);
  }

  void assertRange(TextRange range, int startLine, int startOffset, int endLine, int endOffset) {
    assertThat(range.start().line()).isEqualTo(startLine);
    assertThat(range.start().lineOffset()).isEqualTo(startOffset);
    assertThat(range.end().line()).isEqualTo(endLine);
    assertThat(range.end().lineOffset()).isEqualTo(endOffset);
  }

  static Trivia mockTrivia(String commentValue, int line, int column) {
    Trivia trivia = mock(Trivia.class);
    Token token = mock(Token.class);
    when(trivia.getToken()).thenReturn(token);
    when(token.getValue()).thenReturn(commentValue);
    when(token.getLine()).thenReturn(line);
    when(token.getColumn()).thenReturn(column);
    return trivia;
  }

  static Input mockInput(String fileContent, int startIndexCall, int line, int column) {
    Input input = mock(Input.class);
    when(input.lineAndColumnAt(startIndexCall)).thenReturn(new int[] {line, column});
    when(input.input()).thenReturn(fileContent.toCharArray());
    return input;
  }
}
