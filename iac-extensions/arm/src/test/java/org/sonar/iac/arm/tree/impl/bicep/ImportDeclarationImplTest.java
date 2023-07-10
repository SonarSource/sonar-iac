package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;

class ImportDeclarationImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidStatement() {
    Assertions.assertThat(BicepLexicalGrammar.IMPORT_DECLARATION)
      .matches("import 'foo'")
      .matches("import 'foo' as bar")
      .matches("import 'foo' with {}")
      .matches("import 'foo' with {} as bar")

      .notMatches("import")
      .notMatches("import with {}")
      .notMatches("import as bar")
      .notMatches("import 'foo' as");
  }

  @Test
  void shouldParseImportStatement() {
    ArmTree tree = createParser(BicepLexicalGrammar.IMPORT_DECLARATION)
      .parse("import 'foo' with {} as bar");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ImportDeclaration.class);
    softly.assertThat(tree.children()).hasSize(6);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.IMPORT_DECLARATION);
    softly.assertAll();
  }
}
