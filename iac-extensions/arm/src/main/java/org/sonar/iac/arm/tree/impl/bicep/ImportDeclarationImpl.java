package org.sonar.iac.arm.tree.impl.bicep;

import com.sonar.sslr.api.typed.Optional;
import org.sonar.iac.arm.tree.api.bicep.ImportDeclaration;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportAsClause;
import org.sonar.iac.arm.tree.impl.bicep.importdecl.ImportWithClause;
import org.sonar.iac.common.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ImportDeclarationImpl extends AbstractArmTreeImpl implements ImportDeclaration {
  private final SyntaxToken keyword;
  private final InterpolatedString specification;
  @Nullable
  private final ImportWithClause withClause;
  @Nullable
  private final ImportAsClause asClause;
  private final SyntaxToken newLine;

  public ImportDeclarationImpl(SyntaxToken keyword, InterpolatedString specification, Optional<ImportWithClause> withClause, Optional<ImportAsClause> asClause,
    SyntaxToken newLine) {
    this.keyword = keyword;
    this.specification = specification;
    this.withClause = withClause.orNull();
    this.asClause = asClause.orNull();
    this.newLine = newLine;
  }

  @Override
  public List<Tree> children() {
    List<Tree> result = new ArrayList<>();
    result.add(keyword);
    result.add(specification);
    if (withClause != null) {
      result.addAll(withClause.children());
    }
    if (asClause != null) {
      result.addAll(asClause.children());
    }
    result.add(newLine);
    return result;
  }
}
