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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.api.tree.Tree;

public final class ResourceAccessPolicyVector {

  private static final Logger LOG = Loggers.get(ResourceAccessPolicyVector.class);
  private static final String VECTOR_FILE = "ResourceAccessPolicyVector.json";

  private ResourceAccessPolicyVector() {
  }

  private static final List<String> RESOURCE_ACCESS_POLICIES = loadResourceAccessPolicies();

  private static List<String> loadResourceAccessPolicies() {
    try {
      String resourceAccessPolicies = loadJsonFile(VECTOR_FILE);
      JSONParser parser = new JSONParser();
      return  (JSONArray) parser.parse(resourceAccessPolicies);
    } catch (IOException | ParseException e) {
      LOG.error(e.getMessage());
    }
    return Collections.emptyList();
  }

  static String loadJsonFile(String filePath) throws IOException {
    try (InputStream input = ResourceAccessPolicyVector.class.getClassLoader().getResourceAsStream(filePath)) {
      if (input == null) {
        throw new IOException("No able to load " + filePath);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4_096];
      for (int read = input.read(buffer); read != -1; read = input.read(buffer)) {
        out.write(buffer, 0, read);
      }
      return  out.toString(StandardCharsets.UTF_8);
    }
  }

  public static boolean isResourceAccessPolicy(Tree action) {
    return TextUtils.matchesValue(action, RESOURCE_ACCESS_POLICIES::contains).isTrue();
  }
}
