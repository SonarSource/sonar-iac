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
package org.sonar.iac.arm;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;

import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.IacTestUtils.code;

public class ArmTestUtils {

  private static final ArmParser PARSER = new ArmParser();

  public static final CheckContext CTX = mock(CheckContext.class);

  public static ResourceDeclaration parseResource(String code) {
    String wrappedCode = code("{",
      "  \"resources\": [",
      code,
      "  ]",
      "}");
    File file = (File) PARSER.parse(wrappedCode, null);
    return (ResourceDeclaration) file.statements().get(0);
  }

  public static ParameterDeclaration parseParameter(String parameterName, String type, @Nullable String defaultValue) {
    String wrappedParameterCode = "\"" + parameterName + "\": {" +
      "  \"type\": \"" + type + "\"" +
      (defaultValue != null ? ", \"defaultValue\": \"" + defaultValue + "\"" : "") +
      "}";
    File file = (File) PARSER.parse("{ \"parameters\": {" + wrappedParameterCode + "}}", null);
    return (ParameterDeclaration) file.statements().get(0);
  }

  public static Property parseProperty(String propertyCode) {
    String wrappedPropertyCode = code("{",
      "    \"name\": \"dummy resource\",",
      "    \"type\": \"resource type\",",
      "    \"apiVersion\": \"version\",",
      "    \"properties\": {",
      propertyCode,
      "    }",
      "}");
    ResourceDeclaration resourceDeclaration = parseResource(wrappedPropertyCode);
    return resourceDeclaration.properties().get(0);
  }

  public static ObjectExpression parseObject(String objectCode) {
    String wrappedPropertyCode = code("{",
      "    \"name\": \"dummy resource\",",
      "    \"type\": \"resource type\",",
      "    \"apiVersion\": \"version\",",
      "    \"properties\": {",
      "      \"object\": " + objectCode,
      "    }",
      "}");
    ResourceDeclaration resourceDeclaration = parseResource(wrappedPropertyCode);
    return (ObjectExpression) resourceDeclaration.properties().get(0).value();
  }

  public static List<String> recursiveTransformationOfTreeChildrenToStrings(Tree tree) {
    return recursiveTransformationOfTreeChildrenToStrings(tree, 10).collect(Collectors.toList());
  }

  public static Stream<String> recursiveTransformationOfTreeChildrenToStrings(Tree tree, int maxDepth) {
    if (maxDepth == 0) {
      return Stream.empty();
    }
    return tree.children().stream().flatMap(t -> {
      if (t instanceof TextTree) {
        return Stream.of(((TextTree) t).value());
      } else {
        return recursiveTransformationOfTreeChildrenToStrings(t, maxDepth - 1);
      }
    });
  }
}
