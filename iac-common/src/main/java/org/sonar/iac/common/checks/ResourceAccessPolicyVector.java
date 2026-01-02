/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.iac.common.api.tree.Tree;

public final class ResourceAccessPolicyVector {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceAccessPolicyVector.class);
  private static final String VECTOR_FILE = "ResourceAccessPolicyVector.json";

  private ResourceAccessPolicyVector() {
  }

  private static final List<String> RESOURCE_ACCESS_POLICIES = loadResourceAccessPolicies(VECTOR_FILE);

  static List<String> loadResourceAccessPolicies(String filePath) {
    try {
      JsonValue value = Json.parse(loadJsonFile(filePath));
      return value.asArray().values().stream().map(JsonValue::asString).toList();
    } catch (IOException e) {
      LOG.warn(e.getMessage());
    }
    return Collections.emptyList();
  }

  static String loadJsonFile(String filePath) throws IOException {
    try (InputStream input = ResourceAccessPolicyVector.class.getClassLoader().getResourceAsStream(filePath)) {
      if (input == null) {
        throw new IOException("Unable to load " + filePath);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4_096];
      for (int read = input.read(buffer); read != -1; read = input.read(buffer)) {
        out.write(buffer, 0, read);
      }
      return out.toString(StandardCharsets.UTF_8);
    }
  }

  public static boolean isResourceAccessPolicy(Tree action) {
    return TextUtils.matchesValue(action, RESOURCE_ACCESS_POLICIES::contains).isTrue();
  }
}
