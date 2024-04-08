/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.TemplateFileReader.readContent;
import static org.sonar.iac.common.testing.Verifier.issue;

class ParameterAndVariableNameConventionCheckTest {

  @Test
  void shouldVerifyBicep() {
    BicepVerifier.verify("ParameterAndVariableNameConvention/nameConvention.bicep", new ParameterAndVariableNameConventionCheck());
  }

  @Test
  void shouldVerifyBicepCustomPattern() {
    var check = new ParameterAndVariableNameConventionCheck();
    check.format = "^[a-z][a-z0-9_]*$";
    String content = readContent("ParameterAndVariableNameConvention/nameConvention.bicep");
    BicepVerifier.verifyContent(content, check,
      issue(5, 6, 5, 24, "Rename this parameter \"StorageAccountName\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(7, 6, 7, 24, "Rename this parameter \"storageAccountName\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(13, 4, 13, 18, "Rename this variable \"StringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(15, 4, 15, 18, "Rename this variable \"stringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."));
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("ParameterAndVariableNameConvention/nameConvention.json", new ParameterAndVariableNameConventionCheck(),
      issue(5, 4, 5, 26, "Rename this parameter \"storage_account_name\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(8, 4, 8, 24, "Rename this parameter \"StorageAccountName\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(16, 4, 16, 21, "Rename this variable \"string_variable\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(17, 4, 17, 20, "Rename this variable \"StringVariable\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."));
  }

  @Test
  void shouldVerifyJsonCustomPattern() {
    var check = new ParameterAndVariableNameConventionCheck();
    check.format = "^[a-z][a-z0-9_]*$";
    ArmVerifier.verify("ParameterAndVariableNameConvention/nameConvention.json", check,
      issue(8, 4, 8, 24, "Rename this parameter \"StorageAccountName\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(11, 4, 11, 24, "Rename this parameter \"storageAccountName\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(17, 4, 17, 20, "Rename this variable \"StringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(18, 4, 18, 20, "Rename this variable \"stringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."));
  }
}
