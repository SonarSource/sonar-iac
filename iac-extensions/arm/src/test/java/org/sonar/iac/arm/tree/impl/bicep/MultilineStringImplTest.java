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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.MultilineString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class MultilineStringImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseMultilineString() {
    ArmAssertions.assertThat(BicepLexicalGrammar.MULTILINE_STRING)
      .matches("''''''")
      .matches("'''python main.py'''")
      .matches("'''python main.py --abc ${{input.abc}} --def ${xyz}'''")
      .matches("""
        '''
        first line
        second line
        '''""")
      .matches("""
        '''
        first line
        // inline comment
        '''""")
      .matches("""
        '''
        first line
        /* inline comment */
        '''""")
      .matches("""
        '''
        first line
        /* inline
        comment */
        '''""")
      .matches("""
        '''
        it's awesome
        '''""")
      .matches("""
        '''
        it''s awesome
        '''""")
      .matches("""
        ''''test''''""")
      .matches("'''ab''''")
      .matches("""
        '''
        ab
        ''''""")
      .matches("''''aaa'''")
      .matches("'''aa\"a'''")
      .matches("'''a\\'\\'\\'a'''")
      .matches("'''a''\\'a'''")
      .matches("''''''")
      .matches("'''plain text'''")
      .matches("""
        '''
        first line
        second line
        '''""")

      .notMatches("''''")
      .notMatches("''ab''''")
      .notMatches("''ab''")
      .notMatches("'ab'")
      .notMatches("'''ab'")
      .notMatches("'''ab''")
      .notMatches("\"\"\"ab\"\"\"")
      .notMatches("\"\"\"ab'''")
      .notMatches("'''a")
      .notMatches("''a")
      .notMatches("'a")
      .notMatches("""
        '''
        ab"
        ''""")
      .notMatches("""
        '''
        ab
        '""")
      .notMatches("""
        '''
        ab""");
  }

  @Test
  void shouldParseSimpleMultilineString() {
    String code = """
      '''
      a
      123
      BBB
      '''""";

    MultilineString tree = parse(code, BicepLexicalGrammar.MULTILINE_STRING);

    assertThat(tree.value()).isEqualTo("a\n123\nBBB\n");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MULTILINE_STRING);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("'''", "a\n123\nBBB\n", "'''");
    assertThat(tree.textRange()).hasRange(1, 0, 5, 3);
  }

  @Test
  void shouldParseMultistringEndingWIthSeveralQuotes() {
    MultilineString tree = parse("''''abc'''''''", BicepLexicalGrammar.MULTILINE_STRING);

    assertThat(tree.value()).isEqualTo("'abc''''");
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MULTILINE_STRING);
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("'''", "'abc''''", "'''");
    assertThat(tree.textRange()).hasRange(1, 0, 1, 14);
  }

  @Test
  void shouldFallbackToMultilineStringWhenNoExpression() {
    ArmTree tree = parse("'''\n\nplain text'''", BicepLexicalGrammar.MULTILINE_STRING);

    assertThat(tree).isInstanceOf(MultilineString.class);
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MULTILINE_STRING);
    assertThat(((MultilineString) tree).value()).isEqualTo("plain text");
    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("'''", "plain text", "'''");
  }
}
