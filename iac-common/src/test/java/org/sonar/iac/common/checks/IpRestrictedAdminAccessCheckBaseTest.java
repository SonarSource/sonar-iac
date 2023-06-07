package org.sonar.iac.common.checks;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.policy.IpRestrictedAdminAccessCheckBase.rangeContainsSshOrRdpPort;

class IpRestrictedAdminAccessCheckBaseTest {

  @Test
  void testSimplePort() {
    assertThat(rangeContainsSshOrRdpPort("*")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("22")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("3389")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("80")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("222")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("other")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("-22")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("**")).isFalse();
  }

  @Test
  void testPortRange() {
    assertThat(rangeContainsSshOrRdpPort("10-30")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("22-22")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("10-4000")).isTrue();
    assertThat(rangeContainsSshOrRdpPort("5-20")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("25-300")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("3400-3500")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("3400-string")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("10-22-50")).isFalse();
    assertThat(rangeContainsSshOrRdpPort("*-22")).isFalse();
  }
}
