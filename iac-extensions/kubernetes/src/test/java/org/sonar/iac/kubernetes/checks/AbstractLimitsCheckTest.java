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
package org.sonar.iac.kubernetes.checks;

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.object.BlockObject;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;
import org.sonar.iac.kubernetes.visitors.ProjectContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.kubernetes.checks.AbstractLimitsCheck.getFirstChildElement;

class AbstractLimitsCheckTest {
  static MappingTree CONTAINER_TREE;

  static {
    var parser = new YamlParser();
    CONTAINER_TREE = (MappingTree) parser.parse("""
        containers:
          - name: foo
      """, null).documents().get(0);
  }

  static TextRange ABSENT_TEXT_RANGE = TextRanges.range(1, 2, 1, 12);
  static String MESSAGE = "message";

  @Test
  void testGetFirstChildElement() {
    CheckContext checkContext = mock(CheckContext.class);
    BlockObject block = BlockObject.fromAbsent(checkContext, "a");
    HasTextRange firstChildElement = getFirstChildElement(block);
    assertThat(firstChildElement).isNull();
  }

  CheckContext checkContextAfterReportMissingLimit(ProjectContext projectContext) {
    KubernetesCheckContext checkContext = mock(KubernetesCheckContext.class);
    when(checkContext.projectContext()).thenReturn(projectContext);
    BlockObject container = BlockObject.fromPresent(checkContext, CONTAINER_TREE, null);
    var check = new TestAbstractLimitsCheck(null);
    check.reportMissingLimit(container);
    return checkContext;
  }

  static class TestAbstractLimitsCheck extends AbstractLimitsCheck {

    private final String limitAttributeKey;

    TestAbstractLimitsCheck(@Nullable String limitAttributeKey) {
      this.limitAttributeKey = limitAttributeKey;
    }

    @Override
    String getLimitAttributeKey() {
      return limitAttributeKey;
    }

    @Override
    String getMessage() {
      return MESSAGE;
    }
  }
}
