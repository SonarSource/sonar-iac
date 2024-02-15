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
package org.sonar.iac.kubernetes.visitors;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.Checks;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.common.yaml.YamlParser;

import static org.sonar.iac.common.testing.IacTestUtils.code;

class AdjustableChecksVisitorTest {
  @Test
  void shouldFindValueInValuesFile() throws IOException {
    var textRange = getTextRangeFor(code(
      "foo:",
      "  bar:",
      "    baz: qux"), List.of("foo", "bar", "baz"));

    TextRangeAssert.assertThat(textRange).hasRange(3, 9, 3, 12);
  }

  @Test
  void shouldReturnNullForMissingPath() throws IOException {
    var textRange = getTextRangeFor(code(
      "foo:",
      "  bar:",
      "    baz: qux"), List.of("foo", "baz"));

    TextRangeAssert.assertThat(textRange).isNull();
  }

  @Test
  void shouldReturnNullForListNode() throws IOException {
    var textRange = getTextRangeFor(code(
      "foo:",
      "  bar:",
      "    - baz: qux"), List.of("foo", "bar", "baz"));

    TextRangeAssert.assertThat(textRange).isNull();
  }

  @CheckForNull
  private TextRange getTextRangeFor(String valuesFileContent, List<String> valuePath) throws IOException {
    var adjustableChecksVisitor = new AdjustableChecksVisitor(Mockito.mock(Checks.class), null, null, new YamlParser());
    var adjustableCheckContext = (AdjustableChecksVisitor.AdjustableContextAdapter) adjustableChecksVisitor.context(null);

    var valuesFile = new TestInputFileBuilder("test", ".")
      .setContents(valuesFileContent)
      .build();
    var inputFileContext = new HelmInputFileContext(null, null);
    inputFileContext.setAdditionalFiles(Map.of("values.yaml", valuesFile));

    return adjustableCheckContext.toLocationInValuesFile(valuePath, inputFileContext);
  }
}
