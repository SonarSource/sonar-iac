/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.checks;

import java.io.File;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.jupiter.api.Test;
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
  protected boolean hasTodoCommentCheck() {
    // There is a custom implementation for Kubernetes/Helm
    return false;
  }

  @Override
  @Test
  protected void count() {
    IOFileFilter filter = and(suffixFileFilter("Check.java"), notFileFilter(prefixFileFilter("Abstract")));
    Collection<File> files = FileUtils.listFiles(checkClassDir(), filter, trueFileFilter());
    Collection<File> commonFiles = FileUtils.listFiles(new File("src/common/java/org/sonar/iac/kubernetes/checks/"), filter, trueFileFilter());
    files.addAll(commonFiles);
    // We can increase the files size by 1 because the ParsingErrorCheck is located in iac-commons
    int checksSize = files.size() + 1;
    assertThat(checks()).hasSize(checksSize);
  }
}
