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
package org.sonar.iac.arm.parser;

import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlConverter;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.ScalarTreeImpl;
import org.sonar.iac.common.yaml.tree.YamlTree;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.sonar.iac.common.extension.ParseException.createGeneralParseException;

public class ArmJsonParser implements TreeParser<Tree> {

  private final YamlParser yamlParser = new YamlParser(new ArmJsonConverter());

  @Nullable
  private InputFileContext inputFileContext;
  private String source;

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
    ArmTree file = convert(parseJson(source));
    setParents(file);
    return file;
  }

  protected FileTree parseJson(String source) {
    this.source = source;
    InputFile inputFile = inputFileContext != null ? inputFileContext.inputFile : null;
    try {
      return yamlParser.parse(source, inputFileContext);
    } catch (ScannerException e) {
      TextPointer position = e.getContextMark()
        .map(mark -> new BasicTextPointer(mark.getLine() + 1, mark.getColumn()))
        .orElse(null);
      throw createGeneralParseException("parse", inputFile, e, position);
    } catch (Exception e) {
      throw createGeneralParseException("parse", inputFile, e, null);
    }
  }

  private ArmTree convert(FileTree fileTree) {
    FileConverter fileConverter = new FileConverter(inputFileContext);
    return fileConverter.convertFile(fileTree);
  }

  private static void setParents(ArmTree tree) {
    for (Tree children : tree.children()) {
      ArmTree child = (ArmTree) children;
      child.setParent(tree);
      setParents(child);
    }
  }

  class ArmJsonConverter extends YamlConverter {

    @Override
    public YamlTree convertScalar(ScalarNode node) {
      var metadata = YamlTreeMetadata.builder().fromNode(node).build();
      var value = node.getValue();
      if (isMultilineScalar(metadata)) {
        value = originalStringValue(metadata);
      }
      return new ScalarTreeImpl(value, scalarStyleConvert(node.getScalarStyle()), metadata);
    }

    private String originalStringValue(YamlTreeMetadata metadata) {
      var effectiveBeginIdx = source.offsetByCodePoints(0, metadata.startPointer() + 1);
      var effectiveEndIdx = source.offsetByCodePoints(effectiveBeginIdx, metadata.endPointer() - 1 - metadata.startPointer() - 1);
      return source.substring(effectiveBeginIdx, effectiveEndIdx);
    }

    private static boolean isMultilineScalar(YamlTreeMetadata metadata) {
      var range = metadata.textRange();
      return range.start().line() != range.end().line();
    }
  }
}
