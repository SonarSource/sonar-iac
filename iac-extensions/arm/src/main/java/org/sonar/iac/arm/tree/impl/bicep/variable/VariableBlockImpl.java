package org.sonar.iac.arm.tree.impl.bicep.variable;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.variable.LocalVariable;
import org.sonar.iac.arm.tree.api.bicep.variable.VariableBlock;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class VariableBlockImpl extends AbstractArmTreeImpl implements VariableBlock {
  private final SyntaxToken lPar;
  private final SeparatedList<LocalVariable, SyntaxToken> variableList;
  private final SyntaxToken rPar;

  public VariableBlockImpl(SyntaxToken lPar, SeparatedList<LocalVariable, SyntaxToken> variableList, SyntaxToken rPar) {
    this.lPar = lPar;
    this.variableList = variableList;
    this.rPar = rPar;
  }

  @Override
  public List<LocalVariable> variables() {
    return variableList.elements();
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(lPar);
    children.addAll(variableList.elementsAndSeparators());
    children.add(rPar);
    return children;
  }
}
