package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.ModuleDeclaration;
import org.sonar.iac.common.testing.IacTestUtils;

class ModuleDeclarationImplTest extends BicepTreeModelTest {
  @Test
  void shouldParseValidDeclarations() {
    Assertions.assertThat(BicepLexicalGrammar.MODULE_DECLARATION)
      .matches("module foo 'path-to-file' = {}")
      .matches("module foo 'path-to-file' = [for d in deployments: expression]")
      .matches("module foo 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}")

      .notMatches("module foo = {}")
      .notMatches("module 'br:mcr.microsoft.com/bicep/foo.bicep:bar' = {}")
      .notMatches("module foo bar = {}")
      .notMatches("module foo 'foo.bicep' = 123");
  }

  @Test
  void shouldParseDeclarationCorrectly() {
    ModuleDeclaration tree = (ModuleDeclaration) createParser(BicepLexicalGrammar.MODULE_DECLARATION).parse(
      IacTestUtils.code("module stgModule '../storageAccount.bicep' = {",
        "  name: 'storageDeploy'",
        "}"));

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree).isInstanceOf(ModuleDeclaration.class);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.MODULE_DECLARATION);
    softly.assertThat(tree.children()).hasSize(5);
    softly.assertThat(tree.children().get(4)).isInstanceOf(Expression.class);
    softly.assertAll();
  }
}
