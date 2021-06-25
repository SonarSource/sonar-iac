/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.tree.impl.AttributeTreeImpl;
import org.sonar.iac.terraform.tree.impl.BlockTreeImpl;
import org.sonar.iac.terraform.tree.impl.LabelTreeImpl;
import org.sonar.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;

import static org.sonar.iac.terraform.TestTreeBuilders.SyntaxTokenBuilder.token;

public class TestTreeBuilders {


  public static class SyntaxTokenBuilder {

    private SyntaxTokenBuilder() {
    }

    public static SyntaxToken token(String value) {
      return new SyntaxTokenImpl(value, null , Collections.emptyList());
    }
  }

  public static class BlockBuilder {

    private SyntaxToken type;
    private List<LabelTree> labels = new ArrayList<>();
    private List<TerraformTree> statements = new ArrayList<>();

    private BlockBuilder() {
    }

    public static BlockBuilder block() {
      return new BlockBuilder();
    }

    public BlockBuilder type(String type) {
      this.type = token(type);
      return this;
    }

    public BlockBuilder labels(LabelTree... labels) {
      return labels(Arrays.asList(labels));
    }

    public BlockBuilder labels(List<LabelTree> labels) {
      this.labels = labels;
      return this;
    }

    public BlockBuilder statements(List<TerraformTree> statements) {
      this.statements = statements;
      return this;
    }

    public BlockTree build() {
      return new BlockTreeImpl(type, labels, token("{"), statements, token("}"), TerraformTree.Kind.BLOCK);
    }
  }

  public static class LabelBuilder {

    private LabelBuilder() {
    }

    public static LabelTree label(String label) {
        return new LabelTreeImpl(token(label));
    }
  }

  public static class AttributeBuilder {
    private SyntaxToken name;
    private ExpressionTree value;

    private AttributeBuilder() {

    }

    public static AttributeBuilder attribute() {
      return new AttributeBuilder();
    }

    public AttributeBuilder name(String  name) {
      this.name = token(name);
      return this;
    }

    public AttributeBuilder value(ExpressionTree value) {
      this.value = value;
      return this;
    }

    public AttributeTree build() {
      return new AttributeTreeImpl(name, token("="), value);
    }
  }

  public static class LiteralExprBuilder {

    private LiteralExprBuilder() {
    }

    public static LiteralExprTree booleanExpr(String value) {
      return new LiteralExprTreeImpl(TerraformTree.Kind.BOOLEAN_LITERAL, token(value));
    }

    public static LiteralExprTree stringExpr(String value) {
      return new LiteralExprTreeImpl(TerraformTree.Kind.STRING_LITERAL, token(value));
    }
  }
}
