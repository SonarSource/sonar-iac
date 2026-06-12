/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.parser;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonLiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.json.JsonObjectTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class HeredocContentParserTest {

  private static final TextRange RANGE = new TextRange(new TextPointer(1, 0), new TextPointer(5, 0));

  @Test
  void shouldUseGeneralTypesForNonJsonContent() {
    // Non-JSON heredoc bodies must use general Terraform tree nodes — not the JSON-specific ones —
    // so future content kinds (SQL, shell, ...) can slot in without forcing everything through the JSON path.
    SyntaxToken token = heredoc("<<EOF\nSELECT * FROM users\nEOF");
    ExpressionTree result = HeredocContentParser.parse(token);

    assertThat(result).isInstanceOf(LiteralExprTreeImpl.class)
      .isNotInstanceOf(JsonLiteralExprTreeImpl.class);
    assertThat(((LiteralExprTreeImpl) result).token()).isInstanceOf(SyntaxTokenImpl.class);
    assertThat(((LiteralExprTreeImpl) result).value()).isEqualTo("SELECT * FROM users");
    assertThat(result.getKind()).isEqualTo(Kind.TEMPLATE_STRING_PART_LITERAL);
  }

  @Test
  void shouldUseJsonTypesOnlyForJsonContent() {
    SyntaxToken token = heredoc("<<POLICY\n{\"key\": \"value\"}\nPOLICY");
    ExpressionTree result = HeredocContentParser.parse(token);

    assertThat(result).isInstanceOf(JsonObjectTreeImpl.class);
  }

  @Test
  void shouldBuildObjectTreeFromJsonObjectBody() {
    SyntaxToken token = heredoc("""
      <<POLICY
      {
        "Version": "2012-10-17",
        "Statement": [
          { "Effect": "Deny", "Action": "s3:*" }
        ]
      }
      POLICY""");

    ExpressionTree result = HeredocContentParser.parse(token);

    assertThat(result).isInstanceOf(ObjectTree.class);
    ObjectTree object = (ObjectTree) result;
    assertThat(PropertyUtils.value(object, "Version").flatMap(TextUtils::getValue)).hasValue("2012-10-17");

    ExpressionTree statementsValue = (ExpressionTree) PropertyUtils.value(object, "Statement").orElseThrow();
    assertThat(statementsValue).isInstanceOf(TupleTree.class);
    TupleTree statements = (TupleTree) statementsValue;
    assertThat(statements.elements().trees()).hasSize(1);

    ObjectTree statement = (ObjectTree) statements.elements().trees().get(0);
    assertThat(PropertyUtils.value(statement, "Effect").flatMap(TextUtils::getValue)).hasValue("Deny");
    assertThat(PropertyUtils.value(statement, "Action").flatMap(TextUtils::getValue)).hasValue("s3:*");
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("nonJsonObjectBodies")
  void shouldWrapNonJsonObjectBodyAsPlainTextLiteral(String tokenText, String expectedValue, String description) {
    SyntaxToken token = heredoc(tokenText);
    ExpressionTree result = HeredocContentParser.parse(token);
    assertThat(result).isInstanceOf(LiteralExprTree.class);
    assertThat(((LiteralExprTree) result).value()).isEqualTo(expectedValue);
  }

  private static Stream<Arguments> nonJsonObjectBodies() {
    return Stream.of(
      arguments("<<EOF\n[1, 2, 3]\nEOF", "[1, 2, 3]", "JSON array root is not the {...} shape we expose structurally"),
      arguments("<<EOF\n{ not valid json\nEOF", "{ not valid json", "Malformed JSON falls back to raw"),
      arguments("<<EOF\nplain text body\nEOF", "plain text body", "Plain text body is preserved verbatim"),
      arguments("<<EOFonelinerEOF", "<<EOFonelinerEOF", "No interior newlines — markers cannot be stripped, full token text preserved"));
  }

  @Test
  void shouldHandleEmptyJsonObject() {
    SyntaxToken token = heredoc("<<POLICY\n{}\nPOLICY");
    ExpressionTree result = HeredocContentParser.parse(token);
    assertThat(result).isInstanceOf(ObjectTree.class);
    assertThat(((ObjectTree) result).properties()).isEmpty();
  }

  @Test
  void shouldAssignPreciseRangesToEachNode() {
    // The shared RANGE starts on line 1, so the body (which begins on the line right after the heredoc's
    // start marker) maps to file line 2. The per-node column expectations below are 0-indexed file columns.
    SyntaxToken token = heredoc("""
      <<POLICY
      {"key": ["a", "b"]}
      POLICY""");
    ExpressionTree result = HeredocContentParser.parse(token);

    // Root object spans the whole body on file line 2.
    assertRange(result.textRange(), 2, 0, 2, 19);

    ObjectTree object = (ObjectTree) result;
    ExpressionTree key = object.properties().get(0).key();
    // Key "key" (including the surrounding quotes in the source — but value() reports unquoted).
    assertRange(key.textRange(), 2, 1, 2, 6);

    ExpressionTree value = object.properties().get(0).value();
    assertThat(value).isInstanceOf(TupleTree.class);
    // Array ["a", "b"]: `[` at col 8 (0-indexed), `]` at col 17 → exclusive end at col 18.
    assertRange(value.textRange(), 2, 8, 2, 18);

    TupleTree tuple = (TupleTree) value;
    ExpressionTree first = tuple.elements().trees().get(0);
    ExpressionTree second = tuple.elements().trees().get(1);
    // "a" is at columns 9-12, "b" at columns 14-17.
    assertRange(first.textRange(), 2, 9, 2, 12);
    assertRange(second.textRange(), 2, 14, 2, 17);
  }

  @Test
  void shouldAssignPreciseRangesAcrossMultipleBodyLines() {
    // Heredoc starts on file line 1, so the body's first line is file line 2.
    SyntaxToken token = heredoc("""
      <<POLICY
      {
        "k": 7
      }
      POLICY""");
    ExpressionTree result = HeredocContentParser.parse(token);

    // Whole object spans from `{` on line 2 col 0 to just past `}` on line 4 col 1.
    assertRange(result.textRange(), 2, 0, 4, 1);

    ObjectTree object = (ObjectTree) result;
    ExpressionTree key = object.properties().get(0).key();
    ExpressionTree value = object.properties().get(0).value();
    // Key "k" sits at line 3 cols 2-5 (the surrounding quotes count toward the column span).
    assertRange(key.textRange(), 3, 2, 3, 5);
    // Number 7 sits at line 3 col 7.
    assertRange(value.textRange(), 3, 7, 3, 8);
    assertThat(((LiteralExprTree) value).value()).isEqualTo("7");
  }

  @Test
  void shouldAssignPreciseRangesForIndentedHeredoc() {
    // Indented <<- form: body keeps its leading whitespace (we don't dedent), so columns are absolute.
    SyntaxToken token = heredoc("""
      <<-POLICY
        {"k": true}
        POLICY""");
    ExpressionTree result = HeredocContentParser.parse(token);

    // Two leading spaces push `{` to col 2 on body line 1 (= file line 2).
    assertRange(result.textRange(), 2, 2, 2, 13);
    ObjectTree object = (ObjectTree) result;
    ExpressionTree value = object.properties().get(0).value();
    // `true` is at cols 8-12.
    assertRange(value.textRange(), 2, 8, 2, 12);
  }

  private static void assertRange(TextRange range, int startLine, int startCol, int endLine, int endCol) {
    assertThat(range.start().line()).as("start line").isEqualTo(startLine);
    assertThat(range.start().lineOffset()).as("start col").isEqualTo(startCol);
    assertThat(range.end().line()).as("end line").isEqualTo(endLine);
    assertThat(range.end().lineOffset()).as("end col").isEqualTo(endCol);
  }

  /**
   * Each JSON scalar must carry the Kind that matches its native HCL counterpart, so a check gating on
   * {@code is(Kind.BOOLEAN_LITERAL)} (the natural HCL form) sees heredoc-embedded JSON the same way it
   * sees {@code jsonencode({...})}. Collapsing everything to {@code STRING_LITERAL} would make such a
   * check silently skip heredoc-based policies.
   */
  @Test
  void shouldAssignNativeKindToEachJsonScalar() {
    SyntaxToken token = heredoc("""
      <<POLICY
      {
        "s": "value",
        "b": true,
        "n": 7,
        "z": null,
        "arr": [1.5],
        "obj": {"nested": "v"}
      }
      POLICY""");
    ObjectTree result = (ObjectTree) HeredocContentParser.parse(token);

    assertThat(result.is(Kind.OBJECT)).isTrue();
    assertThat(valueOf(result, "s").getKind()).isEqualTo(Kind.STRING_LITERAL);
    assertThat(valueOf(result, "b").getKind()).isEqualTo(Kind.BOOLEAN_LITERAL);
    assertThat(valueOf(result, "n").getKind()).isEqualTo(Kind.NUMERIC_LITERAL);
    assertThat(valueOf(result, "z").getKind()).isEqualTo(Kind.NULL_LITERAL);
    // Composites stay OBJECT / TUPLE; array elements keep their own kinds too.
    ExpressionTree arr = valueOf(result, "arr");
    assertThat(arr.is(Kind.TUPLE)).isTrue();
    assertThat(((TupleTree) arr).elements().trees().get(0).getKind()).isEqualTo(Kind.NUMERIC_LITERAL);
    assertThat(valueOf(result, "obj").is(Kind.OBJECT)).isTrue();
  }

  private static ExpressionTree valueOf(ObjectTree object, String key) {
    return object.properties().stream()
      .filter(p -> ((LiteralExprTree) p.key()).value().equals(key))
      .map(p -> (ExpressionTree) p.value())
      .findFirst()
      .orElseThrow();
  }

  private static SyntaxToken heredoc(String text) {
    return new SyntaxTokenImpl(text, RANGE, Collections.<org.sonar.iac.common.api.tree.Comment>emptyList());
  }

}
