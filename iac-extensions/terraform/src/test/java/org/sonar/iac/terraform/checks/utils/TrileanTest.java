/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrileanTest {

  @Test
  void isTrue() {
    assertThat(Trilean.TRUE.isTrue()).isTrue();
    assertThat(Trilean.FALSE.isTrue()).isFalse();
    assertThat(Trilean.UNKNOWN.isTrue()).isFalse();
  }

  @Test
  void isFalse() {
    assertThat(Trilean.TRUE.isFalse()).isFalse();
    assertThat(Trilean.FALSE.isFalse()).isTrue();
    assertThat(Trilean.UNKNOWN.isFalse()).isFalse();
  }
}
