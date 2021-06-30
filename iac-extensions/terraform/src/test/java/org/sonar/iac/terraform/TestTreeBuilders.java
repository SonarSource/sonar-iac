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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.tree.impl.AttributeTreeImpl;
import org.sonar.iac.terraform.tree.impl.BlockTreeImpl;
import org.sonar.iac.terraform.tree.impl.LabelTreeImpl;
import org.sonar.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.ObjectElementTreeImpl;
import org.sonar.iac.terraform.tree.impl.ObjectTreeImpl;
import org.sonar.iac.terraform.tree.impl.SeparatedTreesImpl;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.VariableExprTreeImpl;

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

    private SyntaxToken identifier;
    private List<LabelTree> labels = new ArrayList<>();
    private List<StatementTree> statements = new ArrayList<>();

    private BlockBuilder() {
    }

    public static BlockBuilder block() {
      return new BlockBuilder();
    }

    public BlockBuilder identifier(String identifier) {
      this.identifier = token(identifier);
      return this;
    }

    public BlockBuilder labels(LabelTree... labels) {
      return labels(Arrays.asList(labels));
    }

    public BlockBuilder labels(List<LabelTree> labels) {
      this.labels = labels;
      return this;
    }

    public BlockBuilder statements(List<StatementTree> statements) {
      this.statements = statements;
      return this;
    }

    public BlockTree build() {
      return new BlockTreeImpl(identifier, labels, token("{"), token("\n"), statements, token("}"), TerraformTree.Kind.BLOCK);
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
    private SyntaxToken identifier;
    private ExpressionTree value;

    private AttributeBuilder() {

    }

    public static AttributeBuilder attribute() {
      return new AttributeBuilder();
    }

    public AttributeBuilder identifier(String identifier) {
      this.identifier = token(identifier);
      return this;
    }

    public AttributeBuilder value(ExpressionTree value) {
      this.value = value;
      return this;
    }

    public AttributeTree build() {
      return new AttributeTreeImpl(identifier, token("="), value);
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

  public static class ObjectBuilder {

    private List<ObjectElementTree> elements = new ArrayList<>();

    private ObjectBuilder() {
    }

    public static ObjectBuilder object() {
      return new ObjectBuilder();
    }

    public ObjectBuilder element(ObjectElementTree element) {
      elements.add(element);
      return this;
    }
    public ObjectBuilder element(String identifier, ExpressionTree value) {
      elements.add(new ObjectElementTreeImpl(new VariableExprTreeImpl(token(identifier)), token(":"), value));
      return this;
    }

    public ObjectTree build() {
      return new ObjectTreeImpl(token("{"), new SeparatedTreesImpl<>(elements, getSeparators()),token("}"));
    }

    private List<SyntaxToken> getSeparators() {
      return Stream.generate(() -> token(",")).limit(elements.size()).collect(Collectors.toList());
    }
  }
}
