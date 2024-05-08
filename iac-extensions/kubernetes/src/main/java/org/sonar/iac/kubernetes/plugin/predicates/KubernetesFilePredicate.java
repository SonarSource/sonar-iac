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
package org.sonar.iac.kubernetes.plugin.predicates;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

import static org.sonar.iac.common.yaml.YamlSensor.FILE_SEPARATOR;

public class KubernetesFilePredicate implements FilePredicate {

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
      LOG.error("Unable to read file: {}.", inputFile);
      LOG.error(e.getMessage());
    }

    if (hasExpectedIdentifier) {
      return true;
    } else {
      LOG.debug("File without Kubernetes identifier: {}", inputFile);
      return false;
    }
  }
}
