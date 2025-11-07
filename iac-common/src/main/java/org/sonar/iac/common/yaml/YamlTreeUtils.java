/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.yaml;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;

import static org.sonarsource.analyzer.commons.collections.ListUtils.getLast;

public final class YamlTreeUtils {
  private YamlTreeUtils() {
  }

  public static List<String> getListValueElements(@Nullable Tree tree) {
    if (tree instanceof ScalarTree scalarTree) {
      return List.of(TextUtils.getValue(scalarTree).orElse(""));
    } else if (tree instanceof SequenceTree sequenceTree) {
      return getValuesOfSequenceTree(sequenceTree);
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Get raw value of this YAML scalar. In the parsed tree, leading whitespaces can be removed, e.g. in case of folded scalars.
   * Then it is impossible to navigate the text and get correct text ranges.
   * Moreover, we can't rely on pointers in YamlTreeMetadata, because snakeyaml claims they are only relevant for error reporting
   * and thus can contain incorrect data in case of no error.
   */
  public static String getRawValue(HasTextRange scalarTree, String source) {
    String lineSeparator;
    if (source.contains("\r\n")) {
      lineSeparator = "\r\n";
    } else {
      lineSeparator = "\n";
    }
    var rawLines = source.lines()
      .skip(scalarTree.textRange().start().line() - 1L)
      .limit(scalarTree.textRange().end().line() - scalarTree.textRange().start().line() + 1L)
      .toList();
    var rawLinesValue = String.join(lineSeparator, rawLines);
    return rawLinesValue.substring(scalarTree.textRange().start().lineOffset(), rawLinesValue.length() - getLast(rawLines).length() + scalarTree.textRange().end().lineOffset());
  }

  private static List<String> getValuesOfSequenceTree(SequenceTree tree) {
    return tree.elements().stream()
      .map(YamlTreeUtils::getListValueElements)
      .flatMap(List::stream)
      .toList();
  }
}
