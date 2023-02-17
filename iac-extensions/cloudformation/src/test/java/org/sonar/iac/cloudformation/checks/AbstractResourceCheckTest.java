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
package org.sonar.iac.cloudformation.checks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.MappingTreeImpl;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractResourceCheckTest {

  private static final YamlTreeMetadata METADATA = new YamlTreeMetadata(null, null, Collections.emptyList());

  @Test
  void checkResource_is_called_on_every_resource() {
    Check check = new Check();
    CloudformationVerifier.verifyNoIssue("AbstractResourceCheck/test.yaml", check);

    assertThat(check.foundResources).hasSize(4);
    assertType(check.foundResources.get(0), "type1");
    assertType(check.foundResources.get(1), "type2");
    assertType(check.foundResources.get(2), "type3");
    assertThat(check.foundResources.get(3).properties()).isNull();
  }

  @ParameterizedTest
  @CsvSource({
    "UnknownType, false",
    "AWS::S3::Bucket, true",
  })
  void test_isS3Bucket(String type, boolean isBucket) {
    ScalarTree typeScalar = new ScalarTreeImpl(type, null, METADATA);
    AbstractResourceCheck.Resource resource = new AbstractResourceCheck.Resource(null, typeScalar, null);
    assertThat(AbstractResourceCheck.isS3Bucket(resource)).isEqualTo(isBucket);
  }

  @Test
  void test_isS3Bucket_without_scalar() {
    AbstractResourceCheck.Resource resource = new AbstractResourceCheck.Resource(null, null, null);
    assertThat(AbstractResourceCheck.isS3Bucket(resource)).isFalse();

    MappingTree type = new MappingTreeImpl(Collections.emptyList(), METADATA);
    resource = new AbstractResourceCheck.Resource(null, type, null);
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
