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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.Verifier;
import org.sonar.iac.helm.HelmEvaluator;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.plugin.HelmProcessor;
import org.sonar.iac.kubernetes.plugin.KubernetesParser;
import org.sonar.iac.kubernetes.plugin.KubernetesParserStatistics;
import org.sonar.iac.kubernetes.visitors.LocationShifter;
import org.sonarsource.analyzer.commons.checks.verifier.SingleFileVerifier;

import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

public class KubernetesVerifier extends Verifier {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesVerifier.class);
  public static final Path BASE_DIR = Paths.get("src", "test", "resources", "checks");
  private static final SensorContextTester sensorContext = SensorContextTester.create(BASE_DIR.toAbsolutePath());
  private static final KubernetesParserStatistics kubernetesParserStatistics = new KubernetesParserStatistics();
  private static final LocationShifter locationShifter = new LocationShifter();
  private static final KubernetesParser PARSER;

  private KubernetesVerifier() {
    super();
  }

  static {
    File temporaryDirectory;
    try {
      temporaryDirectory = Files.createTempDirectory("kubernetesVerifierExecutable").toFile();
    } catch (IOException e) {
      throw new IllegalStateException("Could not create temporary directory", e);
    }
    HelmEvaluator helmEvaluator = new HelmEvaluator(new DefaultTempFolder(temporaryDirectory, false));
    HelmProcessor helmProcessor = new HelmProcessor(helmEvaluator);
    helmProcessor.initialize();
    PARSER = new KubernetesParser(helmProcessor, locationShifter, kubernetesParserStatistics);
    temporaryDirectory.deleteOnExit();
  }

  public static void verify(String templateFileName, IacCheck check) {
    InputFile inputFile = inputFile(templateFileName, BASE_DIR);
    String content;
    try {
      content = inputFile.contents();
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to read content of %s", inputFile), e);
    }
    if (KubernetesParser.hasHelmContent(content)) {
      InputFileContext inputFileContext = prepareHelmContext(templateFileName);
      verifyHelmFile(check, inputFileContext, content);
    } else {
      verify(PARSER, BASE_DIR.resolve(templateFileName), check);
    }
  }

  public static void verifyContent(String content, IacCheck check) {
    verify(PARSER, content, check);
  }

  public static void verifyNoIssue(String fileName, IacCheck check) {
    verifyNoIssue(PARSER, BASE_DIR.resolve(fileName), check);
  }

  public static void verifyHelmFile(IacCheck check, InputFileContext inputFileContext, String content) {
    Tree root = parse(PARSER, content, inputFileContext);
    SingleFileVerifier verifier = createVerifier(Path.of(inputFileContext.inputFile.uri()), root, commentsWithShiftedTextRangeVisitor(inputFileContext));
    LocationShiftedTestContext testContext = new LocationShiftedTestContext(verifier, inputFileContext, locationShifter);
    runAnalysis(testContext, check, root);
    verifier.assertOneOrMoreIssues();
  }

  public static InputFileContext prepareHelmContext(String templateFileName) {
    var sourceInputFile = inputFile(templateFileName, BASE_DIR);
    sensorContext.fileSystem().add(sourceInputFile);
    var filePath = Path.of(sourceInputFile.uri());
    var helmProjectPath = HelmFilesystemUtils.retrieveHelmProjectFolder(filePath, BASE_DIR.toAbsolutePath().toFile());
    if (helmProjectPath == null) {
      throw new IllegalStateException(String.format("Could not resolve helmProjectPath for file %s, possible missing Chart.yaml", filePath));
    }
    addDependentFilesToSensorContext(helmProjectPath);

    return new InputFileContext(sensorContext, sourceInputFile);
  }

  public static void addDependentFilesToSensorContext(Path helmProjectPath) {
    try (Stream<Path> pathStream = Files.list(helmProjectPath)) {
      pathStream
        .filter(path -> path.toFile().isFile())
        .forEach(path -> addFileToSensorContext(sensorContext, BASE_DIR, path.toString()));
    } catch (IOException e) {
      LOG.error("Error while trying to add dependent files to sensor context", e);
    }
  }

  private static BiConsumer<Tree, Map<Integer, Set<Comment>>> commentsWithShiftedTextRangeVisitor(InputFileContext inputFileContext) {
    Set<TextRange> alreadyAdded = new HashSet<>();
    return (root, commentsByLine) -> (new TreeVisitor<>()).register(Tree.class,
      (ctx, tree) -> {
        // The shifted location is not precise enough and always returns the whole line, so it is not suitable for detecting already added tree's
        // The unshifted location is more granular and therefore can be used for detecting duplicates, even if it's not the real location in the
        // source file
        if (tree instanceof HasComments && !alreadyAdded.contains(tree.textRange())) {
          for (Comment comment : ((HasComments) tree).comments()) {
            Comment shiftedComment = new CommentImpl(comment.value(), comment.contentText(), locationShifter.computeShiftedLocation(inputFileContext, comment.textRange()));
            commentsByLine.computeIfAbsent(shiftedComment.textRange().start().line(), i -> new HashSet<>()).add(shiftedComment);
          }
          alreadyAdded.add(tree.textRange());
        }
      }).scan(new TreeContext(), root);
  }

  public static class LocationShiftedTestContext extends TestContext {
    private final InputFileContext currentCtx;
    private final LocationShifter locationShifter;

    public LocationShiftedTestContext(SingleFileVerifier verifier, InputFileContext currentCtx, LocationShifter locationShifter) {
      super(verifier);
      this.currentCtx = currentCtx;
      this.locationShifter = locationShifter;
    }

    @Override
    protected void reportIssue(TextRange textRange, String message, List<SecondaryLocation> secondaryLocations) {
      var shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, textRange);
      List<SecondaryLocation> shiftedSecondaryLocations = secondaryLocations.stream().map(this::adaptSecondaryLocation).collect(Collectors.toList());
      super.reportIssue(shiftedTextRange, message, shiftedSecondaryLocations);
    }

    private SecondaryLocation adaptSecondaryLocation(SecondaryLocation secondaryLocation) {
      var shiftedTextRange = locationShifter.computeShiftedLocation(currentCtx, secondaryLocation.textRange);
      return new SecondaryLocation(shiftedTextRange, secondaryLocation.message);
    }
  }
}
