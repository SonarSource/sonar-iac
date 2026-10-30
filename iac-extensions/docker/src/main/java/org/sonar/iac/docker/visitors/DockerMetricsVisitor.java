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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.NoSonarNextLineResolver;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.docker.plugin.DockerExtension;
import org.sonar.iac.docker.plugin.DockerLanguage;
import org.sonar.iac.docker.tree.api.Instruction;

public class DockerMetricsVisitor extends MetricsVisitor {

  private static final Pattern LINE_SEPARATOR = Pattern.compile("\r\n|\n|\r");

  private NoSonarNextLineResolver noSonarNextLineResolver = new NoSonarNextLineResolver();

  public DockerMetricsVisitor(FileLinesContextFactory fileLinesContextFactory, NoSonarFilter noSonarFilter, SensorTelemetry sensorTelemetry) {
    super(fileLinesContextFactory, noSonarFilter, sensorTelemetry, DockerExtension.REPOSITORY_KEY);
  }

  @Override
  protected void languageSpecificMetrics() {
    super.languageSpecificMetrics();
    register(Instruction.class, (ctx, instruction) -> noSonarNextLineResolver.addElement(instruction.textRange()));
  }

  @Override
  protected void before(InputFileContext ctx, Tree root) {
    super.before(ctx, root);
    noSonarNextLineResolver = new NoSonarNextLineResolver();
  }

  @Override
  protected void after(InputFileContext ctx, Tree root) {
    addNoSonarLinesOfFullLineComments(ctx);
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

  /**
   * A Dockerfile comment is always alone on its line, so the line an issue is reported on can never hold one. Hence, a comment
   * suppressing issues also covers the next line of code, or every line of the instruction that starts on that line.
   * Comment lines are read from the file content, because the comments inside multi-line instructions are not always attached to
   * the tree.
   */
  private void addNoSonarLinesOfFullLineComments(InputFileContext ctx) {
    List<Integer> noSonarCommentLines = new ArrayList<>();
    var lines = LINE_SEPARATOR.split(content(ctx), -1);
    for (var index = 0; index < lines.length; index++) {
      var line = lines[index].strip();
      var lineNumber = index + 1;
      if (line.startsWith("#")) {
        if (line.contains(NOSONAR_PREFIX)) {
          noSonarCommentLines.add(lineNumber);
        }
      } else if (!line.isEmpty()) {
        noSonarNextLineResolver.addCodeLine(lineNumber);
      }
    }
    noSonarLines().addAll(noSonarCommentLines);
    noSonarLines().addAll(noSonarNextLineResolver.coveredLines(noSonarCommentLines));
  }

  private static String content(InputFileContext ctx) {
    try {
      return ctx.inputFile.contents();
    } catch (IOException e) {
      throw new ParseException("Can not read file %s for metric calculation".formatted(ctx.inputFile), null, e.getMessage());
    }
  }
}
