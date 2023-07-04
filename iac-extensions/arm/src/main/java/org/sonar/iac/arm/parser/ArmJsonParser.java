package org.sonar.iac.arm.parser;

import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.BasicTextPointer;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;

import static org.sonar.iac.common.extension.ParseException.createGeneralParseException;

public class ArmJsonParser implements TreeParser<Tree>  {

  @Nullable
  private InputFileContext inputFileContext;

  @Override
  public ArmTree parse(String source, @Nullable InputFileContext inputFileContext) {
    this.inputFileContext = inputFileContext;
    ArmTree file = convert(parseJson(source));
    setParents(file);
    return file;
  }

  private FileTree parseJson(String source) {
    YamlParser yamlParser = new YamlParser();
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
}
