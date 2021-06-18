/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.common.api.checks.CheckContext;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractResourceCheckTest {

  @Test
  void checkResource_is_called_on_every_resource() {
    Check check = new Check();

    CloudformationVerifier.verifyNoIssue("AbstractResourceCheck/test.yaml", check);

    assertThat(check.foundResources).hasSize(4);
    assertType(check.foundResources.get(0), "type1");
    assertType(check.foundResources.get(1), "type2");
    assertType(check.foundResources.get(2), "type3");
    assertThat(check.foundResources.get(3).type()).isNull();
  }

  private void assertType(AbstractResourceCheck.Resource resource, String type) {
    assertThat(resource.type()).isInstanceOfSatisfying(ScalarTree.class, s -> assertThat(s.value()).isEqualTo(type));
  }

  private static class Check extends AbstractResourceCheck {
    List<Resource> foundResources = new ArrayList<>();

    @Override
    protected void checkResource(CheckContext ctx, Resource resource) {
      foundResources.add(resource);
    }
  }
}
