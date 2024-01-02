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
package org.sonar.iac.common.json;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class JsonFileFilterTest {

  @Test
  void buildWrapperFileShouldBeExcluded() {
    JsonFileFilter filter = new JsonFileFilter();
    assertThat(filter.accept(inputFile("build_wrapper_output_directory/build-wrapper-dump.json", "json"))).isFalse();
    assertThat(filter.accept(inputFile("build_wrapper_output_directory/foo.json", "json"))).isTrue();
    assertThat(filter.accept(inputFile("foo.php", "php"))).isTrue();
  }

  private DefaultInputFile inputFile(String file, String lang) {
    return new TestInputFileBuilder("test", file)
      .setCharset(StandardCharsets.UTF_8)
      .setLanguage(lang)
      .setContents("foo")
      .build();
  }
}
