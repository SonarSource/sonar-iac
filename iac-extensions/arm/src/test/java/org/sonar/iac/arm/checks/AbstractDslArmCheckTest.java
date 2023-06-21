/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.symbols.ResourceSymbol;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.testing.Verifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class AbstractDslArmCheckTest {

  final static ArmParser PARSER = new ArmParser();
  final Verifier.TestContext ctx = new Verifier.TestContext(null);

  @Test
  void provideResourceSymbolWhenTypeExists() {
    Consumer consumer = mock(Consumer.class);
    AbstractDslArmCheck check = new AbstractDslArmCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("testType", consumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType"));
    verify(consumer, times(1)).accept(any(ResourceSymbol.class));
  }

  @Test
  void provideMultipleResourceSymbolsOfSameType() {
    Consumer consumer = mock(Consumer.class);
    AbstractDslArmCheck check = new AbstractDslArmCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("testType", consumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType", "testType"));
    verify(consumer, times(2)).accept(any(ResourceSymbol.class));
  }

  @Test
  void provideMultipleResourceSymbolsOfDifferentTypes() {
    Consumer consumer = mock(Consumer.class);
    AbstractDslArmCheck check = new AbstractDslArmCheck() {
      @Override
      protected void registerResourceConsumer() {
        register(List.of("testType1", "testType2"), consumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType1", "testType2"));
    verify(consumer, times(2)).accept(any(ResourceSymbol.class));
  }

  @Test
  void provideNoSymbolWhenTypeDoesNotExits() {
    Consumer consumer = mock(Consumer.class);
    AbstractDslArmCheck check = new AbstractDslArmCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("anotherType", consumer);
      }
    };
    check.initialize(ctx);
    ctx.scan(parseResources("testType"));
    verify(consumer, never()).accept(any(ResourceSymbol.class));
  }

  @Test
  void provideCorrectChildResource() {
    Consumer consumer = mock(Consumer.class);
    AbstractDslArmCheck check = new AbstractDslArmCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("parentType/childType", consumer);
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

    Tree tree = PARSER.parse(code, null);
    check.initialize(ctx);
    ctx.scan(tree);
    verify(consumer, times(1)).accept(any(ResourceSymbol.class));
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

    return PARSER.parse(sb.toString(), null);
  }

}
