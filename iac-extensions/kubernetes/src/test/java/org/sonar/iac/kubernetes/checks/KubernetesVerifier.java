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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.sonar.iac.kubernetes.plugin.KubernetesParserStatistics;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;
import org.sonar.iac.kubernetes.visitors.KubernetesCheckContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;
import org.sonar.iac.kubernetes.visitors.ProjectContext;
import org.sonar.iac.kubernetes.visitors.ProjectContextEnricherVisitor;
import org.sonar.iac.kubernetes.visitors.SecondaryLocationLocator;
import org.sonarsource.analyzer.commons.checks.verifier.MultiFileVerifier;

import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

public class KubernetesVerifier {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesVerifier.class);
  public static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
  private static final SensorContextTester SENSOR_CONTEXT = SensorContextTester.create(BASE_DIR.toAbsolutePath());
  private static final KubernetesAnalyzer KUBERNETES_ANALYZER = initializeKubernetesAnalyzer();
  private static final TreeParser<Tree> PARSER = KUBERNETES_ANALYZER::parse;

  public static void verify(String templateFileName, IacCheck check, String... fileNames) {
    var initialization = initializeVerification(templateFileName, fileNames);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    var projectContext = prepareProjectContext(inputFileContext, fileNames);
    Verifier.verify(PARSER, inputFileContext, check, multiFileVerifier -> new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext), commentsVisitor);
  }

  public static void verify(String templateFileName, IacCheck check, Collection<Verifier.Issue> expectedIssues) {
    var initialization = initializeVerification(templateFileName);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    var projectContext = prepareProjectContext(inputFileContext);
    Verifier.verify(PARSER, inputFileContext, check, multiFileVerifier -> new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext), commentsVisitor,
      expectedIssues.stream().toList());
  }

  /**
   * Only usable for pure Kubernetes files
   */
  public static void verifyContent(String content, IacCheck check) {
    Verifier.verify(PARSER, content, check);
  }

  public static void verifyNoIssue(String templateFileName, IacCheck check, String... fileNames) {
    var initialization = initializeVerification(templateFileName, fileNames);
    var inputFileContext = initialization.first();
    var commentsVisitor = initialization.second();
    var projectContext = prepareProjectContext(inputFileContext, fileNames);
    Verifier.verifyNoIssue(PARSER, inputFileContext, check, multiFileVerifier -> new KubernetesTestContext(multiFileVerifier, inputFileContext, projectContext), commentsVisitor);
  }

  private static Tuple<HelmInputFileContext, BiConsumer<Tree, Map<Integer, Set<Comment>>>> initializeVerification(String templateFileName, String... fileNames) {
    if (containsHelmContent(templateFileName)) {
      if (fileNames.length > 0) {
        throw new IllegalArgumentException("For Helm projects, all project files will be discovered. Explicit input is not required.");
      }
      var inputFileContext = HelmVerifier.prepareHelmContext(templateFileName);
      return new Tuple<>(inputFileContext, HelmVerifier.commentsWithShiftedTextRangeVisitor(inputFileContext));
    } else {
      var inputFileContext = new HelmInputFileContext(SENSOR_CONTEXT, inputFile(templateFileName, BASE_DIR));
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

  private static ProjectContext prepareProjectContext(HelmInputFileContext inputFileContext, String... additionalFiles) {
    var projectContextBuilder = ProjectContext.builder();
    var projectContextEnricherVisitor = new ProjectContextEnricherVisitor(projectContextBuilder);

    Stream.concat(
      inputFileContext.getAdditionalFiles().values().stream(),
      Arrays.stream(additionalFiles).map(fileName -> inputFile(fileName, BASE_DIR)))
      .map(additionalFile -> {
        String additionalContent = retrieveContent(additionalFile);
        return PARSER.parse(additionalContent, new HelmInputFileContext(SENSOR_CONTEXT, additionalFile));
      }).forEach(tree -> projectContextEnricherVisitor.scan(inputFileContext, tree));

    return projectContextBuilder.build();
  }

  private static String retrieveContent(InputFile inputFile) {
    try {
      return inputFile.contents();
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to read content of %s", inputFile), e);
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
    private final HelmInputFileContext currentCtx;

    private final ProjectContext projectContext;
    private boolean shouldReportSecondaryInValues = true;

    public KubernetesTestContext(MultiFileVerifier verifier, HelmInputFileContext currentCtx, ProjectContext projectContext) {
      super(verifier);
      this.currentCtx = currentCtx;
      this.projectContext = projectContext;
    }

    public HelmInputFileContext currentCtx() {
      return currentCtx;
    }

    @Override
    public ProjectContext projectContext() {
      return projectContext;
    }

    @Override
    protected void reportIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      var shiftedTextRange = LocationShifter.shiftLocation(currentCtx, textRange);

      List<SecondaryLocation> allSecondaryLocations = new ArrayList<>();
      if (shouldReportSecondaryInValues) {
        allSecondaryLocations = SecondaryLocationLocator.findSecondaryLocationsInAdditionalFiles(currentCtx, shiftedTextRange);
      }
      List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream()
        .map(secondaryLocation -> LocationShifter.computeShiftedSecondaryLocation(currentCtx, secondaryLocation))
        .toList();

      allSecondaryLocations.addAll(shiftedSecondaryLocations);

      super.reportIssue(shiftedTextRange, message, allSecondaryLocations);
    }

    @Override
    public boolean shouldReportSecondaryInValues() {
      return shouldReportSecondaryInValues;
    }

    @Override
    public void setShouldReportSecondaryInValues(boolean shouldReport) {
      shouldReportSecondaryInValues = shouldReport;
    }
  }
}
