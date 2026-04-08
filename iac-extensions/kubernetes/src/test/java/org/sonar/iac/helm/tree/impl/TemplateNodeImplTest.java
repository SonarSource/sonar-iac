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
package org.sonar.iac.helm.tree.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.helm.tree.api.PipeNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class TemplateNodeImplTest {

  @Test
  void shouldReturnAllChildren() {
    var node = Mockito.mock(PipeNode.class);
    var templateNode = new TemplateNodeImpl(() -> range(1, 0, 1, 10), "dummy", node);
    var actual = templateNode.children();
    assertThat(actual).contains(node);
  }

  @Test
  void shouldReturnEmptyListIfPipeNodeIsNull() {
    var templateNode = new TemplateNodeImpl(() -> range(1, 0, 1, 10), "dummy", null);
    var actual = templateNode.children();
    assertThat(actual).isEmpty();
  }
}
