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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedMultilineString;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringLeftPiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringMiddlePiece;
import org.sonar.iac.arm.tree.api.bicep.interpstring.InterpolatedStringRightPiece;

class InterpolatedMultilineStringImplTest extends BicepTreeModelTest {

  @Test
  void shouldMatchValidMultilineInterpolatedStrings() {
    ArmAssertions.assertThat(BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING)
      // interpolated multiline — single expression
      .matches("$'''${expr}'''")
      .matches("$'''hello ${name}world'''")
      .matches("$'''\nhello ${name}\nworld'''")
      // interpolated multiline — multiple expressions
      .matches("$'''${a}text${b}'''")
      .matches("$'''hello ${first}middle${second}world'''")
      .matches("$$'''hello ${first}middle$${second}world'''")
      .matches("$$$'''hello $$${first}middle$${second}world'''")
      .matches("$$$$$$$$$'''hello $$$$$$$$$$$$$$$${first}middle$$$$$$$${second}world'''")
      .matches("$'''{${123}{0}${true}}'''")
      .matches("$'''abc${'def${123}'}_${'${456}${789}'}'''")
      .matches("$'''abc${123}${456}jk$l${789}p$'''")
      .matches("$$'''abc$${123}'''")
      .matches("$'''${123}def'''")
      .matches("$''''''")
      // comments containing ''' between expressions must not terminate the string prematurely
      .matches("$'''\n// comment with ''' in it\n${expr}suffix'''")
      .matches("$'''prefix${expr1}\n// comment with ''' in it\n${expr2}suffix'''")
      .matches("$'''prefix${expr1}/* block comment with ''' in it */${expr2}suffix'''")
      .matches("$'''prefix${expr1}\n# hash comment with ''' in it\n${expr2}suffix'''")
      // multiple comments back-to-back (no non-comment content between them)
      .matches("$'''prefix${expr1}\n// first '''\n// second '''\n${expr2}suffix'''")
      .matches("$'''prefix${expr1}/* block ''' *//* another ''' */${expr2}suffix'''")
      // multiple comments with non-comment content in between
      .matches("$'''prefix${expr1}\n// first '''\nmiddle\n// second '''\n${expr2}suffix'''")
      .matches("$'''prefix${expr1}/* block ''' */between/* another ''' */${expr2}suffix'''")
      // multiline block comment containing ''' and newlines
      .matches("$'''prefix${expr1}/* block \n  comment with ''' \n  and new line in it */${expr2}suffix'''")

      .notMatches("'text'")
      .notMatches("${expr}")
      .notMatches("$'''unclosed")
      .notMatches("$$$$$$$$$$$'''$$$${unclosed_quotes}")
      .notMatches("$$$$$$$$$$$'''$$$${unclosed_curly'''");
  }

  @Test
  void shouldParseMultilineInterpolatedStringTree() {
    InterpolatedMultilineString tree = parse("$$$'''hello $$$$$$${name}world'''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.INTERPOLATED_MULTILINE_STRING);
      softly.assertThat(tree.children()).hasSize(2);
      softly.assertThat(tree.children().get(0)).isInstanceOf(InterpolatedStringLeftPiece.class);
      softly.assertThat(tree.children().get(1)).isInstanceOf(InterpolatedStringRightPiece.class);
    });
  }

  @Test
  void shouldParseMultilineInterpolatedStringTreeWithMiddlePieces() {
    InterpolatedMultilineString tree = parse("$$$$$$$$'''a$$${123}b$${456}c'''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.INTERPOLATED_MULTILINE_STRING);
      softly.assertThat(tree.children()).hasSize(3);
      softly.assertThat(tree.children().get(0)).isInstanceOf(InterpolatedStringLeftPiece.class);
      softly.assertThat(tree.children().get(1)).isInstanceOf(InterpolatedStringMiddlePiece.class);
      softly.assertThat(tree.children().get(2)).isInstanceOf(InterpolatedStringRightPiece.class);
    });
  }

  @Test
  void shouldDropExpressionInMultilineInterpolatedValue() {
    SoftAssertions.assertSoftly(softly -> {
      InterpolatedMultilineString single = parse("$'''hello ${name}world'''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);
      softly.assertThat(single.value()).isEqualTo("hello world");
      InterpolatedMultilineString withMiddle = parse("$'''a${123}b${456}c'''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);
      softly.assertThat(withMiddle.value()).isEqualTo("abc");
      InterpolatedMultilineString adjacent1 = parse("$$$'''a$$${123}$${456}c'''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);
      softly.assertThat(adjacent1.value()).isEqualTo("ac");
      InterpolatedMultilineString adjacent2 = parse("$'''a${123}$${456}c'''", BicepLexicalGrammar.INTERPOLATED_MULTILINE_STRING);
      softly.assertThat(adjacent2.value()).isEqualTo("ac");
    });
  }
}
