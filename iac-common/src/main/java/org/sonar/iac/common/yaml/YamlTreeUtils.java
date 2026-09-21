/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
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
   * The values of a property written either as a single scalar or as a sequence of scalars. Unlike
   * {@link #getListValueElements(Tree)} it is all or nothing: the result is empty as soon as one entry is not a
   * readable scalar, so that a caller cannot mistake a partially unreadable property for the shorter list its readable
   * entries spell out.
   */
  public static List<String> getListValueElementsOrNone(@Nullable Tree tree) {
    if (tree instanceof SequenceTree sequenceTree) {
      return getListValueElementsOrNone(sequenceTree.elements());
    }
    return readableScalarValue(tree).map(List::of).orElseGet(List::of);
  }

  /**
   * The values of the given trees, empty as soon as one of them is not a readable scalar. Same contract as
   * {@link #getListValueElementsOrNone(Tree)}, for entries a caller collected itself, such as the keys of a mapping.
   */
  public static List<String> getListValueElementsOrNone(List<? extends Tree> trees) {
    List<String> values = new ArrayList<>(trees.size());
    for (Tree tree : trees) {
      var value = readableScalarValue(tree);
      if (value.isEmpty()) {
        return List.of();
      }
      values.add(value.get());
    }
    return List.copyOf(values);
  }

  private static Optional<String> readableScalarValue(@Nullable Tree tree) {
    if (tree instanceof ScalarTree scalarTree && !scalarTree.value().isBlank()) {
      return Optional.of(scalarTree.value());
    }
    return Optional.empty();
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
