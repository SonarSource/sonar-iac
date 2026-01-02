/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.SequenceTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

public class YamlTreeTestUtils {
  private static final YamlTreeMetadata METADATA = new YamlTreeMetadata(null, null, 0, 0, Collections.emptyList());
  private static final ScalarTree.Style STYLE = ScalarTree.Style.PLAIN;

  public static ScalarTree scalar(String value) {
    return new ScalarTreeImpl(value, STYLE, METADATA);
  }

  public static SequenceTree sequence(String... values) {
    List<YamlTree> elements = Arrays.stream(values).map(v -> new ScalarTreeImpl(v, STYLE, METADATA)).collect(Collectors.toList());
    return new SequenceTreeImpl(elements, METADATA);
  }
}
