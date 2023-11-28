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
package org.sonar.iac.kubernetes.visitors;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.FileTreeImpl;
import org.sonar.iac.kubernetes.plugin.HelmProcessor;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class CommentLocationVisitorTest {

  private final LocationShifter shifter = new LocationShifter();

  @Test
  void shouldFindShiftedLocation() throws IOException {
    scanFile("helm",
      code("test:",
        "  key:value"),
      code("test: #1",
        "  additional:line",
        "  key:value #2"));

    TextRange shiftedLocation = shifter.computeShiftedLocation(TextRanges.range(3, 1, 3, 5));
    assertThat(shiftedLocation).hasRange(2, 0, 2, 11);
  }

  private FileTree scanFile(String template, String originalCode, String transformedCode) throws IOException {
    InputFileContext inputFileContext = mockInputFileContext("test.yaml", originalCode);
    FileTree file = new KubernetesParser(new HelmProcessor()).parse(transformedCode, inputFileContext);
    file = new FileTreeImpl(file.documents(), file.metadata(), template);
    CommentLocationVisitor visitor = new CommentLocationVisitor(shifter);
    visitor.initialize();
    visitor.scan(inputFileContext, file);
    return file;
  }

  private InputFileContext mockInputFileContext(String name, String content) throws IOException {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn(name);
    when(inputFile.inputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    when(inputFile.charset()).thenReturn(StandardCharsets.UTF_8);
    return new InputFileContext(mock(SensorContext.class), inputFile);
  }
}
