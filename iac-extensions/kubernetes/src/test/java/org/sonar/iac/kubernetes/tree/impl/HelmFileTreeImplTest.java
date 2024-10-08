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
package org.sonar.iac.kubernetes.tree.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;
import org.sonar.iac.helm.tree.api.GoTemplateTree;

import static org.assertj.core.api.Assertions.assertThat;

class HelmFileTreeImplTest {

  @Test
  void shouldReturnAllChildren() {

    var metadata = Mockito.mock(YamlTreeMetadata.class);
    var tree1 = Mockito.mock(YamlTree.class);
    var documents = List.of(tree1);
    var templateTree = Mockito.mock(GoTemplateTree.class);

    var helmFileTree = new HelmFileTreeImpl(documents, metadata, templateTree);

    var actual = helmFileTree.children();
    assertThat(actual).containsExactly(tree1, templateTree);
  }
}
