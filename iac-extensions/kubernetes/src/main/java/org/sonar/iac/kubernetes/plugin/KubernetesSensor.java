/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.yaml.YamlSensor;
import org.sonar.iac.kubernetes.checks.KubernetesCheckList;

public class KubernetesSensor extends YamlSensor {

  public KubernetesSensor(SonarRuntime sonarRuntime, FileLinesContextFactory fileLinesContextFactory, CheckFactory checkFactory,
                             NoSonarFilter noSonarFilter, KubernetesLanguage language) {
    super(sonarRuntime, fileLinesContextFactory, checkFactory, noSonarFilter, language, KubernetesCheckList.checks());
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
  protected FilePredicate customFilePredicate(SensorContext sensorContext) {
    return new KubernetesFilePredicate();
  }

  static class KubernetesFilePredicate implements FilePredicate {

    // https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/#required-fields
    private static final Set<String> IDENTIFIER = Set.of("apiVersion", "kind", "metadata", "spec");
    private static final Logger LOG = Loggers.get(KubernetesFilePredicate.class);

    @Override
    public boolean apply(InputFile inputFile) {
      return hasKubernetesObjectStructure(inputFile);
    }

    private static boolean hasKubernetesObjectStructure(InputFile inputFile) {
      int identifierCount = 0;
      try (Scanner scanner = new Scanner(inputFile.inputStream(), inputFile.charset().name())) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if (IDENTIFIER.stream().anyMatch(line::startsWith)) {
            identifierCount++;
          } else if (FILE_SEPERATOR.equals(line)) {
            identifierCount = 0;
          }
          if (identifierCount == 4) {
            return true;
          }
        }
      } catch (IOException e) {
        LOG.error(String.format("Unable to read file: %s.", inputFile.uri()));
        LOG.error(e.getMessage());
      }
      return false;
    }
  }
}
