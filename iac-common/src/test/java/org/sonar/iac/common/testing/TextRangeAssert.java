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
package org.sonar.iac.common.testing;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.sonar.iac.common.api.tree.impl.TextRange;

public class TextRangeAssert extends AbstractAssert<TextRangeAssert, TextRange> {

  public TextRangeAssert(@Nullable TextRange actual) {
    super(actual, TextRangeAssert.class);
  }

  public TextRangeAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    isNotNull();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.start().line()).as("startLine mismatch").isEqualTo(startLine);
      softly.assertThat(actual.start().lineOffset()).as("startLineOffset mismatch").isEqualTo(startLineOffset);
      softly.assertThat(actual.end().line()).as("endLine mismatch").isEqualTo(endLine);
      softly.assertThat(actual.end().lineOffset()).as("endLineOffset mismatch").isEqualTo(endLineOffset);
    });
    return this;
  }

  public static TextRangeAssert assertTextRange(@Nullable TextRange actual) {
    return new TextRangeAssert(actual);
  }
}
