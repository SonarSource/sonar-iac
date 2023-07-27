package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.Verifier;

class SecureValuesExposureCheckTest {
  @Test
  void testJson() {
    ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_compliant_secure_scope.json", new SecureValuesExposureCheck());
    ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_compliant_no_top_level_parameter.json", new SecureValuesExposureCheck());
    ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_noncompliant.json", new SecureValuesExposureCheck(),
      Verifier.issue(12, 14, 12, 47, "Change this code to not use an outer expression evaluation scope in nested templates."));
  }

  @Test
  void testBicep() {
    // ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_compliant_secure_scope.json", new
    // SecureValuesExposureCheck());
    // ArmVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_compliant_no_top_level_parameter.json", new
    // SecureValuesExposureCheck());
    BicepVerifier.verify("SecureValuesExposureCheck/Microsoft.Resources_deployments_noncompliant.bicep", new SecureValuesExposureCheck());
  }
}
