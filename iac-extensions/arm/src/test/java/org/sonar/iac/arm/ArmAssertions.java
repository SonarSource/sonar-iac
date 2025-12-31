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
package org.sonar.iac.arm;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.utils.ParserAssert;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ArrayExpressionAssert;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.BooleanLiteralAssert;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ExpressionAssert;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.IdentifierAssert;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteralAssert;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ObjectExpressionAssert;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.StringLiteralAssert;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class ArmAssertions {
  public static TextRangeAssert assertThat(@Nullable TextRange actual) {
    return TextRangeAssert.assertThat(actual);
  }

  public static ArmTreeAssert assertThat(@Nullable ArmTree actual) {
    return ArmTreeAssert.assertThat(actual);
  }

  public static ExpressionAssert assertThat(Expression actual) {
    return ExpressionAssert.assertThat(actual);
  }

  public static IdentifierAssert assertThat(Identifier actual) {
    return IdentifierAssert.assertThat(actual);
  }

  public static StringLiteralAssert assertThat(StringLiteral actual) {
    return StringLiteralAssert.assertThat(actual);
  }

  public static NumericLiteralAssert assertThat(NumericLiteral actual) {
    return NumericLiteralAssert.assertThat(actual);
  }

  public static BooleanLiteralAssert assertThat(BooleanLiteral actual) {
    return BooleanLiteralAssert.assertThat(actual);
  }

  public static ObjectExpressionAssert assertThat(ObjectExpression actual) {
    return ObjectExpressionAssert.assertThat(actual);
  }

  public static ArrayExpressionAssert assertThat(ArrayExpression actual) {
    return ArrayExpressionAssert.assertThat(actual);
  }

  public static ParserAssert assertThat(GrammarRuleKey rule) {
    return new ParserAssert(BicepParser.create(rule));
  }

  public static AbstractComparableAssert<?, ArmTree.Kind> assertThat(ArmTree.Kind kind) {
    return Assertions.assertThat(kind);
  }
}
