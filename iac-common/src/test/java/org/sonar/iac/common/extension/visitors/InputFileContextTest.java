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
package org.sonar.iac.common.extension.visitors;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.impl.TextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class InputFileContextTest {

  private static final TextRange INVALID_RANGE = range(1, 2, 0, 1);
  private static final TextRange EMPTY_RANGE = range(1, 1, 1, 1);
  @TempDir
  Path tempDir;

  private SensorContextTester sensorContext;
  private InputFileContext inputFileContext;

  @BeforeEach
  void beforeEach() {
    DefaultInputFile inputFile = new TestInputFileBuilder("moduleKey", "file")
      .setCharset(StandardCharsets.UTF_8)
      .initMetadata("The content of the file.")
      .build();

    sensorContext = SensorContextTester.create(tempDir);
    inputFileContext = new InputFileContext(sensorContext, inputFile);
  }

  @Test
  void primary_location_range_is_not_added_when_range_is_null() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), null, "message", List.of());
    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).primaryLocation().textRange()).isNull();
  }

  @Test
  void primary_location_range_is_not_added_when_range_is_invalid_or_empty() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), EMPTY_RANGE, "message", List.of());
    inputFileContext.reportIssue(RuleKey.parse("s:43"), INVALID_RANGE, "message", List.of());

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(2)
      .allSatisfy(i -> assertThat(i.primaryLocation().textRange()).isNull());
  }

  @Test
  void secondary_location_is_not_added_when_its_range_is_invalid_or_empty() {
    inputFileContext.reportIssue(RuleKey.parse("s:42"), range(1, 1, 1, 2), "message", List.of(
      new SecondaryLocation(EMPTY_RANGE, "message"),
      new SecondaryLocation(INVALID_RANGE, "message")));

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).flows()).isEmpty();
  }

  @Test
  void secondary_location_is_not_added_when_it_is_null() {
    ArrayList<SecondaryLocation> secondaries = new ArrayList<>();
    secondaries.add(null);
    inputFileContext.reportIssue(RuleKey.parse("s:42"), range(1, 1, 1, 2), "message", secondaries);

    List<Issue> issues = new ArrayList<>(sensorContext.allIssues());
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).flows()).isEmpty();
  }
}
