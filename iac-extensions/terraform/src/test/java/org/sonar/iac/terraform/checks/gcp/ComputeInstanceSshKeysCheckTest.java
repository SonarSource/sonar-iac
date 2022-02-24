package org.sonar.iac.terraform.checks.gcp;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.checks.TerraformVerifier;

public class ComputeInstanceSshKeysCheckTest {

  @Test
  void ssh_keys() {
    TerraformVerifier.verify("GCP/ComputeInstanceSshKeysCheck/ssh_keys.tf", new ComputeInstanceSshKeysCheck());
  }

  @Test
  void ssh_keys_by_template() {
    TerraformVerifier.verify("GCP/ComputeInstanceSshKeysCheck/ssh_keys_by_template.tf", new ComputeInstanceSshKeysCheck());
  }

}
