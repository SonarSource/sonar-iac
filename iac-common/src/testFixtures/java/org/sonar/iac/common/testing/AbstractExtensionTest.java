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
package org.sonar.iac.common.testing;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractExtensionTest {
  protected abstract SonarRuntime sonarQubeRuntime();

  protected abstract SonarRuntime sonarLintRuntime();

  protected abstract Consumer<Plugin.Context> extensionDefiner();

  protected abstract int extensionsCountOnSQ();

  protected int extensionsCountOnSL() {
    return extensionsCountOnSQ();
  }

  @Test
  void shouldDefineExtensionsOnSonarqube() {
    var runtime = sonarQubeRuntime();
    var context = new Plugin.Context(runtime);

    extensionDefiner().accept(context);

    assertThat(context.getExtensions())
      .hasSize(extensionsCountOnSQ());
  }

  @Test
  void shouldDefineExtensionsOnSonarlint() {
    var runtime = sonarLintRuntime();
    var context = new Plugin.Context(runtime);

    extensionDefiner().accept(context);

    assertThat(context.getExtensions())
      .hasSize(extensionsCountOnSL());
  }
}
