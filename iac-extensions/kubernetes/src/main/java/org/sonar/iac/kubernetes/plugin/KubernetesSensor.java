/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;

public class KubernetesSensor extends YamlSensor {
  private static final String DIRECTIVE_IN_COMMENT = "#.*\\{\\{";
  private static final String DIRECTIVE_IN_SINGLE_QUOTE = "'[^']*\\{\\{[^']*'";
  private static final String DIRECTIVE_IN_DOUBLE_QUOTE = "\"[^\"]*\\{\\{[^\"]*\"";
  private static final String CODEFRESH_VARIABLES = "\\{\\{[\\w\\s]+}}";
  private static final Pattern HELM_DIRECTIVE_IN_COMMENT_OR_STRING = Pattern.compile("(" +
    String.join("|", DIRECTIVE_IN_COMMENT, DIRECTIVE_IN_SINGLE_QUOTE, DIRECTIVE_IN_DOUBLE_QUOTE, CODEFRESH_VARIABLES) + ")");

  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
    NoSonarFilter noSonarFilter, KubernetesLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, KubernetesCheckList.checks());
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(YAML_LANGUAGE_KEY)
      .name("IaC " + language.getName() + " Sensor");
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
    return new KubernetesFilePredicate();
  }

  static class KubernetesFilePredicate implements FilePredicate {

    private static final Pattern LINE_TERMINATOR = Pattern.compile("[\\n\\r\\u2028\\u2029]");

    // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
    private static final Set<String> IDENTIFIER = Set.of("apiVersion", "kind", "metadata", "spec");
    private static final Logger LOG = LoggerFactory.getLogger(KubernetesFilePredicate.class);
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public boolean apply(InputFile inputFile) {
      return hasKubernetesObjectStructure(inputFile);
    }

    private static boolean hasKubernetesObjectStructure(InputFile inputFile) {
      int identifierCount = 0;
      boolean hasExpectedIdentifier = false;
      try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputFile.inputStream())) {
        // Only firs 8k bytes is read to avoid slow execution for big one-line files
        byte[] bytes = bufferedInputStream.readNBytes(DEFAULT_BUFFER_SIZE);
        String text = new String(bytes, inputFile.charset());
        String[] lines = LINE_TERMINATOR.split(text);
        for (String line : lines) {
          if (IDENTIFIER.stream().anyMatch(line::startsWith)) {
            identifierCount++;
          } else if (FILE_SEPERATOR.equals(line)) {
            identifierCount = 0;
          } else if (line.contains("{{") && !HELM_DIRECTIVE_IN_COMMENT_OR_STRING.matcher(line).find()) {
            LOG.debug("Line contains Helm Chart directive, file will not be analyzed.\n{}", line);
            return false;
          }
          if (identifierCount == 4) {
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
}
