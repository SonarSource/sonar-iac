/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.arm.tree.impl.json;

import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;

import static org.sonar.iac.common.testing.IacTestUtils.code;

public class PropertyTestUtils {

  public static final int LINE_OFFSET = 8;

  public static Property parseProperty(ArmParser parser, String property) {
    String code = getCode(property);
    File tree = (File) parser.parse(code, null);
    return ((ResourceDeclaration) tree.statements().get(0)).properties().get(0);
  }

  private static String getCode(String property) {
    return code("{",
      "  \"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\",",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"Microsoft.Kusto/clusters\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": \"myResource\",",
      "      \"properties\": {",
      property,
      "      }",
      "    }",
      "  ]",
      "}");
  }
}
