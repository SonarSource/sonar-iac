/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import org.junit.jupiter.api.Test;

class ClearTextProtocolsCheckTest {

  @Test
  void test_msk_yaml() {
    CloudformationVerifier.verify("ClearTextProtocolsCheck/test_msk.yaml", new ClearTextProtocolsCheck());
  }

  @Test
  void test_search_domain_yaml() {
    CloudformationVerifier.verify("ClearTextProtocolsCheck/test_search_domain.yaml", new ClearTextProtocolsCheck());
  }

  @Test
  void test_load_balancer_listeners() {
    CloudformationVerifier.verify("ClearTextProtocolsCheck/test_load_balancers.yaml", new ClearTextProtocolsCheck());
  }

  @Test
  void test_ecs_tasks() {
    CloudformationVerifier.verify("ClearTextProtocolsCheck/test_ecs_tasks.yaml", new ClearTextProtocolsCheck());
  }

  @Test
  void test_es_replication_group() {
    CloudformationVerifier.verify("ClearTextProtocolsCheck/test_es_replication_group.yaml", new ClearTextProtocolsCheck());
  }

  @Test
  void test_kinesis_stream() {
    CloudformationVerifier.verify("ClearTextProtocolsCheck/test_kinesis_stream.yaml", new ClearTextProtocolsCheck());
  }
}
