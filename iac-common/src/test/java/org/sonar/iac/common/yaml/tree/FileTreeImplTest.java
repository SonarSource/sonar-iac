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
package org.sonar.iac.common.yaml.tree;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.yaml.YamlTreeTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class FileTreeImplTest extends YamlTreeTest {

  @Test
  void shouldParseFileThatContainsSingleDocument() {
    FileTree tree = parse("a: b");
    assertThat(tree.metadata().tag()).isEqualTo("FILE");
    assertThat(tree.children()).hasSize(1);
    assertThat(tree.documents()).hasSize(1);
    assertThat(tree.textRange())
      .hasRange(1, 0, 1, 4)
      .isEqualTo(tree.documents().get(0).textRange());
    assertThat(tree.toHighlight())
      .hasRange(1, 0, 1, 4)
      .isEqualTo(tree.documents().get(0).textRange());
  }

  @Test
  void shouldParseFileThatContainsMultipleDocuments() {
    FileTree tree = parse("a: b\n---\na: b");
    assertThat(tree.metadata().tag()).isEqualTo("FILE");
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.documents()).hasSize(2);
    assertThat(tree.textRange()).hasRange(1, 0, 3, 4);

    List<TextRange> documentRanges = tree.documents().stream().map(HasTextRange::textRange).collect(Collectors.toList());
    assertThat(tree.textRange()).isEqualTo(TextRanges.merge(documentRanges));
    assertThat(tree.toHighlight()).isEqualTo(tree.documents().get(0).textRange());
  }

  @Test
  void shouldParseEmptyContent() {
    assertThrows(ParseException.class, () -> parse(""));
  }

  @Test
  void shouldParseFileWithOnlyAComment() {
    FileTree tree = parse("# foo");
    assertThat(tree.documents()).hasSize(1);
    assertThat(tree.metadata().tag()).isEqualTo("FILE");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 0);
    assertThat(tree.toHighlight()).hasRange(1, 0, 1, 0);
    YamlTree document = tree.documents().get(0);
    assertThat(document).isInstanceOf(MappingTree.class);
    var comments = document.metadata().comments();
    assertThat(comments).hasSize(1);
    assertThat(comments.get(0).value()).isEqualTo("# foo");
    assertThat(comments.get(0).contentText()).isEqualTo(" foo");
  }

  @Test
  void shouldParseEndOfFileComment() {
    var file = parse("""
      a: b
      # comment
      """);
    assertThat(file.metadata().comments()).isNotEmpty();
  }
}
