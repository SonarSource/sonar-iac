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
package org.sonar.iac.docker.parser;

import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DockerNodeBuilderTest {

  private final DockerNodeBuilder nodeBuilder = new DockerNodeBuilder();

  @Test
  void shouldAddCommentToFollowingToken() {
    setCommentMap(buildCommentMap(1));
    TextRange tokenRange = TextRanges.range(2, 1, 2, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).hasSize(1);
  }

  @Test
  void shouldAddNoCommentToFollowingToken() {
    setCommentMap(buildCommentMap(3));
    TextRange tokenRange = TextRanges.range(2, 1, 2, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).isEmpty();
  }

  @Test
  void shouldAddMultipleCommentsToFollowingToken() {
    setCommentMap(buildCommentMap(1, 2));
    TextRange tokenRange = TextRanges.range(3, 1, 3, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).hasSize(2);
  }

  @Test
  void shouldAddSuitableCommentOnlyToFollowingToken() {
    setCommentMap(buildCommentMap(1, 3));
    TextRange tokenRange = TextRanges.range(2, 1, 2, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).hasSize(1);
  }

  @Test
  void shouldAddInlineCommentToToken() {
    setCommentMap(buildCommentMap(2));
    TextRange tokenRange = TextRanges.range(1, 1, 3, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).hasSize(1);
  }

  @Test
  void shouldAddMultipleInlineCommentsToToken() {
    setCommentMap(buildCommentMap(2, 3));
    TextRange tokenRange = TextRanges.range(1, 1, 4, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).hasSize(2);
  }

  @Test
  void shouldAddLeadingAndInlineCommentsToToken() {
    setCommentMap(buildCommentMap(1, 3));
    TextRange tokenRange = TextRanges.range(2, 1, 4, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).hasSize(2);
  }

  @Test
  void shouldAddCommentByOrderToToken() {
    SortedMap<Integer, Comment> commentMap = buildCommentMap(2, 4, 1);
    Comment firstComment = commentMap.get(1);
    Comment secondComment = commentMap.get(2);
    setCommentMap(commentMap);
    TextRange tokenRange = TextRanges.range(3, 1, 3, 5);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).containsExactly(firstComment, secondComment);
  }

  @Test
  void shouldResetComments() {
    SortedMap<Integer, Comment> firstCommentMap = buildCommentMap(1);
    setCommentMap(firstCommentMap);
    SortedMap<Integer, Comment> secondCommentMap = buildCommentMap();
    setCommentMap(secondCommentMap);
    TextRange tokenRange = TextRanges.range(2, 1, 2, 3);
    assertThat(nodeBuilder.getCommentsForToken(tokenRange)).isEmpty();
  }

  private void setCommentMap(SortedMap<Integer, Comment> commentMap) {
    nodeBuilder.setPreprocessorResult(new DockerPreprocessor.PreprocessorResult(null, null, commentMap));
  }

  private static SortedMap<Integer, Comment> buildCommentMap(int... commentLines) {
    SortedMap<Integer, Comment> commentMap = new TreeMap<>();
    for (int line : commentLines) {
      commentMap.put(line, mock(Comment.class));
    }
    return commentMap;
  }



}
