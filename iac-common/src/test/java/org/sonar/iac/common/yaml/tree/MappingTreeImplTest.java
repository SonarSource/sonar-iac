/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.yaml.tree;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.common.yaml.YamlTreeTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class MappingTreeImplTest extends YamlTreeTest {

  @Test
  void shouldParseSimpleMapping() {
    MappingTree tree = parse("a: b", MappingTree.class);
    assertThat(tree.elements()).hasSize(1);
    assertThat(tree.children()).hasSize(1);
    assertThat(tree.properties()).hasSize(1);
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:map");
    assertThat(tree.elements().get(0)).isInstanceOf(TupleTree.class);
    assertThat(tree.comments()).isEmpty();
    assertThat(tree.textRange()).hasRange(1, 0, 1, 4);
    assertThat(tree.toHighlight()).hasRange(1, 3, 1, 4);
  }

  @Test
  void shouldParseFileComment() {
    MappingTree tree = parse("# comment", MappingTree.class);
    assertThat(tree.elements()).isEmpty();
    assertThat(tree.children()).isEmpty();
    assertThat(tree.properties()).isEmpty();
    assertThat(tree.metadata().tag()).isEqualTo("tag:yaml.org,2002:comment");
    assertThat(tree.comments()).hasSize(1);
    assertThat(tree.textRange()).hasRange(1, 0, 1, 0);
    assertThat(tree.toHighlight()).hasRange(1, 0, 1, 0);
  }
}
