/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
      return parse(source);
    } catch (RecognitionException recognitionException) {
      InputFile inputFile = inputFileContext != null ? inputFileContext.inputFile : null;
      throw ParseException.createGeneralParseException("parse", inputFile, recognitionException, new BasicTextPointer(recognitionException.getLine(), 0));
    }
  }

  @Override
  public ArmTree parse(String source) {
    ArmTree tree = super.parse(source);
    setParents(tree);
    return tree;
  }

  private static void setParents(ArmTree tree) {
    for (Tree children : tree.children()) {
      ArmTree child = (ArmTree) children;
      child.setParent(tree);
      setParents(child);
    }
  }

}
