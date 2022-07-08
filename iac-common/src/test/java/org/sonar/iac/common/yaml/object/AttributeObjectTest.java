package org.sonar.iac.common.yaml.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeObjectTest {

  static final List<TestIssue> raisedIssues = new ArrayList<>();
  static YamlParser PARSER = new YamlParser();
  CheckContext ctx = new TestContext();

  @Test
  void fromPresent() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attr = AttributeObject.fromPresent(ctx, tree, "a");
    assertThat(attr.key).isEqualTo("a");
    assertThat(attr.status).isEqualTo(YamlObject.Status.PRESENT);
    assertThat(attr.tree).isEqualTo(tree);
    assertThat(attr.ctx).isEqualTo(ctx);
  }

  @Test
  void fromPresent_unknown() {
    YamlTree tree = PARSER.parse("a:b", null).root();
    AttributeObject attr = AttributeObject.fromPresent(ctx, tree, "a");
    assertThat(attr.key).isEqualTo("a");
    assertThat(attr.status).isEqualTo(YamlObject.Status.UNKNOWN);
    assertThat(attr.tree).isNull();
    assertThat(attr.ctx).isEqualTo(ctx);
  }

  @Test
  void fromAbsent() {
    AttributeObject attr = AttributeObject.fromAbsent(ctx,"a");
    assertThat(attr.key).isEqualTo("a");
    assertThat(attr.status).isEqualTo(YamlObject.Status.ABSENT);
    assertThat(attr.tree).isNull();
    assertThat(attr.ctx).isEqualTo(ctx);
  }

  @Test
  void reportIfValue() {
    TupleTree tree = parseTuple("a: b");
    AttributeObject attr = AttributeObject.fromPresent(ctx, tree, "a");
    attr.reportIfValue(t -> true, "message");
    assertThat(raisedIssues).hasSize(1);
    TestIssue issue = raisedIssues.get(0);
    assertThat(issue.message).isEqualTo("message");
    assertThat(issue.secondaryLocations).isEmpty();
    assertThat(issue.textRange).isEqualTo(tree.textRange());
  }

  private static TupleTree parseTuple(String source) {
    return ((MappingTree) PARSER.parse(source, null).root()).elements().get(0);
  }

  private static class TestContext implements CheckContext {


    @Override
    public void reportIssue(TextRange textRange, String message) {
      raisedIssues.add(new TestIssue(textRange, message, Collections.emptyList()));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message) {
      reportIssue(toHighlight, message, Collections.emptyList());
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message, SecondaryLocation secondaryLocation) {
      reportIssue(toHighlight, message, List.of(secondaryLocation));
    }

    @Override
    public void reportIssue(HasTextRange toHighlight, String message, List<SecondaryLocation> secondaryLocations) {
      raisedIssues.add(new AttributeObjectTest.TestIssue(toHighlight.textRange(), message, secondaryLocations));
    }
  }

  private static class TestIssue {

    final TextRange textRange;
    final String message;
    final List<SecondaryLocation> secondaryLocations;

    private TestIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      this.textRange = textRange;
      this.message = message;
      this.secondaryLocations = secondaryLocations;
    }
  }

}
