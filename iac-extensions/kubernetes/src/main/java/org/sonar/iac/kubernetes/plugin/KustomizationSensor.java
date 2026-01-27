/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.plugin;

import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.yaml.YamlParser;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

@DependedUpon("KustomizationSensor")
public class KustomizationSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(KustomizationSensor.class);
  private static final Set<String> KUSTOMIZATION_FILE_NAMES = Set.of("kustomization.yaml", "kustomization.yml");

  private final KustomizationParser kustomizationParser;
  private final Language language;
  private final KustomizationInfoProvider kustomizationInfoProvider;

  public KustomizationSensor(KustomizationInfoProvider kustomizationInfoProvider, KubernetesLanguage language) {
    this.kustomizationInfoProvider = kustomizationInfoProvider;
    this.kustomizationParser = new KustomizationParser(new YamlParser());
    this.language = language;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguages(YAML_LANGUAGE_KEY, language.getKey())
      .name("IaC Kustomization Sensor");

  }

  @Override
  public void execute(SensorContext context) {
    context.fileSystem()
      .inputFiles(KustomizationSensor::isKustomizationFile)
      .forEach(inputFile -> processKustomizationFile(context, inputFile));

    var kustomizationReferencedFiles = kustomizationInfoProvider.kustomizationReferencedFiles();
    LOG.debug("Kustomization sensor collected {} referenced files: {}",
      kustomizationReferencedFiles.size(), kustomizationReferencedFiles);
  }

  private static boolean isKustomizationFile(InputFile f) {
    return f.isFile() && KUSTOMIZATION_FILE_NAMES.contains(f.filename().toLowerCase(Locale.ROOT));
  }

  private void processKustomizationFile(SensorContext context, InputFile inputFile) {
    var referencedFiles = kustomizationParser.parse(context, inputFile);
    kustomizationInfoProvider.addKustomizationReferencedFiles(referencedFiles);
  }
}
