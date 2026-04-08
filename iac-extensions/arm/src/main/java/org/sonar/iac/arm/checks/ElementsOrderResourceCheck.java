/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checks.elementsorder.ElementsOrderResourceCheckBicep;
import org.sonar.iac.arm.checks.elementsorder.ElementsOrderResourceCheckJson;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

@Rule(key = "S6975")
public class ElementsOrderResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    new ElementsOrderResourceCheckJson().initialize(init);
    new ElementsOrderResourceCheckBicep().initialize(init);
  }
}
