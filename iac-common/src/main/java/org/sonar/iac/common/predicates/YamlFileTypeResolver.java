/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Single entry point to determine and share the {@link FileType} of YAML (or YAML-like) files between the different YAML
 * based sensors.
 * <p>
 * Historically, each sensor was managing predicate conflicts on its own (applying its own predicate while negating the
 * predicates of all other sensors), which was both error prone and expensive (the same predicate, including content
 * based ones, was re-evaluated by every sensor). This class centralizes that logic:
 * <ul>
 *   <li>it defines a single order in which predicates are applied to determine the type of a file;</li>
 *   <li>it caches the resulting {@link FileType} per file in {@link YamlFileTypeCache} so that it is computed only once.</li>
 * </ul>
 * The resolver itself is created once per analysis: it depends on the analysis' {@link FileSystem} and
 * {@link Configuration}, which in SonarLint only exist in the per-analysis container (a longer lived instance could not
 * be injected with them). What is shared across sensors and across analyses is the injected {@link YamlFileTypeCache},
 * which is engine-wide, so a file's type is still computed only once and reused by every sensor.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.SINGLE_ANALYSIS)
public class YamlFileTypeResolver {
  public static final String EXTENDED_LOGGING_PROPERTY_NAME = "sonar.internal.iac.extendedLogging";

  protected final KubernetesFilePredicate kubernetesFilePredicate;
  protected final HelmFilePredicate helmFilePredicate;
  protected final JvmConfigFilePredicate jvmConfigFilePredicate;
  protected final CloudFormationFilePredicate cloudFormationFilePredicate;
  protected final GithubActionsFilePredicate githubActionsFilePredicate;
  protected final ArmJsonFilePredicate armJsonFilePredicate;

  protected final YamlFileTypeCache yamlFileTypeCache;

  // The order in which predicates are applied to determine a file's type. It is built once in the constructor: the
  // predicates are all final fields, so the order never changes for a given resolver instance.
  private final List<YamlFileTypePredicate> filePredicatesOrder;

  public YamlFileTypeResolver(FileSystem fileSystem, Configuration config, YamlFileTypeCache yamlFileTypeCache) {
    this(fileSystem, config, yamlFileTypeCache, List.of());
  }

  /**
   * @param additionalFilePredicates predicates contributed by a subclass (for example the enterprise-only file types),
   *   appended after the community predicates with the lowest precedence. They are passed in rather than collected
   *   through an overridable method because the order is built here, in the constructor: a subclass' own predicate
   *   fields are only initialized after {@code super(...)} returns, so an overridable method would still see them null.
   */
  protected YamlFileTypeResolver(FileSystem fileSystem, Configuration config, YamlFileTypeCache yamlFileTypeCache,
    List<YamlFileTypePredicate> additionalFilePredicates) {
    var extendedLoggingEnabled = isExtendedLoggingEnabled(config);
    this.yamlFileTypeCache = yamlFileTypeCache;
    this.kubernetesFilePredicate = new KubernetesFilePredicate(fileSystem, extendedLoggingEnabled);
    this.helmFilePredicate = new HelmFilePredicate(fileSystem, extendedLoggingEnabled);
    this.jvmConfigFilePredicate = new JvmConfigFilePredicate(fileSystem.predicates(), config, extendedLoggingEnabled);
    this.cloudFormationFilePredicate = new CloudFormationFilePredicate(config, extendedLoggingEnabled);
    this.githubActionsFilePredicate = new GithubActionsFilePredicate(fileSystem.predicates(), extendedLoggingEnabled);
    this.armJsonFilePredicate = new ArmJsonFilePredicate(fileSystem.predicates(), config, extendedLoggingEnabled);
    this.filePredicatesOrder = computeFilePredicatesOrder(additionalFilePredicates);
  }

  /**
   * Define the order of appliance of predicates. The order encodes the precedence between file types (the first matching
   * predicate wins) and must reproduce the precedence the sensors had before this logic was centralized: each sensor used
   * to apply its own predicate while negating the predicates of the sensors that should win over it.
   * <p>
   * Within that constraint we keep the cheaper, more specific predicates first. A predicate with a precise filepath
   * pattern is cheaper to execute; the most costly ones read file content. The notable exception is the JVM config
   * predicate: although it is filepath only (and therefore cheap), it used to defer to GitHub Actions, Kubernetes and
   * CloudFormation, so it must be applied after them to keep a Spring/Micronaut/Quarkus config file that also looks like
   * a Kubernetes or CloudFormation file classified as it was before centralization.
   * <p>
   * The {@code additionalFilePredicates} contributed by a subclass are appended last, with the lowest precedence.
   */
  private List<YamlFileTypePredicate> computeFilePredicatesOrder(List<YamlFileTypePredicate> additionalFilePredicates) {
    var order = new ArrayList<YamlFileTypePredicate>(List.of(
      // Cheap filepath checks (content is only read in the rare action.yml case). GitHub Actions files always take precedence.
      githubActionsFilePredicate,
      // Helm is checked before plain Kubernetes: a Helm template that also carries Kubernetes content must resolve to
      // HELM (filepath/Helm-project based), as the YAML sensor relied on Helm detection ignoring such content.
      helmFilePredicate,
      // Content check (Kubernetes identifiers) on YAML/Kubernetes files
      kubernetesFilePredicate,
      // Only content check
      cloudFormationFilePredicate,
      // Filepath only, but intentionally after Kubernetes/CloudFormation: JVM config used to defer to them (see above)
      jvmConfigFilePredicate,
      // JSON only, content check
      armJsonFilePredicate));
    order.addAll(additionalFilePredicates);
    return List.copyOf(order);
  }

  protected static boolean isExtendedLoggingEnabled(Configuration config) {
    return config.getBoolean(EXTENDED_LOGGING_PROPERTY_NAME).orElse(false);
  }

  /**
   * Returns a predicate matching the files resolved to any of the given {@link FileType}s. As all sensors share this
   * resolver and its cache, the type-specific behaviour (including the enterprise-only types) is selected by
   * {@code fileTypes} rather than by the concrete resolver type, so sensors only need to depend on
   * {@link YamlFileTypeResolver}. Passing several types lets a sensor that handles more than one (e.g. Kubernetes and
   * Helm) match them in a single predicate.
   *
   * @throws IllegalArgumentException if no {@link FileType} is provided
   */
  public FilePredicate getFilePredicate(DurationStatistics durationStatistics, FileType... fileTypes) {
    if (fileTypes.length == 0) {
      throw new IllegalArgumentException("At least one FileType must be provided to build a file predicate");
    }
    dispatchTimers(durationStatistics);
    var types = EnumSet.copyOf(Arrays.asList(fileTypes));
    return (InputFile file) -> isFileType(file, types);
  }

  /**
   * Re-binds the shared predicate instances to the calling sensor's {@link DurationStatistics}. Sensors run sequentially
   * and each calls {@link #getFilePredicate} right before scanning its files, so a predicate's evaluation time is
   * recorded into the statistics of the sensor that triggers it. Because the resolved {@link FileType} is cached, a
   * file's predicates are evaluated at most once (by the first sensor that reaches it); later sensors get cache hits and
   * record no time for it. The binding is therefore intentionally "last writer wins": it reflects the currently running
   * sensor, which is the one actually paying for any cache miss.
   */
  protected void dispatchTimers(DurationStatistics durationStatistics) {
    filePredicatesOrder.forEach(predicate -> predicate.applyTimers(durationStatistics));
  }

  protected boolean isFileType(InputFile file, Set<FileType> fileTypes) {
    return fileTypes.contains(computeFileTypeWithCache(file));
  }

  protected FileType computeFileTypeWithCache(InputFile file) {
    var type = yamlFileTypeCache.get(file.uri());
    if (type == null) {
      type = computeFileType(file, filePredicatesOrder).orElse(FileType.UNDETERMINED);
      yamlFileTypeCache.put(file.uri(), type);
    }
    return type;
  }

  protected Optional<FileType> computeFileType(InputFile file, List<YamlFileTypePredicate> filePredicates) {
    return filePredicates.stream()
      .filter(predicate -> predicate.apply(file))
      .map(YamlFileTypePredicate::fileType)
      .findFirst();
  }
}
