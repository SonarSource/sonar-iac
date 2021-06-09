/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CloudformationExclusionsFileFilterTest {

  // TODO add test cases for yaml

  private static MapSettings settings = new MapSettings();

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void default_should_exclude_nothing_by_path() {
    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, CloudformationExtension.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json")));
    assertTrue(filter.accept(inputFile("vendor/file.json")));
    assertTrue(filter.accept(inputFile("vendor/someDir/file.json")));
    assertTrue(filter.accept(inputFile("someDir/vendor/file.json")));
  }

  @Test
  void should_exclude_using_custom_path_regex() {
    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.json")));
    assertTrue(filter.accept(inputFile("someDir/file.json")));
    assertFalse(filter.accept(inputFile("path/file.json")));
    assertFalse(filter.accept(inputFile("someDir/path/file.json")));
  }

  @Test
  void should_exclude_other_language() {
    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertFalse(filter.accept(inputFile("path/file.json")));
    assertTrue(filter.accept(inputFile("path/file.go")));
  }

  @Test
  void should_ignore_empty_path_regex() {
    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, "," + CloudformationExtension.EXCLUSIONS_DEFAULT_VALUE + ",");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.json")));
  }

  @Test
  void default_should_exclude_only_files_without_identifier() {
    settings.setProperty(CloudformationExtension.FILE_IDENTIFIER_KEY, CloudformationExtension.FILE_IDENTIFIER_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertTrue(filter.accept(inputFile("file.json", "\nSomeValue\n\"AWSTemplateFormatVersion\"")));
    assertFalse(filter.accept(inputFile("file.json", "")));
    assertFalse(filter.accept(inputFile("file.json", "DifferentIdentifier")));
  }

  @Test
  void should_exclude_nothing_with_empty_identifier() {
    settings.setProperty(CloudformationExtension.FILE_IDENTIFIER_KEY, "");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertTrue(filter.accept(inputFile("file.json", "")));
    assertTrue(filter.accept(inputFile("file.json", "DifferentIdentifier")));
  }

  @Test
  void should_exclude_using_custom_identifier_regex() {
    settings.setProperty(CloudformationExtension.FILE_IDENTIFIER_KEY, "DifferentIdentifier");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertFalse(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertFalse(filter.accept(inputFile("file.json", "")));
    assertTrue(filter.accept(inputFile("file.json", "DifferentIdentifier")));
  }

  @Test
  void should_exclude_identifier_case_sensitive() {
    settings.setProperty(CloudformationExtension.FILE_IDENTIFIER_KEY, CloudformationExtension.FILE_IDENTIFIER_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertFalse(filter.accept(inputFile("file.json", "awstemplateformatversion")));
  }

  @Test
  void test_invalid_file() throws IOException {
    InputFile inputFile = inputFile("fakeFile.json", "");
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.inputStream()).thenThrow(IOException.class);
    settings.setProperty(CloudformationExtension.FILE_IDENTIFIER_KEY, CloudformationExtension.FILE_IDENTIFIER_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertFalse(filter.accept(spyInputFile));

    assertThat(logTester.logs()).contains(String.format("Unable to read file: %s.", inputFile.uri()));
  }

  private InputFile inputFile(String filename) {
    return inputFile(filename, CloudformationExtension.FILE_IDENTIFIER_DEFAULT_VALUE);
  }

  private InputFile inputFile(String filename, String contents) {
    return new TestInputFileBuilder("test","test_path/" + filename)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(language(filename))
      .setContents(contents)
      .build();
  }

  private static String language(String filename) {
    String extension = filename.split("\\.")[1];
    return  "json".equals(extension) ? "cloudformation" : extension;
  }
}
