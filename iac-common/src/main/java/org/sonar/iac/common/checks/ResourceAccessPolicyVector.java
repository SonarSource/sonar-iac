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
package org.sonar.iac.common.checks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.api.tree.Tree;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public final class ResourceAccessPolicyVector {

  private static final Logger LOG = Loggers.get(ResourceAccessPolicyVector.class);
  private static final String VECTOR_FILE = "ResourceAccessPolicyVector.json";

  private ResourceAccessPolicyVector() {
  }

  private static final List<String> RESOURCE_ACCESS_POLICIES = loadResourceAccessPolicies(VECTOR_FILE);

  static List<String> loadResourceAccessPolicies(String filePath) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      URL resource = ResourceAccessPolicyVector.class.getClassLoader().getResource(filePath);
      if (resource == null) {
        throw new IOException("No able to load " + filePath);
      }
      return objectMapper.readValue(resource, new TypeReference<>() {
      });
    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
    return Collections.emptyList();
  }

  public static boolean isResourceAccessPolicy(Tree action) {
    return TextUtils.matchesValue(action, RESOURCE_ACCESS_POLICIES::contains).isTrue();
  }
}
