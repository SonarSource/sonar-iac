/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.checks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.impl.utils.DefaultTempFolder;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.HasComments;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.Tuple;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.HelmFileSystem;
import org.sonar.iac.kubernetes.plugin.HelmParser;
import org.sonar.iac.kubernetes.plugin.HelmProcessor;
import org.sonar.iac.kubernetes.plugin.KubernetesAnalyzer;
import org.sonar.iac.kubernetes.plugin.KubernetesExtension;
import org.sonar.iac.kubernetes.plugin.KubernetesLanguage;
import org.sonar.iac.kubernetes.plugin.KubernetesParserStatistics;
import org.sonar.iac.kubernetes.tree.impl.HelmFileTreeImpl;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;
import org.sonar.iac.kubernetes.visitors.ProjectContext;
import org.sonar.iac.kubernetes.visitors.ProjectContextEnricherVisitor;
import org.sonar.iac.kubernetes.visitors.SecondaryLocationLocator;
import org.sonarsource.analyzer.commons.checks.verifier.MultiFileVerifier;

import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;
import static org.sonar.iac.common.testing.Verifier.contentToTmp;

public class KubernetesVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesVerifier.class);
  public static final Path BASE_DIR = Paths.get("build", "resources", "test", "checks");
  public static final String TMP_CONTENT_FILE_NAME = "temp-file-for-k8s-verify-content.yaml";
  private static final SensorContextTester SENSOR_CONTEXT = SensorContextTester.create(BASE_DIR.toAbsolutePath());
  private static final KubernetesAnalyzer KUBERNETES_ANALYZER = initializeKubernetesAnalyzer();
  private static final TreeParser<Tree> PARSER = KUBERNETES_ANALYZER::parse;
  private static final TreeParser<Tree> GO_PARSER = (String content, InputFileContext inputFileContext) -> {
    var yamlTree = PARSER.parse(content, inputFileContext);
    return ((HelmFileTreeImpl) yamlTree).getGoTemplateAst();
  };

  private static TreeParser<? extends Tree> parserFor(IacCheck check) {
    if (check instanceof ChecksGoTemplate) {
      return GO_PARSER;
    }
    return PARSER;
  }

  public static void verify(String templateFileName, IacCheck check, String... fileNames) {
    var initialization = initializeVerification(templateFileName, fileNames);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    Verifier.verify(parserFor(check), inputFileContext, check,
      multiFileVerifier -> {
        // Prepare project context inside this lambda so that it happens after parsing. HelmInputFileContext
        // is fully initialized during parsing (i.e. additional files are discovered and added).
        var projectContext = prepareProjectContext(inputFileContext, fileNames);
        return new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext);
      },
      commentsVisitor);
  }

  public static void verify(String templateFileName, IacCheck check, List<Verifier.Issue> expectedIssues, String... fileNames) {
    var initialization = initializeVerification(templateFileName, fileNames);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    Verifier.verify(parserFor(check), inputFileContext, check,
      multiFileVerifier -> {
        var projectContext = prepareProjectContext(inputFileContext, fileNames);
        return new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext);
      },
      commentsVisitor, expectedIssues);
  }

  public static void verify(String templateFileName, IacCheck check, List<Verifier.Issue> expectedIssues) {
    var initialization = initializeVerification(templateFileName);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    Verifier.verify(parserFor(check), inputFileContext, check,
      multiFileVerifier -> {
        var projectContext = prepareProjectContext(inputFileContext);
        return new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext);
      },
      commentsVisitor, expectedIssues);
  }

  public static void verifyContent(String content, String basePath, IacCheck check, String... fileNames) {
    var relativePath = createFileInBaseDir(basePath, content);
    verify(relativePath, check, fileNames);
  }

  public static void verifyContentNoIssue(String content, String basePath, IacCheck check, String... fileNames) {
    var relativePath = createFileInBaseDir(basePath, content);
    verifyNoIssue(relativePath, check, fileNames);
  }

  /**
   * Only usable for single pure Kubernetes files
   */
  public static void verifyContent(String content, IacCheck check) {
    var tempFile = contentToTmp(content);
    var inputFileContext = new HelmInputFileContext(SENSOR_CONTEXT, inputFile(tempFile.getName(), tempFile.getParentFile().toPath(),
      KubernetesLanguage.NAME));
    Verifier.verify(parserFor(check), tempFile.toPath(), check, multiFileVerifier -> new KubernetesTestContext(multiFileVerifier, inputFileContext,
      new ProjectContext()));
  }

  public static void verifyNoIssue(String templateFileName, IacCheck check, String... fileNames) {
    var initialization = initializeVerification(templateFileName, fileNames);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    Verifier.verifyNoIssue(parserFor(check), inputFileContext, check,
      multiFileVerifier -> {
        var projectContext = prepareProjectContext(inputFileContext, fileNames);
        return new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext);
      },
      commentsVisitor);
  }

  private static Tuple<InputFileContext, BiConsumer<Tree, Map<Integer, Set<Comment>>>> initializeVerification(String templateFileName,
    String... fileNames) {
    if (containsHelmContent(templateFileName)) {
      if (fileNames.length > 0) {
        throw new IllegalArgumentException("For Helm projects, all project files will be discovered. Explicit input is not required.");
      }
      var inputFileContext = HelmVerifier.prepareHelmContext(templateFileName);
      return new Tuple<>(inputFileContext, HelmVerifier.commentsWithShiftedTextRangeVisitor(inputFileContext));
    } else {
      var inputFileContext = new InputFileContext(SENSOR_CONTEXT, inputFile(templateFileName, BASE_DIR));
      return new Tuple<>(inputFileContext, Verifier.commentsVisitor());
    }
  }

  private static boolean containsHelmContent(String templateFileName) {
    try {
      var content = Files.readString(BASE_DIR.resolve(templateFileName));
      return KubernetesAnalyzer.hasHelmContent(content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static KubernetesAnalyzer initializeKubernetesAnalyzer() {
    File temporaryDirectory;
    try {
      temporaryDirectory = Files.createTempDirectory("kubernetesVerifierExecutable").toFile();
    } catch (IOException e) {
      throw new IllegalStateException("Could not create temporary directory", e);
    }
    HelmEvaluator helmEvaluator = new HelmEvaluator(new DefaultTempFolder(temporaryDirectory, false));
    HelmFileSystem helmFileSystem = new HelmFileSystem(SENSOR_CONTEXT.fileSystem());
    HelmProcessor helmProcessor = new HelmProcessor(helmEvaluator, helmFileSystem);
    helmProcessor.initialize();
    HelmParser helmParser = new HelmParser(helmProcessor);
    temporaryDirectory.deleteOnExit();

    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();

    var durationStatistics = new DurationStatistics(SENSOR_CONTEXT.config());

    return new KubernetesAnalyzer(
      KubernetesExtension.REPOSITORY_KEY,
      new YamlParser(),
      visitors,
      durationStatistics,
      helmParser,
      new KubernetesParserStatistics(),
      new TreeVisitor<>());
  }

  private static ProjectContext prepareProjectContext(InputFileContext inputFileContext, String... additionalFiles) {
    var projectContext = new ProjectContext();
    var projectContextEnricherVisitor = new ProjectContextEnricherVisitor(projectContext);

    Stream<InputFile> additionalHelmProjectFiles = Stream.empty();
    if (inputFileContext instanceof HelmInputFileContext helmCtx) {
      additionalHelmProjectFiles = helmCtx.getAdditionalFiles().values().stream();
    }
    Stream.concat(
      additionalHelmProjectFiles,
      Arrays.stream(additionalFiles).map(fileName -> inputFile(fileName, BASE_DIR)))
      .forEach(additionalFile -> {
        String additionalContent = retrieveContent(additionalFile);
        InputFileContext additionalInputFileContext;
        if (KubernetesAnalyzer.hasHelmContent(additionalContent)) {
          additionalInputFileContext = new HelmInputFileContext(SENSOR_CONTEXT, additionalFile);
        } else {
          additionalInputFileContext = new InputFileContext(SENSOR_CONTEXT, additionalFile);
        }
        var tree = PARSER.parse(additionalContent, additionalInputFileContext);
        projectContextEnricherVisitor.scan(additionalInputFileContext, tree);
      });

    return projectContext;
  }

  private static String retrieveContent(InputFile inputFile) {
    try {
      return inputFile.contents();
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to read content of %s", inputFile), e);
    }
  }

  private static String createFileInBaseDir(String basePath, String content) {
    var tempPath = Path.of(basePath).resolve(TMP_CONTENT_FILE_NAME);
    try {
      Files.writeString(BASE_DIR.resolve(tempPath), content, StandardCharsets.UTF_8);
      return tempPath.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static class HelmVerifier {
    private static HelmInputFileContext prepareHelmContext(String templateFileName) {
      var sourceInputFile = inputFile(templateFileName, BASE_DIR);
      SENSOR_CONTEXT.fileSystem().add(sourceInputFile);
      var filePath = Path.of(sourceInputFile.uri());
      var helmProjectPath = HelmFileSystem.retrieveHelmProjectFolder(filePath, SENSOR_CONTEXT.fileSystem());
      if (helmProjectPath == null) {
        throw new IllegalStateException(String.format("Could not resolve helmProjectPath for file %s, possible missing Chart.yaml",
          filePath));
      }
      addDependentFilesToSensorContext(helmProjectPath);
      return new HelmInputFileContext(SENSOR_CONTEXT, sourceInputFile);
    }

    public static void addDependentFilesToSensorContext(Path helmProjectPath) {
      try (Stream<Path> pathStream = FileUtils.streamFiles(helmProjectPath.toFile(), true, (String[]) null).map(File::toPath)) {
        pathStream
          .filter(path -> path.toFile().isFile())
          .forEach(path -> addFileToSensorContext(SENSOR_CONTEXT, BASE_DIR, path.toString()));
      } catch (IOException e) {
        LOG.error("Error while trying to add dependent files to sensor context", e);
      }
    }

    private static BiConsumer<Tree, Map<Integer, Set<Comment>>> commentsWithShiftedTextRangeVisitor(HelmInputFileContext inputFileContext) {
      Set<TextRange> alreadyAdded = new HashSet<>();
      return (root, commentsByLine) -> (new TreeVisitor<>()).register(Tree.class,
        (ctx, tree) -> {
          // The shifted location is not precise enough and always returns the whole line, so it is not suitable for detecting already
          // added tree's.
          // The unshifted location is more granular and therefore can be used for detecting duplicates, even if it's not the real location
          // in the source file.
          if (tree instanceof HasComments && !alreadyAdded.contains(tree.textRange())) {
            for (Comment comment : ((HasComments) tree).comments()) {
              Comment shiftedComment = new CommentImpl(comment.value(), comment.contentText(),
                LocationShifter.computeShiftedLocation(inputFileContext, comment.textRange()));
              commentsByLine.computeIfAbsent(shiftedComment.textRange().start().line(), i -> new HashSet<>()).add(shiftedComment);
            }
            alreadyAdded.add(tree.textRange());
          }
        }).scan(new TreeContext(), root);
    }
  }

  public static class KubernetesTestContext extends Verifier.TestContext implements KubernetesCheckContext {
    private final InputFileContext inputFileContext;
    private final ProjectContext projectContext;
    private boolean shouldReportSecondaryInValues = true;
    private boolean enableLocationShifting = true;

    public KubernetesTestContext(MultiFileVerifier verifier, InputFileContext inputFileContext, ProjectContext projectContext) {
      super(verifier);
      this.inputFileContext = inputFileContext;
      this.projectContext = projectContext;
    }

    @Override
    public InputFileContext inputFileContext() {
      return inputFileContext;
    }

    @Override
    public ProjectContext projectContext() {
      return projectContext;
    }

    @Override
    protected void reportIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      if (enableLocationShifting && inputFileContext instanceof HelmInputFileContext helmCtx) {
        var shiftedTextRange = LocationShifter.shiftLocation(helmCtx, textRange);

        List<SecondaryLocation> allSecondaryLocations = new ArrayList<>();
        if (shouldReportSecondaryInValues) {
          allSecondaryLocations = SecondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(helmCtx, shiftedTextRange);
        }
        List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream()
          .map(secondaryLocation -> LocationShifter.computeShiftedSecondaryLocation(computeHelmInputFileContextForSecondaryLocation(secondaryLocation, helmCtx), secondaryLocation))
          .distinct()
          .toList();

        allSecondaryLocations.addAll(shiftedSecondaryLocations);

        super.reportIssue(shiftedTextRange, message, allSecondaryLocations);
      } else {
        super.reportIssue(textRange, message, secondaryLocations);
      }
    }

    private HelmInputFileContext computeHelmInputFileContextForSecondaryLocation(SecondaryLocation secondaryLocation, HelmInputFileContext defaultHelmContext) {
      InputFileContext context = null;
      if (secondaryLocation.filePath != null) {
        context = projectContext.getInputFileContext(secondaryLocation.filePath);
      }
      if (!(context instanceof HelmInputFileContext)) {
        context = defaultHelmContext;
      }
      return (HelmInputFileContext) context;
    }

    @Override
    public boolean shouldReportSecondaryInValues() {
      return shouldReportSecondaryInValues;
    }

    @Override
    public void setShouldReportSecondaryInValues(boolean shouldReport) {
      shouldReportSecondaryInValues = shouldReport;
    }

    @Override
    public void reportIssueNoLineShift(TextRange textRange, String message) {
      super.reportIssue(textRange, message, List.of());
    }
  }
}
