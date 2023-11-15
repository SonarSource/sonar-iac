package org.sonar.iac.helm.parser;

import com.google.protobuf.Any;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sonarsource.iac.helm.ActionNode;
import org.sonarsource.iac.helm.GoTemplateAst;
import org.sonarsource.iac.helm.ListNode;
import org.sonarsource.iac.helm.TextNode;

public class ReadAstTest {
  @Test
  void shouldReadL1M1() throws IOException {
    var listNode = ListNode.parseFrom(new FileInputStream("src/test/resources/L1M1.pb"));
    System.out.println(listNode);

    var node0 = listNode.getNodes(0);
    var textNode = node0.unpack(TextNode.class);
    System.out.println("Text Node "  +textNode);
    var node1 = listNode.getNodes(1);
    // Throws ArrayIndexOutOfBoundsException: Index 4 out of bounds for length 4
    var actionNode = node1.unpack(ActionNode.class);
    var pos = actionNode.getPos();
    System.out.println("Position: " + pos);
  }
}
