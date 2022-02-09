package org.sonar.iac.terraform.checks.gcp;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.checks.gcp.IamResourcesCheckPart;

@Rule(key = "S6400")
public class AdministrativeRolesOnWorkloadResourcesCheck implements IacCheck {
  @Override
  public void initialize(InitContext init) {
    new IamResourcesCheckPart().initialize(init);
  }
}
