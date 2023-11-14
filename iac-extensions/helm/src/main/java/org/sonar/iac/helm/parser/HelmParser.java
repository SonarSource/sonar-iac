package org.sonar.iac.helm.parser;

import com.sun.jna.Native;
import javax.annotation.Nullable;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.jna.Example;
import org.sonar.iac.helm.jna.library.Template;
import org.sonar.iac.helm.jna.mapping.GoString;

public class HelmParser extends YamlParser {
  private final Template templateLib;
  private final String valuesFilePath = Thread.currentThread().getContextClassLoader().getResource("values.yaml").getPath();

  public HelmParser() {
    super();

    this.templateLib = Native.loadLibrary(
      Example.class.getResource("/golang-template").getPath(),
      Template.class);
  }

  @Override
  public FileTree parse(String source, @Nullable InputFileContext inputFileContext) {
    var templateId = loadGoTemplate(source);
//    source = renderGoTemplate(templateId);
//    System.out.println("source after rendering:\n" + source);

    var fileTree = super.parse(source, inputFileContext);

    return fileTree;
  }

  private String renderGoTemplate(long templateId) {
    var rendered = templateLib.ExecuteWithValues(templateId, new GoString.ByValue(valuesFilePath));
    System.out.println("rendered:\n" + rendered);
    return rendered;
  }
  private long loadGoTemplate(String template) {
    System.out.println("Processing template: " + template);
    var templateId = templateLib.NewHandleID(new GoString.ByValue("gotpl"), new GoString.ByValue(template));
    System.out.println(templateLib.PrintTree(templateId));
//    templateLib.Tree(templateId);
    return templateId;
  }
}
