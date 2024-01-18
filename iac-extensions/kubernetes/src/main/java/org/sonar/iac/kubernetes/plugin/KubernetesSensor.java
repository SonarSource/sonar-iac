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
package org.sonar.iac.kubernetes.plugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.common.yaml.visitors.YamlMetricsVisitor;
import org.sonar.iac.helm.utils.HelmFilesystemUtils;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;
import org.sonar.iac.kubernetes.visitors.AdjustableChecksVisitor;
import org.sonar.iac.kubernetes.visitors.CommentLocationVisitor;
import org.sonar.iac.kubernetes.visitors.KubernetesHighlightingVisitor;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

public class KubernetesSensor extends YamlSensor {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesSensor.class);
  private static final String HELM_ACTIVATION_KEY = "sonar.kubernetes.internal.helm.enable";
  private final HelmProcessor helmProcessor;

  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language, HelmProcessor helmProcessor) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, KubernetesCheckList.checks());
    this.helmProcessor = helmProcessor;
  }

  @Override
  protected void initContext(SensorContext sensorContext) {
    if (shouldEnableHelmAnalysis(sensorContext)) {
      LOG.debug("Initializing Helm processor");
      helmProcessor.initialize();
    } else {
      LOG.debug("Skipping initialization of Helm processor");
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(YAML_LANGUAGE_KEY)
      .name("IaC " + language.getName() + " Sensor");
  }

  @Override
  protected TreeParser<Tree> treeParser() {
    return new KubernetesParser(helmProcessor);
  }

  @Override
  protected List<TreeVisitor<InputFileContext>> visitors(SensorContext sensorContext, DurationStatistics statistics) {
    List<TreeVisitor<InputFileContext>> visitors = new ArrayList<>();
    if (isNotSonarLintContext(sensorContext)) {
      visitors.add(new KubernetesHighlightingVisitor());
      visitors.add(new YamlMetricsVisitor(fileLinesContextFactory, noSonarFilter));
    }
    var locationShifter = new LocationShifter();
    visitors.add(new CommentLocationVisitor(locationShifter));
    visitors.add(new AdjustableChecksVisitor(checks, statistics, locationShifter));
    return visitors;
  }

  @Override
  protected String repositoryKey() {
    return KubernetesExtension.REPOSITORY_KEY;
  }

  @Override
  protected String getActivationSettingKey() {
    return KubernetesSettings.ACTIVATION_KEY;
  }

  @Override
  protected FilePredicate mainFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    return predicates.and(
      predicates.hasLanguage(YAML_LANGUAGE_KEY),
      predicates.hasType(InputFile.Type.MAIN),
      customFilePredicate(sensorContext));
  }

  @Override
  protected FilePredicate customFilePredicate(SensorContext sensorContext) {
    FilePredicates predicates = sensorContext.fileSystem().predicates();
    var helmTemplatePredicate = predicates.and(
      predicates.matchesPathPattern("**/templates/**"),
      new HelmProjectMemberPredicate());
    return predicates.or(
      new KubernetesFilePredicate(),
      helmTemplatePredicate);
  }

  private boolean shouldEnableHelmAnalysis(SensorContext sensorContext) {
    return isNotSonarLintContext(sensorContext) && sensorContext.config().getBoolean(HELM_ACTIVATION_KEY).orElse(true);
  }

  static class KubernetesFilePredicate implements FilePredicate {

    private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");

    // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
    private static final Set<String> IDENTIFIER = Set.of("apiVersion", "kind", "metadata");
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesFilePredicate.class);
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public boolean apply(InputFile inputFile) {
      return hasKubernetesObjectStructure(inputFile);
    }

    private static boolean hasKubernetesObjectStructure(InputFile inputFile) {
      var identifierCount = 0;
      var hasExpectedIdentifier = false;
      try (var bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
        // Only firs 8k bytes is read to avoid slow execution for big one-line files
        byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
        var text = new String(bytes, inputFile.charset());
        String[] lines = LINE_TERMINATOR.split(text);
        for (String line : lines) {
          if (IDENTIFIER.stream().anyMatch(line::startsWith)) {
            identifierCount++;
          } else if (FILE_SEPARATOR.equals(line)) {
            identifierCount = 0;
          }
          if (identifierCount == IDENTIFIER.size()) {
            hasExpectedIdentifier = true;
          }
        }
      } catch (IOException e) {
        LOG.error("Unable to read file: {}.", inputFile.uri());
        LOG.error(e.getMessage());
      }

      if (hasExpectedIdentifier) {
        return true;
      } else {
        LOG.debug("File without Kubernetes identifier: {}", inputFile.uri());
        return false;
      }
    }
  }

  static class HelmProjectMemberPredicate implements FilePredicate {
    @Override
    public boolean apply(InputFile inputFile) {
      return HelmFilesystemUtils.retrieveHelmProjectFolder(Path.of(inputFile.uri())) != null;
    }
  }
}
