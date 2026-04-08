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
package org.sonar.iac.arm.parser;

import javax.annotation.Nullable;
import org.sonar.iac.arm.plugin.ArmParserStatistics;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.sonar.iac.arm.plugin.ArmSensor.isBicepFile;

public class ArmParser implements TreeParser<Tree> {

  private static final BicepParser bicepParser = BicepParser.create();
  private static final ArmJsonParser jsonParser = new ArmJsonParser();

  private final ArmParserStatistics statistics;

  public ArmParser() {
    this(new ArmParserStatistics());
  }

  public ArmParser(ArmParserStatistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    statistics.recordFileStart(inputFileContext);
    var armTree = doParse(source, inputFileContext);
    statistics.recordFileEnd(inputFileContext);
    return armTree;
  }

  private static ArmTree doParse(String source, @Nullable InputFileContext inputFileContext) {
    if (inputFileContext != null && isBicepFile(inputFileContext)) {
      return bicepParser.parse(source, inputFileContext);
    }
    return jsonParser.parse(source, inputFileContext);
  }
}
