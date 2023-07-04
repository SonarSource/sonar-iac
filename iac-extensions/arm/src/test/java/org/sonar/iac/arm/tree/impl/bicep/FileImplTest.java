package org.sonar.iac.arm.tree.impl.bicep;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.tree.api.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class FileImplTest {

  BicepParser parser = BicepParser.create();

  @Test
  void shouldParseMinimalParameter() {
    String code = code("");

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
    assertThat(tree.targetScope()).isEqualTo(File.Scope.RESOURCE_GROUP);
    ArmAssertions.assertThat(tree.targetScopeLiteral()).isNull();
  }
}
