package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationExtensionTest {

  @Test
  void test_extension_count() {
    assertThat(CloudformationExtension.getExtensions().size()).isEqualTo(8);
  }

}
