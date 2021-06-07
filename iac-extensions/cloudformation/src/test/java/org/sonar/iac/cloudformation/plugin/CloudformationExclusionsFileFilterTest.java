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

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.internal.MapSettings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CloudformationExclusionsFileFilterTest {

  // TODO add test cases for yaml

  @Test
  void default_should_exclude_nothing() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, CloudformationExtension.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.json")));
    assertTrue(filter.accept(inputFile("vendor/file.json")));
    assertTrue(filter.accept(inputFile("vendor/someDir/file.json")));
    assertTrue(filter.accept(inputFile("someDir/vendor/file.json")));
  }

  @Test
  void should_exclude_using_custom_path_regex() {
    MapSettings settings = new MapSettings();

    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.json")));
    assertTrue(filter.accept(inputFile("someDir/file.json")));
    assertFalse(filter.accept(inputFile("path/file.json")));
    assertFalse(filter.accept(inputFile("someDir/path/file.json")));
  }

  @Test
  void should_exclude_other_language() {
    MapSettings settings = new MapSettings();

    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertFalse(filter.accept(inputFile("path/file.json")));
    assertTrue(filter.accept(inputFile("path/file.go")));
  }

  @Test
  void should_ignore_empty_path_regex() {
    MapSettings settings = new MapSettings();
    settings.setProperty(CloudformationExtension.EXCLUSIONS_KEY, "," + CloudformationExtension.EXCLUSIONS_DEFAULT_VALUE + ",");
    InputFileFilter filter = new CloudformationExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.json")));
  }

  private DefaultInputFile inputFile(String file) {
    String extension = file.split("\\.")[1];
    String language = "json".equals(extension) ? "cloudformation" : extension;
    return new TestInputFileBuilder("test","test_path/" + file).setLanguage(language).build();
  }
}
