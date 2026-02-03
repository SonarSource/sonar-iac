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
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.resources.Language;
import org.sonar.iac.common.extension.TogglableSensor;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.common.yaml.YamlParser;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

@DependedUpon("KustomizationSensor")
public class KustomizationSensor extends TogglableSensor {
  private static final Logger LOG = LoggerFactory.getLogger(KustomizationSensor.class);
  private static final Set<String> KUSTOMIZATION_FILE_NAMES = Set.of("kustomization.yaml", "kustomization.yml");

  private final KustomizationParser kustomizationParser;
  private final Language language;
  private final KustomizationInfoProvider kustomizationInfoProvider;
  private SensorTelemetry sensorTelemetry;

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
  public void executeIfActive(SensorContext context) {
    sensorTelemetry = new SensorTelemetry(context.config());
    processFiles(context);
    addTelemetry();
    sensorTelemetry.reportTelemetry(context);
  }

  private void processFiles(SensorContext context) {
    context.fileSystem().inputFiles(getFilePredicate(context))
      .forEach(inputFile -> processKustomizationFile(context, inputFile));
  }

  private static FilePredicate getFilePredicate(SensorContext context) {
    var predicates = context.fileSystem().predicates();
    return predicates
      .and(predicates.hasType(InputFile.Type.MAIN), KustomizationSensor::isKustomizationFile);
  }

  private void addTelemetry() {
    var kustomizationReferencedFiles = kustomizationInfoProvider.kustomizationReferencedFiles();
    var kustomizationFilesCount = kustomizationInfoProvider.kustomizationFilesCount();
    var kustomizationReferencedFilesCount = kustomizationInfoProvider.kustomizationReferencedFilesCount();

    sensorTelemetry.addTelemetry("kustomize", kustomizationFilesCount == 0 ? "0" : "1");
    sensorTelemetry.addTelemetry("kustomize.files.count", Integer.toString(kustomizationFilesCount));
    sensorTelemetry.addTelemetry("kustomize.referenced.files.count", Integer.toString(kustomizationReferencedFilesCount));

    LOG.debug("Kustomization sensor processed {} kustomization files and collected {} referenced files: {}",
      kustomizationFilesCount, kustomizationReferencedFilesCount, kustomizationReferencedFiles);
  }

  @Override
  protected String getActivationSettingKey() {
    return KubernetesSettings.ACTIVATION_KEY;
  }

  private static boolean isKustomizationFile(InputFile f) {
    return f.isFile() && KUSTOMIZATION_FILE_NAMES.contains(f.filename().toLowerCase(Locale.ROOT));
  }

  private void processKustomizationFile(SensorContext context, InputFile inputFile) {
    var referencedFiles = kustomizationParser.parse(context, inputFile);
    kustomizationInfoProvider.addKustomizationReferencedFiles(referencedFiles);
    kustomizationInfoProvider.incrementKustomizationFilesCount();
  }
}
