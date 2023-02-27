package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.checks.ToDoCommentCheck;

public class DockerToDoCommentCheckTest {
  
  @Test
  void test() {
    DockerVerifier.verify("ToDoCommentCheck/Dockerfile", new ToDoCommentCheck());
  }
}
