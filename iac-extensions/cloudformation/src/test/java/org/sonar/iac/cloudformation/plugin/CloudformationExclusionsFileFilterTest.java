/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  private static final List<String> EXTENSIONS = Arrays.asList("json","yaml","yml");

  private final MapSettings settings = new MapSettings();

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @ParameterizedTest
  @ValueSource(strings = {"json","yaml","yml"})
  void default_should_exclude_nothing_by_path(String extension) {
    settings.setProperty(CloudformationSettings.EXCLUSIONS_KEY, CloudformationSettings.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file." + extension)));
    assertTrue(filter.accept(inputFile("vendor/file." + extension)));
    assertTrue(filter.accept(inputFile("vendor/someDir/file." + extension)));
    assertTrue(filter.accept(inputFile("someDir/vendor/file." + extension)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"json","yaml","yml"})
  void should_exclude_using_custom_path_regex(String extension) {
    settings.setProperty(CloudformationSettings.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file." + extension)));
    assertTrue(filter.accept(inputFile("someDir/file." + extension)));
    assertFalse(filter.accept(inputFile("path/file." + extension)));
    assertFalse(filter.accept(inputFile("someDir/path/file." + extension)));
  }

  @Test
  void should_exclude_other_language() {
    settings.setProperty(CloudformationSettings.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertFalse(filter.accept(inputFile("path/file.json")));
    assertTrue(filter.accept(inputFile("path/file.go")));
  }

  @Test
  void should_ignore_empty_path_regex() {
    settings.setProperty(CloudformationSettings.EXCLUSIONS_KEY, "," + CloudformationSettings.EXCLUSIONS_DEFAULT_VALUE + ",");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.json")));
  }

  @Test
  void default_should_exclude_only_files_without_identifier() {
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, CloudformationSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertTrue(filter.accept(inputFile("file.json", "\nSomeValue\n\"AWSTemplateFormatVersion\"")));
    assertFalse(filter.accept(inputFile("file.json", "")));
    assertFalse(filter.accept(inputFile("file.json", "DifferentIdentifier")));
  }

  @Test
  void should_exclude_nothing_with_empty_identifier() {
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertTrue(filter.accept(inputFile("file.json", "")));
    assertTrue(filter.accept(inputFile("file.json", "DifferentIdentifier")));
  }

  @Test
  void should_exclude_using_custom_identifier_regex() {
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, "DifferentIdentifier");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertFalse(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertFalse(filter.accept(inputFile("file.json", "")));
    assertTrue(filter.accept(inputFile("file.json", "DifferentIdentifier")));
  }

  @Test
  void should_exclude_identifier_case_sensitive() {
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, CloudformationSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json", "AWSTemplateFormatVersion")));
    assertFalse(filter.accept(inputFile("file.json", "awstemplateformatversion")));
  }

  @Test
  void test_invalid_file() throws IOException {
    InputFile inputFile = inputFile("fakeFile.json", "");
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.inputStream()).thenThrow(IOException.class);
    settings.setProperty(CloudformationSettings.FILE_IDENTIFIER_KEY, CloudformationSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertFalse(filter.accept(spyInputFile));

    assertThat(logTester.logs()).contains(String.format("Unable to read file: %s.", inputFile.uri()));
  }

  private InputFile inputFile(String filename) {
    return inputFile(filename, CloudformationSettings.FILE_IDENTIFIER_DEFAULT_VALUE);
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
    return EXTENSIONS.contains(extension) ? "cloudformation" : extension;
  }
}
