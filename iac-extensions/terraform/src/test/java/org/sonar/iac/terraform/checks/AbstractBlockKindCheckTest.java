/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class AbstractBlockKindCheckTest {

  @Test
  void dispatchesAllKindsAndSupportsTheirLabelShapes() {
    var check = new TestAbstractBlockKindCheck();
    TerraformVerifier.verify("BlockKindCheck/blocks.tf", check);

    assertThat(check.visitedBlocks).extracting(block -> block.name)
      .containsExactly("variable", "locals", "module", "provider", "output", "terraform");
    assertThat(check.visitedBlocks).extracting(block -> block.tree.labels().size())
      .containsExactly(1, 0, 1, 1, 1, 0);
    assertThat(check.visitedBlocks).allSatisfy(block -> {
      assertThat(block.parent).isNull();
      assertThat(AbstractNewResourceCheck.resourceType(block.tree))
        .isEqualTo(AbstractResourceCheck.getResourceType(block.tree));
      assertThat(AbstractResourceCheck.hasReferenceLabel(block.tree)).isFalse();
      assertThat(block.block("missing").isAbsent()).isTrue();
      assertThat(block.blocks("nested")).hasSize(1);
    });
    assertThat(check.visitedBlocks).extracting(block -> AbstractNewResourceCheck.resourceType(block.tree))
      .containsExactly("example", null, "example", "example", "example", null);
  }

  @Test
  void existingResourceChecksIgnoreNonResourceBlocks() {
    var legacyCheck = new AbstractResourceCheckTest.TestAbstractResourceCheck();
    TerraformVerifier.verifyNoIssue("BlockKindCheck/blocks.tf", legacyCheck);
    assertThat(legacyCheck.visitedBlocks).extracting(block -> block.key().value())
      .containsExactlyInAnyOrder("resource", "ephemeral");

    var visited = new ArrayList<String>();
    var resourceCheck = new AbstractNewResourceCheck() {
      @Override
      protected void registerResourceConsumer() {
        register("example", resource -> visited.add(resource.tree.key().value()));
      }
    };
    TerraformVerifier.verifyNoIssue("BlockKindCheck/blocks.tf", resourceCheck);
    assertThat(visited).containsExactly("resource", "ephemeral");
  }

  @Test
  void registersBlockTreeOnceForAllKinds() {
    var init = mock(InitContext.class);
    new TestAbstractBlockKindCheck().initialize(init);

    verify(init).register(eq(BlockTree.class), any());
    verifyNoMoreInteractions(init);
  }

  @Test
  void supportsMultipleConsumersAndIgnoresUnregisteredKinds() {
    var visited = new ArrayList<String>();
    var check = new AbstractBlockKindCheck() {
      @Override
      protected void registerBlockConsumers() {
        register(BlockKind.OUTPUT, block -> visited.add("first"));
        register(BlockKind.OUTPUT, block -> visited.add("second"));
      }
    };

    TerraformVerifier.verifyNoIssue("BlockKindCheck/blocks.tf", check);
    assertThat(visited).containsExactly("first", "second");
  }

  static class TestAbstractBlockKindCheck extends AbstractBlockKindCheck {

    private final List<BlockSymbol> visitedBlocks = new ArrayList<>();

    @Override
    protected void registerBlockConsumers() {
      for (var kind : BlockKind.values()) {
        register(kind, block -> {
          visitedBlocks.add(block);
          block.attribute("expected").reportIfAbsent("Missing expected attribute");
          block.block("nested").attribute("expected").reportIfAbsent("Missing nested attribute");
        });
      }
    }
  }
}
