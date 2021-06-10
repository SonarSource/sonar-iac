/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.checks;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AwsTagNameConventionCheckTest {

  @Test
  void test_default_yaml() {
    CloudformationVerifier.verify("AwsTagNameConventionCheck/default.yaml", new AwsTagNameConventionCheck());
  }

  @Test
  void test_custom() {
    AwsTagNameConventionCheck check = new AwsTagNameConventionCheck();
    check.format = "^([a-z-]*[a-z]:)*([a-z-]*[a-z])$";
    CloudformationVerifier.verify("AwsTagNameConventionCheck/custom.yaml", check);
  }

  @Test
  @Disabled("Verifier for files without comments has to be implemented")
  void test_default_json() {
    // TODO: verifier for files without comments
    CloudformationVerifier.verify("AwsTagNameConventionCheck/default.json", new AwsTagNameConventionCheck());
  }
}
