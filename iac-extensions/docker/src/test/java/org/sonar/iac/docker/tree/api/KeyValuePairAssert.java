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
package org.sonar.iac.docker.tree.api;

import org.assertj.core.api.Assertions;
import org.sonar.iac.docker.symbols.ArgumentResolution;

public class KeyValuePairAssert extends DockerTreeAssert<KeyValuePairAssert, KeyValuePair> {

  private KeyValuePairAssert(KeyValuePair keyValuePair) {
    super(keyValuePair, KeyValuePairAssert.class);
  }

  public static KeyValuePairAssert assertThat(KeyValuePair actual) {
    return new KeyValuePairAssert(actual);
  }

  public KeyValuePairAssert hasKey(String key) {
    isNotNull();
    String value = ArgumentResolution.of(actual.key()).value();
    Assertions.assertThat(value)
      .overridingErrorMessage("Expected KeyValuePair key to be <%s> but was <%s>", key, value)
      .isEqualTo(key);
    return this;
  }

  public KeyValuePairAssert hasEqualSignNull() {
    isNotNull();
    SyntaxToken syntaxToken = actual.equalSign();
    Assertions.assertThat(syntaxToken)
      .overridingErrorMessage("Expected KeyValuePair equal sign to be NULL but was <%s>", syntaxToken)
      .isNull();
    return this;
  }

  public KeyValuePairAssert hasEqualSign(String expectedEqualSign) {
    isNotNull();
    SyntaxToken syntaxToken = actual.equalSign();
    Assertions.assertThat(syntaxToken.value())
      .overridingErrorMessage("Expected KeyValuePair equal sign to be NULL but was <%s>", syntaxToken)
      .isEqualTo(expectedEqualSign);
    return this;
  }

  public KeyValuePairAssert hasValue(String expectedValue) {
    isNotNull();
    String value = ArgumentResolution.of(actual.value()).value();
    Assertions.assertThat(value)
      .overridingErrorMessage("Expected KeyValuePair value to be <%s> but was <%s>", expectedValue, value)
      .isEqualTo(expectedValue);
    return this;
  }

  public KeyValuePairAssert hasValueNull() {
    isNotNull();
    Assertions.assertThat(actual.value())
      .overridingErrorMessage("Expected KeyValuePair value to be NULL but was <%s>", actual.value())
      .isNull();
    return this;
  }
}
