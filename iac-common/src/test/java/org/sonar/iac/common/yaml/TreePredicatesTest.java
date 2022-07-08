package org.sonar.iac.common.yaml;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;

class TreePredicatesTest {

  @Test
  void isTrue() {
    assertThat(TreePredicates.isTrue().test(text("true"))).isTrue();
  }

  private ScalarTree text(String value) {
    return new ScalarTreeImpl(value, null, null);
  }
}
