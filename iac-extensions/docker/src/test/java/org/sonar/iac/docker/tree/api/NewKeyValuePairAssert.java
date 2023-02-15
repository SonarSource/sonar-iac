/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.docker.utils.ArgumentUtils;

public class NewKeyValuePairAssert extends DockerTreeAssert<NewKeyValuePairAssert, NewKeyValuePair> {

  private NewKeyValuePairAssert(NewKeyValuePair newKeyValuePair) {
    super(newKeyValuePair, NewKeyValuePairAssert.class);
  }

  public static NewKeyValuePairAssert assertThat(NewKeyValuePair actual) {
    return new NewKeyValuePairAssert(actual);
  }

  public NewKeyValuePairAssert hasKey(String key) {
    isNotNull();
    String value = ArgumentUtils.resolve(actual.key()).value();
    Assertions.assertThat(value)
      .overridingErrorMessage("Expected NewKeyValuePair key to be <%s> but was <%s>", key, value)
      .isEqualTo(key);
    return this;
  }

  public NewKeyValuePairAssert hasEqualSignNull() {
    isNotNull();
    SyntaxToken syntaxToken = actual.equalSign();
    Assertions.assertThat(syntaxToken)
      .overridingErrorMessage("Expected NewKeyValuePair equal sign to be NULL but was <%s>", syntaxToken)
      .isNull();
    return this;
  }

  public NewKeyValuePairAssert hasEqualSign(String expectedEqualSign) {
    isNotNull();
    SyntaxToken syntaxToken = actual.equalSign();
    Assertions.assertThat(syntaxToken.value())
      .overridingErrorMessage("Expected NewKeyValuePair equal sign to be NULL but was <%s>", syntaxToken)
      .isEqualTo(expectedEqualSign);
    return this;
  }

  public NewKeyValuePairAssert hasValue(String expectedValue) {
    isNotNull();
    String value = ArgumentUtils.resolve(actual.value()).value();
    Assertions.assertThat(value)
      .overridingErrorMessage("Expected NewKeyValuePair value to be <%s> but was <%s>", expectedValue, value)
      .isEqualTo(expectedValue);
    return this;
  }

  public NewKeyValuePairAssert hasValueNull() {
    isNotNull();
    Assertions.assertThat(actual.value())
      .overridingErrorMessage("Expected NewKeyValuePair value to be NULL but was <%s>", actual.value())
      .isNull();
    return this;
  }
}
