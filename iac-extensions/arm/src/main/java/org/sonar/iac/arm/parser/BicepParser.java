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
package org.sonar.iac.arm.parser;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.arm.parser.bicep.BicepGrammar;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.bicep.BicepNodeBuilder;
import org.sonar.iac.arm.parser.bicep.TreeFactory;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class BicepParser extends ActionParser<ArmTree> implements TreeParser<Tree> {

  protected BicepParser(BicepNodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    super(StandardCharsets.UTF_8,
      BicepLexicalGrammar.createGrammarBuilder(),
      BicepGrammar.class,
      new TreeFactory(),
      nodeBuilder,
      rootRule);
  }

  public static BicepParser create() {
    return create(BicepLexicalGrammar.FILE);
  }

  public static BicepParser create(GrammarRuleKey rootRule) {
    try {
      return new BicepParser(new BicepNodeBuilder(), rootRule);
    } catch (RuntimeException exception) {
      if (exception.getCause() instanceof InvocationTargetException) {
        throw new GrammarException("Please make sure that all methods in your grammar are public", exception);
      } else {
        throw exception;
      }
    }
  }

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    try {
      return super.parse(source);
    } catch (RecognitionException recognitionException) {
      InputFile inputFile = inputFileContext != null ? inputFileContext.inputFile : null;
      throw ParseException.createGeneralParseException("parse", inputFile, recognitionException, new BasicTextPointer(recognitionException.getLine(), 0));
    }
  }

}
