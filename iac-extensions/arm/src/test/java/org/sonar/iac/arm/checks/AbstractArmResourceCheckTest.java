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
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.testing.Verifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class AbstractArmResourceCheckTest {

  final static ArmParser parser = new ArmParser();
  final static Verifier.TestContext ctx = new Verifier.TestContext(null);

  final Consumer<ContextualResource> contextualConsumer = mock(Consumer.class);
  final BiConsumer<CheckContext, ResourceDeclaration> treeConsumer = mock(BiConsumer.class);

  @Test
  void shouldProvideResourceSymbolWhenTypeExists() {
    AbstractArmResourceCheck check = new AbstractArmResourceCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("testType", contextualConsumer);
        register("testType", treeConsumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType"));

    verify(contextualConsumer, times(1)).accept(any(ContextualResource.class));
    verify(treeConsumer, times(1)).accept(any(CheckContext.class), any(ResourceDeclaration.class));
  }

  @Test
  void shouldProvideMultipleResourceSymbolsOfSameType() {
    AbstractArmResourceCheck check = new AbstractArmResourceCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("testType", contextualConsumer);
        register("testType", treeConsumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType", "testType"));

    verify(contextualConsumer, times(2)).accept(any(ContextualResource.class));
    verify(treeConsumer, times(2)).accept(any(CheckContext.class), any(ResourceDeclaration.class));
  }

  @Test
  void shouldProvideMultipleResourceSymbolsOfDifferentTypes() {
    AbstractArmResourceCheck check = new AbstractArmResourceCheck() {
      @Override
      protected void registerResourceConsumer() {
        register(List.of("testType1", "testType2"), contextualConsumer);
        register(List.of("testType1", "testType2"), treeConsumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType1", "testType2"));

    verify(contextualConsumer, times(2)).accept(any(ContextualResource.class));
    verify(treeConsumer, times(2)).accept(any(CheckContext.class), any(ResourceDeclaration.class));
  }

  @Test
  void shouldProvideNoSymbolWhenTypeDoesNotExits() {
    AbstractArmResourceCheck check = new AbstractArmResourceCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("anotherType", contextualConsumer);
        register("anotherType", treeConsumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType"));

    verifyNoInteractions(contextualConsumer);
    verifyNoInteractions(treeConsumer);
  }

  @Test
  void shouldProvideCorrectChildResource() {
    AbstractArmResourceCheck check = new AbstractArmResourceCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("parentType/childType", contextualConsumer);
        register("parentType/childType", treeConsumer);
      }
    };

    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"parentType\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": \"myResource\",",
      "      \"resources\": [",
      "         {",
      "           \"type\": \"childType\",",
      "           \"apiVersion\": \"2022-12-29\",",
      "           \"name\": \"myResource\",",
      "         }",
      "      ]",
      "    }",
      "  ]",
      "}");

    Tree tree = parser.parse(code, null);
    check.initialize(ctx);
    ctx.scan(tree);

    verify(contextualConsumer, times(1)).accept(any(ContextualResource.class));
    verify(treeConsumer, times(1)).accept(any(CheckContext.class), any(ResourceDeclaration.class));
  }

  private Tree parseResources(String... resourceTypes) {
    StringBuilder sb = new StringBuilder();
    sb.append(code("{",
      "  \"resources\": ["));
    for (String resourceType : resourceTypes) {
      sb.append(code(
        "{",
        "      \"type\": \"" + resourceType + "\",",
        "      \"apiVersion\": \"2022-12-29\",",
        "      \"name\": \"myResource\"",
        "    },"));
    }
    sb.append(code(
      "  ]",
      "}"));

    return parser.parse(sb.toString(), null);
  }

}
