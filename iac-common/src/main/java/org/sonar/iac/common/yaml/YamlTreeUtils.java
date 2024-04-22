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
package org.sonar.iac.common.yaml;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.SequenceTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class YamlTreeUtils {

  private static final YamlTreeMetadata METADATA = new YamlTreeMetadata(null, null, 0, 0, Collections.emptyList());
  private static final ScalarTree.Style STYLE = ScalarTree.Style.PLAIN;

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

  private static List<String> getValuesOfSequenceTree(SequenceTree tree) {
    return tree.elements().stream()
      .map(YamlTreeUtils::getListValueElements)
      .flatMap(List::stream)
      .toList();
  }

  public static YamlTree scalar(String value) {
    return new ScalarTreeImpl(value, STYLE, METADATA);
  }

  public static YamlTree sequence(String... values) {
    List<YamlTree> elements = Arrays.stream(values).map(v -> new ScalarTreeImpl(v, STYLE, METADATA)).collect(Collectors.toList());
    return new SequenceTreeImpl(elements, METADATA);
  }
}
