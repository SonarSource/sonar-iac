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
package org.sonar.iac.kubernetes.checks;

import java.io.File;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.sonar.iac.common.testing.AbstractCheckListTest;

import static org.apache.commons.io.filefilter.FileFilterUtils.and;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.prefixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import static org.assertj.core.api.Assertions.assertThat;

class KubernetesCheckListTest extends AbstractCheckListTest {

  @Override
  protected List<Class<?>> checks() {
    return KubernetesCheckList.checks();
  }

  @Override
  protected File checkClassDir() {
    return new File("src/main/java/org/sonar/iac/kubernetes/checks/");
  }

  @Override
  protected void count() {
    IOFileFilter filter = and(suffixFileFilter("Check.java"), notFileFilter(prefixFileFilter("Abstract")));
    Collection<File> files = FileUtils.listFiles(checkClassDir(), filter, trueFileFilter());
    Collection<File> generalFiles = FileUtils.listFiles(new File("src/general/java/org/sonar/iac/kubernetes/checks/"), filter, trueFileFilter());
    files.addAll(generalFiles);
    // We can increase the files size by 2 because the ParsingErrorCheck and ToDoCommentCheck are located in iac-commons
    int checksSize = files.size() + 2;
    assertThat(checks()).hasSize(checksSize);
  }
}
