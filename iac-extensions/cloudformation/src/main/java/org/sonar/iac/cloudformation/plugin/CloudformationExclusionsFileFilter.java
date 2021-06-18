/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import java.io.IOException;
import java.util.Scanner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CloudformationExclusionsFileFilter implements InputFileFilter {

  private static final Logger LOG = Loggers.get(CloudformationExclusionsFileFilter.class);

  private final WildcardPattern[] excludedPatterns;
  private final String fileIdentifier;

  public CloudformationExclusionsFileFilter(Configuration configuration) {
    this.excludedPatterns = WildcardPattern.create(configuration.getStringArray(CloudformationSettings.EXCLUSIONS_KEY));
    this.fileIdentifier = configuration.get(CloudformationSettings.FILE_IDENTIFIER_KEY).orElse("");
  }

  @Override
  public boolean accept(InputFile inputFile) {
    if (!CloudformationLanguage.KEY.equals(inputFile.language())) {
      return true;
    }

    String relativePath = inputFile.uri().toString();
    if (WildcardPattern.match(excludedPatterns, relativePath)) {
      LOG.debug("File [" + inputFile.uri() + "] is excluded by '" + CloudformationSettings.EXCLUSIONS_KEY + "' property and will not be analyzed");
      return false;
    }

    if (!hasFileIdentifier(inputFile)) {
      LOG.debug("File [" + inputFile.uri() + "] is because it does not contain the identifier.");
      return false;
    }

    return true;
  }

  private boolean hasFileIdentifier(InputFile inputFile) {
    if ("".equals(fileIdentifier)) {
      return true;
    }

    try (Scanner scanner = new Scanner(inputFile.inputStream(), inputFile.charset().name())) {
      while (scanner.hasNextLine()) {
        if (scanner.nextLine().contains(fileIdentifier)) {
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
