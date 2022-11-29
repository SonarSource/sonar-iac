package org.sonar.iac.common.checks;


import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.checks.TextUtilsTest.TestTextTree.text;

class ResourceAccessPolicyVectorTest {

  @Test
  void isResourceAccessPolicy() {
    Tree action = text("backup-gateway:Backup");
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(action)).isTrue();
  }

  @Test
  void isNotResourceAccessPolicy() {
    Tree action = text("foo:bar");
    assertThat(ResourceAccessPolicyVector.isResourceAccessPolicy(action)).isFalse();
  }

  @Test
  void isNotTextTree() {

  }
}
