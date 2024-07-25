package org.sonar.iac.docker.checks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetrieveRemoteResourcesCheckTest {

  RetrieveRemoteResourcesCheck check = new RetrieveRemoteResourcesCheck();

  @Test
  void shouldVerifyWget() {
    DockerVerifier.verify("RetrieveRemoteResourcesCheck/wget.dockerfile", check);
  }
}
