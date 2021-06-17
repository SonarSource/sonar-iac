/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public class CloudformationParser implements TreeParser<Tree> {

  @Override
  public FileTree parse(String source, @Nullable InputFileContext inputFileContext) {
    LoadSettings settings = LoadSettings.builder().setParseComments(shouldParseComments(inputFileContext)).build();
    StreamReader reader = new StreamReader(settings, source);
    ScannerImpl scanner = new ScannerImpl(settings, reader);
    Parser parser = new ParserImpl(settings, scanner);
    Composer composer = new Composer(settings, parser);
    List<Node> nodes = composerNodes(composer);

    return CloudformationConverter.convertFile(nodes);
  }

  private static List<Node> composerNodes(Composer composer) {
    List<Node> nodes = new ArrayList<>();
    while (composer.hasNext()) {
      nodes.add(composer.next());
    }
    return nodes;
  }

  private static boolean shouldParseComments(@Nullable InputFileContext inputFileContext) {
    return inputFileContext == null || !inputFileContext.inputFile.filename().toLowerCase(Locale.ROOT).endsWith(".json");
  }
}
