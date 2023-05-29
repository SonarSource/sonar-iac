package org.sonar.iac.arm.tree.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterTypeTest {

  @Test
  void shouldReturnArray() {
    ParameterType parameterType = ParameterType.fromName("array");
    assertThat(parameterType).isEqualTo(ParameterType.ARRAY);
  }

  @Test
  void shouldReturnNull() {
    ParameterType parameterType = ParameterType.fromName("unknown");
    assertThat(parameterType).isNull();
  }
}
