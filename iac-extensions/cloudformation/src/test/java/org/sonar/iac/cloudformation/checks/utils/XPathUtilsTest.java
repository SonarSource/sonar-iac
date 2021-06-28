package org.sonar.iac.cloudformation.checks.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.checks.CloudformationVerifier;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XPathUtilsTest {

  private TestXPathCheck check;

  @BeforeEach
  void setUp() {
    check = new TestXPathCheck();
    CloudformationVerifier.verifyNoIssue("AbstractXPathCheck/test.yaml", check);
  }

  @Test
  void test_getTrees() {
    assertThat(XPathUtils.getTrees(check.root, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]/Principal/AWS"))
      .isNotEmpty().hasSize(1)
      .satisfies(t -> { CloudformationTree tree = t.get(0);
        assertThat(tree).isInstanceOfSatisfying(SequenceTree.class, s ->
          assertThat(s.elements()).hasSize(1).satisfies(els -> { CloudformationTree element = els.get(0);
              assertThat(element).isInstanceOfSatisfying(ScalarTree.class, v ->
                assertThat(v.value()).isEqualTo("arn:aws:iam::123456789123:root"));
            }));
      });
  }

  @Test
  void test_getSingleTree() {
    assertThat(XPathUtils.getSingleTree(check.root, "/Resources/S3BucketPolicy/Properties/PolicyDocument/Statement[]"))
      .isNotPresent();
    assertThat(XPathUtils.getSingleTree(check.root, "/Resources/S3BucketPolicy/Properties/PolicyDocument"))
      .isPresent();
  }

  @Test
  void test_getSingleTree_with_custom_root() {
    assertThat(XPathUtils.getSingleTree(check.root,"/Resources/S3BucketPolicy")).isPresent()
      .satisfies(o ->
        assertThat(XPathUtils.getSingleTree(o.get(), "/Properties/PolicyDocument")).isPresent());
  }

  @Test
  void test_invalid_expression() {
    assertThatThrownBy(() -> XPathUtils.getSingleTree(check.root,"Resources/S3BucketPolicy"))
      .isInstanceOf(XPathUtils.InvalidXPathExpression.class);
  }

  @Test
  void test_only_root_expression() {
    assertThat(XPathUtils.getSingleTree(check.root,"/")).isPresent().get().isEqualTo(check.root);
  }

  private static class TestXPathCheck implements IacCheck {
    private CloudformationTree root;

    @Override
    public void initialize(InitContext init) {
      init.register(FileTree.class, (ctx, tree) -> {
        this.root = tree.root();
      });
    }
  }

}


