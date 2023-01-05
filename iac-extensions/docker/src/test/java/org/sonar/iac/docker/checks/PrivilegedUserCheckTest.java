/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.checks;

import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.DockerImageTree;
import org.sonar.iac.docker.tree.api.FromTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.impl.DockerImageTreeImpl;
import org.sonar.iac.docker.tree.impl.FromTreeImpl;
import org.sonar.iac.docker.tree.impl.ImageTreeImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

class PrivilegedUserCheckTest {

  private final PrivilegedUserCheck check = new PrivilegedUserCheck();

  @Test
  void testNonCompliant() {
    for (int i = 0; i < 10; i++) {
      DockerVerifier.verify("PrivilegedUserCheck/Dockerfile." + i, check);
    }
  }

  @Test
  void testCompliant() {
    for (int i = 0; i < 7; i++) {
      DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile-Compliant." + i, check);
    }
  }

  @Test
  void testCustomSafeList() {
    check.safeImages = "custom_image1, custom_image2, golang";
    for (int i = 0; i < 3; i++) {
      DockerVerifier.verify("PrivilegedUserCheck/Dockerfile_customSafeImages." + i, check);
    }
  }

  @Test
  void testCustomSafeListCompliant() {
    check.safeImages = "custom_image1, custom_image2, golang";
    for (int i = 0; i < 7; i++) {
      DockerVerifier.verifyNoIssue("PrivilegedUserCheck/Dockerfile_customSafeImages-Compliant." + i, check);
    }
  }

  @Test
  void testMultiStageBuild() {
    DockerVerifier.verify("PrivilegedUserCheck/Dockerfile_multi_stage_build", check);
  }

  @Test
  void shouldRiseIssueWhenFileTreeParentIsNull() {
    CheckContext ctx = Mockito.mock(CheckContext.class);
    SyntaxToken imageName = new SyntaxTokenImpl("scratch", TextRanges.range(1,5, "scratch"), List.of());
    SyntaxToken fromToken = new SyntaxTokenImpl("FROM", TextRanges.range(1,0, "FROM"), List.of());
    FromTree from = new FromTreeImpl(fromToken, null, new ImageTreeImpl(imageName, null, null), null);
    DockerImageTree tree = new DockerImageTreeImpl(from, List.of());

    check.handle(ctx, tree);

    Mockito.verify(ctx).reportIssue(from, "Scratch images run as root by default. Make sure it is safe here.");
  }
}
