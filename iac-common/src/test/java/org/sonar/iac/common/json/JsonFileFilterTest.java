/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
    assertThat(filter.accept(inputFile("build_wrapper_output_directory/compile_commands.json", "json"))).isFalse();
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
