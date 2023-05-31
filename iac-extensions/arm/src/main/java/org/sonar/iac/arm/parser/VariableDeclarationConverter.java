package org.sonar.iac.arm.parser;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.PropertyValue;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.json.VariableDeclarationImpl;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

public class VariableDeclarationConverter extends ArmBaseConverter {

  public VariableDeclarationConverter(@Nullable InputFileContext inputFileContext) {
    super(inputFileContext);
  }

  public Stream<TupleTree> extractVariablesMapping(MappingTree document) {
    return document.elements().stream()
      .filter(filterOnField("variables"))
      .map(TupleTree::value)
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .map(MappingTree::elements)
      .flatMap(List::stream);
  }

  public VariableDeclaration convertVariableDeclaration(TupleTree tupleTree) {
    Identifier name = convertToIdentifier(tupleTree.key());
    PropertyValue value = convertToPropertyValue(tupleTree.value());
    return new VariableDeclarationImpl(name, value);
  }
}
