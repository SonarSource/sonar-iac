/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.tree.impl.MappingTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.ScalarTreeImpl;
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

  @ParameterizedTest
  @CsvSource({
    "UnknownType, false",
    "AWS::S3::Bucket, true",
  })
  void test_isS3Bucket(String type, boolean isBucket) {
    ScalarTree typeScalar = new ScalarTreeImpl(type, null, null, null, Collections.emptyList());
    AbstractResourceCheck.Resource resource = new AbstractResourceCheck.Resource(typeScalar, null);
    assertThat(AbstractResourceCheck.isS3Bucket(resource)).isEqualTo(isBucket);
  }

  @Test
  void test_isS3Bucket_without_scalar() {
    AbstractResourceCheck.Resource resource = new AbstractResourceCheck.Resource(null, null);
    assertThat(AbstractResourceCheck.isS3Bucket(resource)).isFalse();

    MappingTree type = new MappingTreeImpl(Collections.emptyList(), null, null, Collections.emptyList());
    resource = new AbstractResourceCheck.Resource(type, null);
    assertThat(AbstractResourceCheck.isS3Bucket(resource)).isFalse();
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
