package org.sonar.iac.docker.visitors;

import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;
import org.sonar.iac.docker.parser.DockerParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.testing.IacTestUtils.code;


class DockerMetricsVisitorTest extends AbstractMetricsTest {

  @Override
  protected TreeParser<Tree> treeParser() {
    return new DockerParser();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new DockerMetricsVisitor(fileLinesContextFactory, noSonarFilter);
  }

  @Test
  void emptySource() {
    scan("");
    assertThat(visitor.linesOfCode()).isEmpty();
    assertThat(visitor.commentLines()).isEmpty();
    verify(noSonarFilter).noSonarInFile(inputFile, new HashSet<>());
  }

  @Test
  void linesOfCode() {
    scan(code(
      "FROM foo",
        "",
        "MAINTAINER foo<bar>",
        "",
        "RUN \\",
        "  command1 \\",
        "  command2"
    ));
    assertThat(visitor.linesOfCode()).containsExactly(1, 3, 5, 6, 7);
  }

  @Test
  void commentLines() {
    scan(code(
      "# comment 1",
      "# comment 2",
      "FROM foo",
      "RUN \\",
      "  command1 \\",
      "  # comment 3",
      "  command2"
    ));
    assertThat(visitor.commentLines()).containsExactly(1, 2, 6);
  }
}
