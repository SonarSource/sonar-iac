/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.yaml.object;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

public class YamlObjectAssertions {

  public static YamlObjectAssert assertThat(YamlObject yamlObject) {
    return new YamlObjectAssert(yamlObject);
  }

  public static class YamlObjectAssert extends ObjectAssert<YamlObject> {

    public YamlObjectAssert(YamlObject actual) {
      super(actual);
    }

    public YamlObjectAssert isPresent() {
      Assertions.assertThat(actual.isPresent()).isTrue();
      return this;
    }

    public YamlObjectAssert isAbsent() {
      Assertions.assertThat(actual.isAbsent()).isTrue();
      return this;
    }

    public YamlObjectAssert isUnknown() {
      Assertions.assertThat(actual.status).isEqualTo(YamlObject.Status.UNKNOWN);
      return this;
    }
  }
}
