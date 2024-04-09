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
      issue(9, 6, 9, 14, "Rename this parameter \"demo_Int\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(11, 6, 11, 13, "Rename this parameter \"demoInt\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(13, 6, 13, 16, "Rename this parameter \"_demo_bool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(15, 6, 15, 14, "Rename this parameter \"demoBool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(17, 6, 17, 17, "Rename this parameter \"demo_Object\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(19, 6, 19, 16, "Rename this parameter \"demoObject\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(21, 6, 21, 15, "Rename this parameter \"DemoArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(23, 6, 23, 15, "Rename this parameter \"demoArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(26, 6, 26, 19, "Rename this parameter \"AdminPassword\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(37, 4, 37, 18, "Rename this variable \"StringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(39, 4, 39, 18, "Rename this variable \"stringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(42, 6, 42, 15, "Rename this parameter \"itemCount\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(44, 6, 44, 17, "Rename this parameter \"ExampleBool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(45, 6, 45, 17, "Rename this parameter \"exampleBool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(48, 6, 48, 19, "Rename this parameter \"exampleObject\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(50, 4, 50, 16, "Rename this variable \"IntegerArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(51, 4, 51, 16, "Rename this variable \"integerArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."));
  }

  @Test
  void shouldVerifyJson() {
    ArmVerifier.verify("ParameterAndVariableNameConvention/nameConvention.json", new ParameterAndVariableNameConventionCheck(),
      issue(5, 4, 5, 26, "Rename this parameter \"storage_account_name\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(8, 4, 8, 24, "Rename this parameter \"StorageAccountName\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(14, 4, 14, 14, "Rename this parameter \"demo_Int\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(20, 4, 20, 16, "Rename this parameter \"_demo_bool\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(26, 4, 26, 17, "Rename this parameter \"demo_Object\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(32, 4, 32, 15, "Rename this parameter \"DemoArray\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(38, 4, 38, 19, "Rename this parameter \"AdminPassword\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(46, 4, 46, 21, "Rename this variable \"string_variable\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(47, 4, 47, 20, "Rename this variable \"StringVariable\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(49, 4, 49, 16, "Rename this variable \"item_count\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(51, 4, 51, 17, "Rename this variable \"ExampleBool\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(53, 4, 53, 20, "Rename this variable \"example_object\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."),
      issue(55, 4, 55, 18, "Rename this variable \"IntegerArray\" to match the regular expression '^[a-z][a-zA-Z0-9]*$'."));
  }

  @Test
  void shouldVerifyJsonCustomPattern() {
    var check = new ParameterAndVariableNameConventionCheck();
    check.format = "^[a-z][a-z0-9_]*$";
    ArmVerifier.verify("ParameterAndVariableNameConvention/nameConvention.json", check,
      issue(8, 4, 8, 24, "Rename this parameter \"StorageAccountName\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(11, 4, 11, 24, "Rename this parameter \"storageAccountName\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(14, 4, 14, 14, "Rename this parameter \"demo_Int\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(17, 4, 17, 13, "Rename this parameter \"demoInt\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(20, 4, 20, 16, "Rename this parameter \"_demo_bool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(23, 4, 23, 14, "Rename this parameter \"demoBool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(26, 4, 26, 17, "Rename this parameter \"demo_Object\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(29, 4, 29, 16, "Rename this parameter \"demoObject\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(32, 4, 32, 15, "Rename this parameter \"DemoArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(35, 4, 35, 15, "Rename this parameter \"demoArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(38, 4, 38, 19, "Rename this parameter \"AdminPassword\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(47, 4, 47, 20, "Rename this variable \"StringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(48, 4, 48, 20, "Rename this variable \"stringVariable\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(50, 4, 50, 15, "Rename this variable \"itemCount\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(51, 4, 51, 17, "Rename this variable \"ExampleBool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(52, 4, 52, 17, "Rename this variable \"exampleBool\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(54, 4, 54, 19, "Rename this variable \"exampleObject\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(55, 4, 55, 18, "Rename this variable \"IntegerArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."),
      issue(56, 4, 56, 18, "Rename this variable \"integerArray\" to match the regular expression '^[a-z][a-z0-9_]*$'."));
  }
}
