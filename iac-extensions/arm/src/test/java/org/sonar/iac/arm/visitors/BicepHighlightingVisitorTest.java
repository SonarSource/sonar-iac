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
package org.sonar.iac.arm.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.common.testing.AbstractHighlightingTest;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.ANNOTATION;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class BicepHighlightingVisitorTest extends AbstractHighlightingTest {

  protected BicepHighlightingVisitorTest() {
    super(new BicepHighlightingVisitor(), BicepParser.create());
  }

  @Test
  void emptyInput() {
    highlight("");
    assertHighlighting(1, 0, 0, null);
  }

  @Test
  void singleLineComment() {
    highlight("  // Comment ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 12, COMMENT);
  }

  @Test
  void testComment() {
    highlight("  /*Comment*/ ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 12, COMMENT);
    assertHighlighting(13, 13, null);
  }

  @Test
  void testMultilineComment() {
    highlight("/*\nComment\n*/ ");
    assertHighlighting(1, 0, 1, COMMENT);
    assertHighlighting(2, 0, 6, COMMENT);
    assertHighlighting(3, 0, 1, COMMENT);
    assertHighlighting(3, 2, 2, null);
  }

  @Test
  void testDecoratorAndParam() {
    highlight(code(
      "@description('The list of logic app')",
      "param logicAppReceivers array = []"));
    assertHighlighting("@description", ANNOTATION);
    assertHighlighting("'The list of logic app'", STRING);

    assertHighlighting(2, "param", KEYWORD);
    assertHighlighting(2, "logicAppReceivers", KEYWORD_LIGHT);
    assertHighlighting(2, "array", KEYWORD);
    assertHighlighting(2, "= []", null);
  }

  @Test
  void testResourceDeclaration() {
    highlight(code(
      "resource actionGroup 'Microsoft.Insights/actionGroups@2019-06-01' = {",
      "  name: actionGroupName",
      "  location: 'Global'",
      "  properties: {",
      "    groupShortName: actionGroupShortName",
      "    enabled: true",
      "  }",
      "}"));
    assertHighlighting(1, "resource", KEYWORD);
    assertHighlighting(1, "actionGroup", KEYWORD_LIGHT);
    assertHighlighting(1, "'Microsoft.Insights/actionGroups@2019-06-01'", STRING);

    assertHighlighting(2, "name", ANNOTATION);
    assertHighlighting(2, "actionGroupName", null);

    assertHighlighting(3, "location", ANNOTATION);
    assertHighlighting(3, "'Global'", STRING);

    assertHighlighting(4, "properties", ANNOTATION);

    assertHighlighting(5, "groupShortName", ANNOTATION);
    assertHighlighting(5, "actionGroupShortName", null);

    assertHighlighting(6, "enabled", ANNOTATION);
    assertHighlighting(6, "true", CONSTANT);
  }

  @Test
  void testResourceDeclarationIfCondition() {
    highlight(code(
      "resource myName1 'type@version' = if (!empty(logAnalytics)) {",
      "  key: value",
      "  properties: {",
      "    prop1: val1",
      "  }",
      "}"));
    assertHighlighting(1, "resource", KEYWORD);
    assertHighlighting(1, "myName1", KEYWORD_LIGHT);
    assertHighlighting(1, "'type@version'", STRING);
    assertHighlighting(1, "if", KEYWORD);
    assertHighlighting(1, "(!empty(logAnalytics)) {", null);

    assertHighlighting(2, "key", ANNOTATION);
    assertHighlighting(2, "value", null);

    assertHighlighting(3, "properties", ANNOTATION);

    assertHighlighting(4, "prop1", ANNOTATION);
    assertHighlighting(4, "val1", null);
  }

  @Test
  void testResourceDeclarationForExpression() {
    highlight(code(
      "resource myName2 'type@version' = [for item in collection: {",
      "  key: value",
      "  properties: {",
      "    prop1: val1",
      "  }",
      "}",
      "]"));
    assertHighlighting(1, "resource", KEYWORD);
    assertHighlighting(1, "myName2", KEYWORD_LIGHT);
    assertHighlighting(1, "'type@version'", STRING);
    assertHighlighting(1, "for", KEYWORD);
    assertHighlighting(1, "in", KEYWORD);

    assertHighlighting(2, "key", ANNOTATION);
    assertHighlighting(2, "value", null);

    assertHighlighting(3, "properties", ANNOTATION);

    assertHighlighting(4, "prop1", ANNOTATION);
    assertHighlighting(4, "val1", null);
  }

  @Test
  void testTargetScope() {
    highlight(code("targetScope = 123"));
    assertHighlighting(1, "targetScope", KEYWORD);
    assertHighlighting(1, "123", CONSTANT);
  }

  @Test
  void testImportDeclaration() {
    highlight("import 'foo' with {} as bar");
    assertHighlighting("import", KEYWORD);
    assertHighlighting("'foo'", STRING);
    assertHighlighting("with", KEYWORD);
    assertHighlighting("as", KEYWORD);
    assertHighlighting("bar", null);
  }

  @Test
  void testTypeDeclaration() {
    highlight("type myType = bool[] | int?");
    assertHighlighting("type", KEYWORD);
    assertHighlighting("myType", KEYWORD_LIGHT);
    assertHighlighting("bool[] | int?", null);
  }

  @Test
  void testVariableDeclaration() {
    highlight(code(
      "var foo = 42",
      "var aa = null",
      "var bb = true"));
    assertHighlighting("var", KEYWORD);
    assertHighlighting("foo", KEYWORD_LIGHT);
    assertHighlighting("42", CONSTANT);

    assertHighlighting(2, "var", KEYWORD);
    assertHighlighting(2, "aa", KEYWORD_LIGHT);
    assertHighlighting(2, "null", CONSTANT);

    assertHighlighting(3, "var", KEYWORD);
    assertHighlighting(3, "bb", KEYWORD_LIGHT);
    assertHighlighting(3, "true", CONSTANT);
  }

  @Test
  void testMetadataDeclaration() {
    highlight("metadata identifier123 = 567");
    assertHighlighting("metadata", KEYWORD);
    assertHighlighting("identifier123", KEYWORD_LIGHT);
    assertHighlighting("567", CONSTANT);
  }

  @Test
  void testModuleDeclaration() {
    highlight(code(
      "module foo1 'path-to-file' = [for dd in deployments: 'expression']",
      "module foo2 'path-to-file' = if (bar) {}",
      "module for 'resource.bicep' = { name: 'foo' }"));
    assertHighlighting("module", KEYWORD);
    assertHighlighting("foo1", KEYWORD_LIGHT);
    assertHighlighting("'path-to-file'", STRING);
    assertHighlighting("for", KEYWORD);
    assertHighlighting("dd", ANNOTATION);
    assertHighlighting("in", KEYWORD);
    assertHighlighting("deployments", null);
    assertHighlighting("'expression'", STRING);

    assertHighlighting(2, "module", KEYWORD);
    assertHighlighting(2, "foo2", KEYWORD_LIGHT);
    assertHighlighting(2, "'path-to-file'", STRING);
    assertHighlighting(2, "if", KEYWORD);
    assertHighlighting(2, "bar", null);

    assertHighlighting(3, "module", KEYWORD);
    assertHighlighting(3, "for", KEYWORD_LIGHT);
    assertHighlighting(3, "'resource.bicep'", STRING);
    assertHighlighting(3, "name", ANNOTATION);
    assertHighlighting(3, "'foo'", STRING);
  }

  @Test
  void testOutputDeclaration() {
    highlight(code(
      "output myOutput string = myValue",
      "output myOutput string = virtualNetwork::subnet1.id"));
    assertHighlighting(1, "output", KEYWORD);
    assertHighlighting(1, "myOutput", KEYWORD_LIGHT);
    assertHighlighting(1, "string", KEYWORD);
    assertHighlighting(1, "myValue", null);

    assertHighlighting(2, "output", KEYWORD);
    assertHighlighting(2, "myOutput", KEYWORD_LIGHT);
    assertHighlighting(2, "string", KEYWORD);
    assertHighlighting(2, "virtualNetwork::subnet1.id", null);
  }

  @Test
  void testFunctionDeclaration() {
    highlight(code(
      "func myFunction1() string => 'result'",
      "func myFunction2(foo int, bar object) int => 0"));
    assertHighlighting(1, "func", KEYWORD);
    assertHighlighting(1, "myFunction1", KEYWORD_LIGHT);
    assertHighlighting(1, "string", KEYWORD);
    assertHighlighting(1, "'result'", STRING);

    assertHighlighting(2, "func", KEYWORD);
    assertHighlighting(2, "myFunction2", KEYWORD_LIGHT);
    assertHighlighting(2, "foo", null);
    assertHighlighting(2, "int", KEYWORD);
    assertHighlighting(2, "bar", null);
    assertHighlighting(2, "object", KEYWORD);
  }
}
