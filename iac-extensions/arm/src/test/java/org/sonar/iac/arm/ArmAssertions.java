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

import com.sonar.sslr.api.Rule;
import javax.annotation.Nullable;
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
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteralAssert;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.ObjectExpressionAssert;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.StringLiteralAssert;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.tests.RuleAssert;

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

  public static RuleAssert assertThat(Rule actual) {
    return new RuleAssert(actual);
  }

  public static ParserAssert assertThat(GrammarRuleKey rule) {
    return new ParserAssert(BicepParser.create(rule));
  }

  public static org.assertj.core.api.AbstractComparableAssert<?, ArmTree.Kind> assertThat(ArmTree.Kind kind) {
    return Assertions.assertThat(kind);
  }
}
