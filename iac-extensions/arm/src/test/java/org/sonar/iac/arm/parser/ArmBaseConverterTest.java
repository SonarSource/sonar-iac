package org.sonar.iac.arm.parser;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.SequenceTreeImpl;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.TupleTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArmBaseConverterTest {

  private InputFileContext inputFileContext;
  private YamlTreeMetadata yamlTreeMetadata;

  @BeforeEach
  void init() {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.toString()).thenReturn("dir1/dir2/foo.json");
    inputFileContext = new InputFileContext(null, inputFile);

    yamlTreeMetadata = new YamlTreeMetadata(
      "tag",
      new TextRange(new TextPointer(1, 5), new TextPointer(1, 8)),
      List.of());
  }

  @Test
  void shouldThrowExceptionWhenToIdentifierNotScalarTree() {
    ArmBaseConverter converter = new ArmBaseConverter(inputFileContext);
    SequenceTree tree = new SequenceTreeImpl(List.of(), yamlTreeMetadata);

    ParseException exception = catchThrowableOfType(() -> converter.toIdentifier(tree), ParseException.class);

    assertThat(exception)
      .hasMessageStartingWith("Couldn't convert 'org.sonar.iac.common.yaml.tree.SequenceTreeImpl@")
      .hasMessageEndingWith("into Identifier: expecting ScalarTree, got SequenceTreeImpl instead at dir1/dir2/foo.json:1:5");
    assertThat(exception.getDetails()).isNull();
    assertThat(exception.getPosition().line()).isEqualTo(1);
    assertThat(exception.getPosition().lineOffset()).isEqualTo(4);
  }

  @Test
  void shouldThrowExceptionWhenToExpressionWhenTupleTree() {
    ArmBaseConverter converter = new ArmBaseConverter(inputFileContext);
    TupleTree tree = new TupleTreeImpl(new SequenceTreeImpl(List.of(), yamlTreeMetadata), new SequenceTreeImpl(List.of(), yamlTreeMetadata), yamlTreeMetadata);

    ParseException exception = catchThrowableOfType(() -> converter.toIdentifier((YamlTree) tree), ParseException.class);

    assertThat(exception)
      .isInstanceOf(ParseException.class)
      .hasMessageStartingWith("Couldn't convert 'org.sonar.iac.common.yaml.tree.TupleTreeImpl@")
      .hasMessageEndingWith("into Identifier: expecting ScalarTree, got TupleTreeImpl instead at dir1/dir2/foo.json:1:5");
    assertThat(exception.getDetails()).isNull();
    assertThat(exception.getPosition().line()).isEqualTo(1);
    assertThat(exception.getPosition().lineOffset()).isEqualTo(4);
  }
}
