/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonar.iac.common.extension.IacSensor;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.Tree;

public class CloudformationSensor extends IacSensor {

  private final Checks<IacCheck> checks;

  public CloudformationSensor(FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory, NoSonarFilter noSonarFilter, CloudformationLanguage language) {
    super(fileLinesContextFactory, noSonarFilter, language);
    checks = checkFactory.create(CloudformationExtension.REPOSITORY_KEY);
    checks.addAnnotatedChecks((Iterable<?>) CloudformationCheckList.checks());
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return source -> {
      throw new RuntimeException("Not implemented");
    };
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  protected Checks<IacCheck> checks() {
    return checks;
  }
}
