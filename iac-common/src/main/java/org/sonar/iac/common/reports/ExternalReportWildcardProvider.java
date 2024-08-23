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
package org.sonar.iac.common.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.WildcardPattern;

/**
 * It is alternative implementation of {@link org.sonarsource.analyzer.commons.ExternalReportProvider} but it accepts wildcards in filenames.
 */
public final class ExternalReportWildcardProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ExternalReportWildcardProvider.class);
  private static final int MINIMUM_MAJOR_SUPPORTED_VERSION = 7;
  private static final int MINIMUM_MINOR_SUPPORTED_VERSION = 2;

  private ExternalReportWildcardProvider() {
    // util class
  }

  public static List<File> getReportFiles(SensorContext context, String externalReportsProperty) {
    var minumumVersion = Version.create(MINIMUM_MAJOR_SUPPORTED_VERSION, MINIMUM_MINOR_SUPPORTED_VERSION);
    boolean externalIssuesSupported = context.runtime().getApiVersion().isGreaterThanOrEqual(minumumVersion);
    if (!externalIssuesSupported) {
      LOG.error("Import of external issues requires SonarQube 7.2 or greater.");
      return Collections.emptyList();
    }

    String[] reportPaths = context.config().getStringArray(externalReportsProperty);

    if (reportPaths.length == 0) {
      return Collections.emptyList();
    }

    List<File> result = new ArrayList<>();
    for (String reportPath : reportPaths) {
      var reports = getIOFiles(context.fileSystem().baseDir(), reportPath);
      result.addAll(reports);
    }

    return result;
  }

  private static List<File> getIOFiles(File baseDir, String reportPath) {
    var pattern = WildcardPattern.create(reportPath);
    try {
      var baseDirPath = baseDir.toPath();
      try (var stream = Files.find(baseDirPath,
        Integer.MAX_VALUE,
        (filePath, fileAttr) -> fileAttr.isRegularFile() && pattern.match(baseDirPath.relativize(filePath).toFile().getPath()))) {
        return stream.map(Path::toFile)
          .toList();
      }
    } catch (IOException e) {
      LOG.debug("Exception, when searching files to import report.", e);
      return Collections.emptyList();
    }
  }
}
