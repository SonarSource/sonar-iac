/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.terraform.checks;

import org.junit.jupiter.api.Test;

class ClearTextProtocolsCheckTest {

  @Test
  void test_msk() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test_msk.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void test_elasticsearch_domain() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test_elasticsearch_domain.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void test_lb_listener() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test_lb_listener.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void test_elasticsearch_replication_group() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test_elasticsearch_replication_group.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void test_ecs_task_definition() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test_ecs_task_definition.tf", new ClearTextProtocolsCheck());
  }

  @Test
  void test_kinesis_stream() {
    TerraformVerifier.verify("ClearTextProtocolsCheck/test_kinesis_stream.tf", new ClearTextProtocolsCheck());
  }
}
