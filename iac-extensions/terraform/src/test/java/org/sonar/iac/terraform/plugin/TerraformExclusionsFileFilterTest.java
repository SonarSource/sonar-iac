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
package org.sonar.iac.terraform.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.config.internal.MapSettings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerraformExclusionsFileFilterTest {

  @Test
  void default_should_exclude_nothing() {
    MapSettings settings = new MapSettings();
    settings.setProperty(TerraformExtension.EXCLUSIONS_KEY, TerraformExtension.EXCLUSIONS_DEFAULT_VALUE);
    InputFileFilter filter = new TerraformExclusionsFileFilter(settings.asConfig());
    assertTrue(filter.accept(inputFile("file.tf")));
    assertTrue(filter.accept(inputFile("vendor/file.tf")));
    assertTrue(filter.accept(inputFile("vendor/someDir/file.tf")));
    assertTrue(filter.accept(inputFile("someDir/vendor/file.tf")));
  }

  @Test
  void should_exclude_using_custom_path_regex() {
    MapSettings settings = new MapSettings();

    settings.setProperty(TerraformExtension.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new TerraformExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.tf")));
    assertTrue(filter.accept(inputFile("someDir/file.tf")));
    assertFalse(filter.accept(inputFile("path/file.tf")));
    assertFalse(filter.accept(inputFile("someDir/path/file.tf")));
  }

  @Test
  void should_exclude_other_language() {
    MapSettings settings = new MapSettings();

    settings.setProperty(TerraformExtension.EXCLUSIONS_KEY, "**/path/**");
    InputFileFilter filter = new TerraformExclusionsFileFilter(settings.asConfig());

    assertFalse(filter.accept(inputFile("path/file.tf")));
    assertTrue(filter.accept(inputFile("path/file.go")));
  }

  @Test
  void should_ignore_empty_path_regex() {
    MapSettings settings = new MapSettings();
    settings.setProperty(TerraformExtension.EXCLUSIONS_KEY, "," + TerraformExtension.EXCLUSIONS_DEFAULT_VALUE + ",");
    InputFileFilter filter = new TerraformExclusionsFileFilter(settings.asConfig());

    assertTrue(filter.accept(inputFile("file.tf")));
  }

  private DefaultInputFile inputFile(String file) {
    String extension = file.split("\\.")[1];
    String language = "tf".equals(extension) ? "terraform" : extension;
    return new TestInputFileBuilder("test","test_path/" + file).setLanguage(language).build();
  }
}
