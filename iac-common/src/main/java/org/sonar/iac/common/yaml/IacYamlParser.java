/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public interface IacYamlParser<T extends Tree> extends TreeParser<T> {

  T convert(List<Node> nodes);

  default T parse(String source, @Nullable InputFileContext inputFileContext) {
    var settings = LoadSettings.builder().setParseComments(shouldParseComments(inputFileContext)).build();
    var reader = new StreamReader(settings, source);
    var scanner = new ScannerImpl(settings, reader);
    var parser = new ParserImpl(settings, scanner);
    var composer = new Composer(settings, parser);
    var nodes = composerNodes(composer);

    return convert(nodes);
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
