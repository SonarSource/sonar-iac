package org.sonar.iac.kubernetes.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.TupleTreeImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class EmptyChecksVisitorTest {

  @Test
  void shouldNotInteractWithContext() {
    var visitor = new EmptyChecksVisitor();
    InputFileContext ctx = mock(InputFileContext.class);
    Tree root = new TupleTreeImpl(null, null, null);
    visitor.scan(ctx, root);
    verifyNoInteractions(ctx);
  }
}
