package org.sonar.iac.terraform.checks.gcp;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.checks.TerraformVerifier;

public class ComputeInstanceSshKeysCheckTest {

  @Test
  void dns_zone() {
    TerraformVerifier.verify("GCP/ComputeInstanceSshKeysCheck/ssh_keys.tf", new ComputeInstanceSshKeysCheck());
  }
}
