/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.visitors;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.symbols.Usage;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.HasScope;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.OnBuildInstruction;
import org.sonar.iac.docker.tree.api.Variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.docker.tree.TreeUtils.firstDescendant;
import static org.sonar.iac.docker.tree.impl.DockerTestUtils.parse;

class DockerSymbolVisitorTest {

  private static final String IMAGE_PREFIX = "FROM foobar\n";
  private final InputFileContext inputFileContext = mock(InputFileContext.class);

  @Test
  void argInstructionShouldCreateSymbol() {
    DockerImage image = scanImage("ARG foo=bar");
    KeyValuePair keyValuePair = firstDescendant(image, KeyValuePair.class).get();

    Scope scope = image.scope();

    assertThat(scope.getSymbols()).hasSize(1);
    assertThat(image.scope().getSymbol("bar")).isNull();

    Symbol symbol = image.scope().getSymbol("foo");
    assertThat(symbol).isNotNull();
    assertThat(symbol.usages()).hasSize(1).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(keyValuePair);
    });
  }

  @Test
  void envInstructionShouldCreateSymbol() {
    DockerImage image = scanImage("ENV foo=bar");
    KeyValuePair keyValuePair = firstDescendant(image, KeyValuePair.class).get();

    Scope scope = image.scope();

    assertThat(scope.getSymbols()).hasSize(1);
    assertThat(image.scope().getSymbol("bar")).isNull();

    Symbol symbol = image.scope().getSymbol("foo");
    assertThat(symbol).isNotNull();
    assertThat(symbol.usages()).hasSize(1).allSatisfy(usage -> {
      assertThat(usage.kind()).isEqualTo(Usage.Kind.ASSIGNMENT);
      assertThat(usage.tree()).isEqualTo(keyValuePair);
    });
  }

  @Test
  void argInstructionShouldCreateTwoSymbols() {
    DockerImage image = scanImage("ARG foo1=bar foo2=bar");
    List<KeyValuePair> keyValuePairs = firstDescendant(image, ArgInstruction.class).get().keyValuePairs();

    assertThat(image.scope().getSymbols()).hasSize(2);

    Symbol symbol1 = image.scope().getSymbol("foo1");
    assertThat(symbol1.usages()).extracting(Usage::tree).containsExactly(keyValuePairs.get(0));

    Symbol symbol2 = image.scope().getSymbol("foo2");
    assertThat(symbol2.usages()).extracting(Usage::tree).containsExactly(keyValuePairs.get(1));
  }

  @Test
  void scopeShouldBeInherit() {
    Body body = scanBody("ARG foo=bar\n" + IMAGE_PREFIX);
    Scope bodyScope = body.scope();
    Scope imageScope = body.dockerImages().get(0).scope();

    assertThat(bodyScope).isNotEqualTo(imageScope);
    assertThat(bodyScope.getSymbol("foo")).isNotNull();
    assertThat(imageScope.getSymbol("foo")).isNotNull();
    assertThat(bodyScope.getSymbol("foo"))
      .isNotSameAs(imageScope.getSymbol("foo"))
      .usingRecursiveComparison()
      .isEqualTo(imageScope.getSymbol("foo"));
  }

  @Test
  void twoArgInstructionShouldCreateTwoSymbols() {
    DockerImage image = scanImage("ARG foo1=bar\nARG foo2=bar");
    assertThat(image.scope().getSymbols())
      .extracting(Symbol::name).containsExactly("foo1", "foo2");
  }

  @Test
  void bodyScopeShouldNotBeAffectedByImage() {
    Body body = scanBody(IMAGE_PREFIX + "ARG foo=bar");
    Scope bodyScope = body.scope();
    Scope imageScope = body.dockerImages().get(0).scope();

    assertThat(bodyScope.getSymbol("foo")).isNull();
    assertThat(imageScope.getSymbol("foo")).isNotNull();
  }

  @Test
  void bodyScopeShouldNotBeOverrideByImage() {
    Body body = scanBody("ARG foo=bar\n" + IMAGE_PREFIX + "ARG foo=bar");
    Scope bodyScope = body.scope();
    Scope imageScope = body.dockerImages().get(0).scope();

    assertThat(bodyScope.getSymbol("foo")).isNotNull()
      .satisfies(symbol -> assertThat(symbol.usages())
        .hasSize(1)
        .extracting(usage -> usage.scope().kind()).containsExactly(Scope.Kind.GLOBAL));

    assertThat(imageScope.getSymbol("foo")).isNotNull()
      .satisfies(symbol -> assertThat(symbol.usages())
        .hasSize(2)
        .extracting(usage -> usage.scope().kind()).containsExactly(Scope.Kind.GLOBAL, Scope.Kind.IMAGE));
  }

  @Test
  void imageScopesDontShareSymbols() {
    Body body = scanBody(IMAGE_PREFIX + "ARG foo=bar\n" + IMAGE_PREFIX);
    Scope image1Scope = body.dockerImages().get(0).scope();
    Scope image2Scope = body.dockerImages().get(1).scope();

    assertThat(image1Scope.getSymbols()).hasSize(1);
    assertThat(image2Scope.getSymbols()).isEmpty();
  }

  @Test
  void imageScopesAreIndependent() {
    Body body = scanBody(IMAGE_PREFIX + "ARG foo=bar\n" + IMAGE_PREFIX + "ARG foo=bar");
    Scope image1Scope = body.dockerImages().get(0).scope();
    Scope image2Scope = body.dockerImages().get(1).scope();

    assertThat(image1Scope.getSymbol("foo").usages()).hasSize(1);
    assertThat(image2Scope.getSymbol("foo").usages()).hasSize(1);
  }

  @Test
  void globalVariableShouldBeAccessibleInFromInstruction() {
    Body body = scanBody("ARG image=scratch\nFROM $image");
    Argument arg = firstDescendant(body.dockerImages().get(0).from(), Argument.class).get();
    assertThat(ArgumentResolution.of(arg).value()).isEqualTo("scratch");
  }

  @Test
  void globalVariableShouldBeAccessibleInFromInstructionMultiple() {
    Body body = scanBody("ARG image=scratch\nFROM first\nFROM $image");
    Argument arg1 = firstDescendant(body.dockerImages().get(0).from(), Argument.class).get();
    assertThat(ArgumentResolution.of(arg1).value()).isEqualTo("first");
    Argument arg2 = firstDescendant(body.dockerImages().get(1).from(), Argument.class).get();
    assertThat(ArgumentResolution.of(arg2).value()).isEqualTo("scratch");
  }

  @ParameterizedTest
  @CsvSource({
    "$foo",
    "${foo}"
  })
  void updateSymbolOnReadUsage(String varName) {
    DockerImage image = scanImage("ARG foo=bar\nLABEL my_label=" + varName);
    Symbol symbol = image.scope().getSymbol("foo");
    Variable variable = firstDescendant(image, Variable.class).get();

    assertThat(symbol.usages()).extracting(Usage::kind).containsExactly(Usage.Kind.ASSIGNMENT, Usage.Kind.ACCESS);
    assertThat(symbol.usages().get(1).tree()).isEqualTo(variable);
    assertThat(variable.symbol()).isNotSameAs(symbol);
  }

  @Test
  void shouldNotCreateSymbolWithoutAssignment() {
    DockerImage image = scanImage("LABEL my_label=$foo");
    Variable variable = firstDescendant(image, Variable.class).get();

    assertThat(image.scope().getSymbols()).isEmpty();
    assertThat(variable.symbol()).isNull();
  }

  @Test
  void shouldNotCreateSymbolForUnresolvedName() {
    DockerImage image = scanImage("ARG $foo=bar");
    assertThat(image.scope().getSymbols()).isEmpty();
  }

  @Test
  void shouldNotCreateSymbolForEmptyName() {
    DockerImage image = scanImage("ARG FOO=\"\"\nARG $FOO=bar");
    assertThat(image.scope().getSymbols()).extracting(Symbol::name)
      .containsExactly("FOO");
  }

  @Test
  void shouldCreateSymbolForResolvedName() {
    DockerImage image = scanImage("ARG FOO=BAR\nARG $FOO=bar");
    assertThat(image.scope().getSymbols()).extracting(Symbol::name)
      .containsExactly("BAR", "FOO");
  }

  @ParameterizedTest
  @CsvSource({
    "$foo",
    "${foo}"
  })
  void multiSymbolSetShouldNotBeAllowed(String varName) {
    DockerImage image = scanImage("ARG foo=bar\nLABEL my_label=" + varName);
    Variable variable = firstDescendant(image, Variable.class).get();

    Symbol newSymbol = new Symbol("foobar");
    Throwable t = Assert.assertThrows(IllegalArgumentException.class, () -> variable.setSymbol(newSymbol));
    assertThat(t.getMessage()).isEqualTo("A symbol is already set");
  }

  @Test
  void multiScopeShouldNotBeAllowed() throws RuntimeException {
    Body body = scanBody(IMAGE_PREFIX);
    DockerImage image = body.dockerImages().get(0);

    for (HasScope hasScope : List.of(body, image)) {
      Scope newScope = new Scope(Scope.Kind.IMAGE);
      Throwable t = Assert.assertThrows(IllegalArgumentException.class, () -> hasScope.setScope(newScope));
      assertThat(t.getMessage()).isEqualTo("A scope is already set");
    }
  }

  @Test
  void visitWithAllSubscribingMethods() {
    Body body = parse("FROM image\nONBUILD CMD", DockerLexicalGrammar.BODY);
    DockerSymbolVisitor visitor = new DockerSymbolVisitor();
    List<String> visited = new ArrayList<>();

    visitor.register(DockerImage.class, (ctx, tree) -> visited.add("dockerimage_visit"));
    visitor.registerAfter(DockerImage.class, (ctx, tree) -> visited.add("dockerimage_after"));
    visitor.register(OnBuildInstruction.class, (ctx, tree) -> visited.add("onbuild_visit"));
    visitor.registerAfter(OnBuildInstruction.class, (ctx, tree) -> visited.add("onbuild_after"));
    visitor.register(CmdInstruction.class, (ctx, tree) -> visited.add("cmd_visit"));
    visitor.registerAfter(CmdInstruction.class, (ctx, tree) -> visited.add("cmd_after"));
    visitor.scan(inputFileContext, body);

    assertThat(visited).containsExactly("dockerimage_visit", "onbuild_visit", "cmd_visit", "cmd_after", "onbuild_after", "dockerimage_after");
  }

  private Body scanBody(String code) {
    Body body = parse(code, DockerLexicalGrammar.BODY);
    DockerSymbolVisitor visitor = new DockerSymbolVisitor();
    visitor.scan(inputFileContext, body);
    return body;
  }

  private DockerImage scanImage(String code) {
    DockerImage image = parse(IMAGE_PREFIX + code, DockerLexicalGrammar.DOCKERIMAGE);
    DockerSymbolVisitor visitor = new DockerSymbolVisitor();
    visitor.scan(inputFileContext, image);
    return image;
  }

}
