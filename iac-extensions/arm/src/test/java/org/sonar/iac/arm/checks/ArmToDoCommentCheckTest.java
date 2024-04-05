package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.checks.ToDoCommentCheck;

class ArmToDoCommentCheckTest {

  @Test
  void testToDoComments() {
    BicepVerifier.verify("ToDoCommentCheck/TodoCheck.bicep", new ToDoCommentCheck());
  }
}
