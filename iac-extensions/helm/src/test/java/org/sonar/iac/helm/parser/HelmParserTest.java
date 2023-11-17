package org.sonar.iac.helm.parser;

import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HelmParserTest {
  @ParameterizedTest
  @ValueSource(strings = {
    "L1M1.yaml",
    "L1M2.yaml",
    "L1M3.yaml",
    "L1M4.yaml",
    "L1M5.yaml",
  })
  void shouldBuildAndLoadAst(String filename) throws IOException {
    var helmParser = new HelmParser();
    var templateId = helmParser.loadGoTemplate(
      new String(
        Thread.currentThread().getContextClassLoader().getResourceAsStream(filename).readAllBytes()));
    var listNode = helmParser.getAst(templateId);

    Assertions.assertThat(listNode).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "L1M1.yaml",
    "L1M2.yaml",
    "L1M3.yaml",
    "L1M4.yaml",
    "L1M5.yaml",
  })
  void test(String filename) throws IOException {
    new HelmParser().parse(
      new String(
        Thread.currentThread().getContextClassLoader().getResourceAsStream(filename).readAllBytes()),
      null);
  }
}
