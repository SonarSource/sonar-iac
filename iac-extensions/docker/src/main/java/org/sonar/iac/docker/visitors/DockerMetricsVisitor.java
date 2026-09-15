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
package org.sonar.iac.docker.visitors;

import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.docker.plugin.DockerExtension;
import org.sonar.iac.docker.plugin.DockerLanguage;

public class DockerMetricsVisitor extends MetricsVisitor {

  public DockerMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetry sensorTelemetry) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetry, DockerExtension.REPOSITORY_KEY);
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    super.after(ctx, root);
    var fileLanguage = ctx.inputFile.language();
    if (fileLanguage == null || fileLanguage.isBlank()) {
      addLoCTelemetry(language + ".noLanguage");
    } else if (DockerLanguage.KEY.equals(fileLanguage)) {
      addLoCTelemetry(language + ".dockerLanguage");
    } else {
      addLoCTelemetry(language + ".otherLanguage");
    }
  }
}
