package org.sonar.iac.arm.parser.bicep;

import com.sonar.sslr.api.typed.Optional;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Statement;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.api.bicep.TargetScopeDeclaration;
import org.sonar.iac.arm.tree.impl.bicep.FileImpl;
import org.sonar.iac.arm.tree.impl.bicep.StringLiteralImpl;
import org.sonar.iac.arm.tree.impl.bicep.SyntaxTokenImpl;
import org.sonar.iac.arm.tree.impl.bicep.TargetScopeDeclarationImpl;

public class TreeFactory {

  public File file(Optional<List<Statement>> statements, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileImpl(statements.or(Collections.emptyList()), eof);
  }

  public TargetScopeDeclaration targetScopeDeclaration(SyntaxToken targetScope, SyntaxToken equals, Expression expression) {
    return new TargetScopeDeclarationImpl(targetScope, equals, expression);
  }

  public Expression expression(StringLiteral stringLiteral) {
    return stringLiteral;
  }

  public Expression expression() {
    return new StringLiteralImpl(new SyntaxTokenImpl("", null, null));
  }

  public StringLiteral stringLiteral(SyntaxToken token) {
    return new StringLiteralImpl(token);
  }
}
