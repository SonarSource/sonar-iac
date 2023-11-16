package org.sonar.iac.helm.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.sonarsource.iac.helm.ActionNode;
import org.sonarsource.iac.helm.FieldNode;
import org.sonarsource.iac.helm.IfNode;
import org.sonarsource.iac.helm.ListNode;
import org.sonarsource.iac.helm.TextNode;

public class ReadAstTest {
  @Test
  void shouldReadL1M1() throws IOException {
    var listNode = ListNode.parseFrom(new FileInputStream("src/test/resources/L1M1.pb"));
    var yamlFromFile = IOUtils.toString(new FileInputStream("src/test/resources/L1M1.yaml"), StandardCharsets.UTF_8);
    System.out.println(listNode);

    var node0 = listNode.getNodes(0);
    var textNode = node0.unpack(TextNode.class);
    System.out.println("Text Node "  +textNode);
    var node1 = listNode.getNodes(1);
    // Throws ArrayIndexOutOfBoundsException: Index 4 out of bounds for length 4
    var actionNode = node1.unpack(ActionNode.class);
    var pos = actionNode.getPos();
    System.out.println("Action node Pos: " + pos);
    System.out.println("Pos in YAML 167: " + yamlFromFile.substring(167, 200));

    var fieldNode = actionNode.getPipe().getCmds(0).getArgs(0).unpack(FieldNode.class);
    System.out.println("fieldNode: " + fieldNode);
    System.out.println("fieldNode pos  : " + fieldNode.getPos());
    System.out.println("Pos in YAML 174: " + yamlFromFile.substring(174, 210));

  }

  @Test
  void shouldReadL1M5() throws IOException {
    var listNode = ListNode.parseFrom(new FileInputStream("src/test/resources/L1M5.pb"));
    var yamlFromFile = IOUtils.toString(new FileInputStream("src/test/resources/L1M1.yaml"), StandardCharsets.UTF_8);

    var ifNode = listNode.getNodes(1).unpack(IfNode.class);
    System.out.println("ifNode pos: 57");
    System.out.println("Pos in YAML 57: " + yamlFromFile.substring(57, 100));

  }
}
