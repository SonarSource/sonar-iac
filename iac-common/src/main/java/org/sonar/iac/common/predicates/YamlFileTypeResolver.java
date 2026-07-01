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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.languages.IacLanguage;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.JSON_LANGUAGE_KEY;
import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

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
 * The resolver is created once per analysis: it depends on the analysis' {@link FileSystem} and {@link Configuration},
 * which in SonarLint only exist in the per-analysis container. It shares the injected {@link YamlFileTypeCache} - which
 * has the same per-analysis lifespan - with every other YAML based sensor of the analysis, so a file's type is computed
 * only once and reused by all of them.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.SINGLE_ANALYSIS)
public class YamlFileTypeResolver {
  public static final String EXTENDED_LOGGING_PROPERTY_NAME = "sonar.internal.iac.extendedLogging";

  // The base part of the candidate-language union (see candidateLanguages()), covering the file types this base resolver
  // classifies. GitHub Actions is intentionally part of this base set even though its analysis sensor ships in the
  // enterprise plugin: its FileType and GithubActionsFilePredicate live in this community module and belong to the base
  // predicate order so that a .github/workflows file is classified GITHUB_ACTIONS rather than Kubernetes/CloudFormation.
  // The community Kubernetes and CloudFormation sensors rely on this to skip workflow files (see
  // KubernetesSensorTest#shouldSkipKubernetesFileInGithubActionsWorkflowFolder and the CloudFormation equivalent), so it
  // cannot move to the enterprise resolver. The enterprise resolver only adds the languages whose predicates it contributes.
  private static final List<String> BASE_CANDIDATE_LANGUAGES = List.of(
    JSON_LANGUAGE_KEY,
    YAML_LANGUAGE_KEY,
    IacLanguage.KUBERNETES.getKey(),
    IacLanguage.CLOUDFORMATION.getKey(),
    IacLanguage.GITHUB_ACTIONS.getKey());

  protected final KustomizationFilePredicate kustomizationFilePredicate;
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
   *   The candidate languages, in contrast, are constants, so a subclass extends them by overriding
   *   {@link #candidateLanguages()} (called at analysis time, not from the constructor).
   */
  protected YamlFileTypeResolver(FileSystem fileSystem, Configuration config, YamlFileTypeCache yamlFileTypeCache,
    List<YamlFileTypePredicate> additionalFilePredicates) {
    var extendedLoggingEnabled = isExtendedLoggingEnabled(config);
    this.yamlFileTypeCache = yamlFileTypeCache;
    this.kustomizationFilePredicate = new KustomizationFilePredicate(extendedLoggingEnabled);
    this.kubernetesFilePredicate = new KubernetesFilePredicate(fileSystem, extendedLoggingEnabled);
    this.helmFilePredicate = new HelmFilePredicate(fileSystem, extendedLoggingEnabled);
    this.jvmConfigFilePredicate = new JvmConfigFilePredicate(fileSystem.predicates(), config, extendedLoggingEnabled);
    this.cloudFormationFilePredicate = new CloudFormationFilePredicate(config, extendedLoggingEnabled);
    this.githubActionsFilePredicate = new GithubActionsFilePredicate(fileSystem.predicates(), extendedLoggingEnabled);
    this.armJsonFilePredicate = new ArmJsonFilePredicate(fileSystem.predicates(), config, extendedLoggingEnabled);
    this.filePredicatesOrder = computeFilePredicatesOrder(additionalFilePredicates);
  }

  /**
   * The languages whose MAIN files {@link #getInputFiles} considers. This must be a fixed union covering every
   * language a YAML based file type can carry: the specialized IaC languages register no suffix by default, but a user
   * can reassign .yaml/.json files to one of them via {@code sonar.<lang>.file.suffixes}, and such files must still be
   * classified (they were, through {@link #getFilePredicate}, before {@link #getInputFiles} existed). Subclasses extend
   * the set by overriding this method (calling {@code super.candidateLanguages()} and adding their languages); it is
   * called at analysis time, so the override may safely depend on subclass state.
   */
  protected Set<String> candidateLanguages() {
    return new LinkedHashSet<>(BASE_CANDIDATE_LANGUAGES);
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
      // Kustomize files are identified by file name and resolved first: a kustomization.yaml/.yml is a Kustomize entry
      // point, not a deployable manifest, so it is classified as KUSTOMIZE even when it also carries Kubernetes content
      // (apiVersion/kind/metadata). This keeps it handled by the Kustomization sensor and out of every content based
      // sensor - including Azure Pipelines, which would otherwise match its `resources:` key (SONARIAC-2859).
      kustomizationFilePredicate,
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
   * Returns the MAIN YAML/JSON files of the given {@code fileSystem} resolved to any of the given {@link FileType}s, in
   * file-system iteration order.
   * <p>
   * Unlike {@link #getFilePredicate}, whose evaluation re-reads file content for every sensor that applies it, this
   * resolves each candidate file's {@link FileType} through the shared {@link YamlFileTypeCache}, so the content based
   * predicates run only once per file no matter how many sensors are interested in it.
   * <p>
   * Selection is scoped to the {@code fileSystem} passed by the caller - the running sensor's own
   * {@code SensorContext.fileSystem()} - and never an analysis-wide one: in a multi-module analysis a sensor must only
   * receive the files of the module being analyzed, and a sensor's hidden-file visibility (whether it declared
   * {@code processesHiddenFiles()}) must be honored. The result is therefore identical to what
   * {@code fileSystem.inputFiles(getFilePredicate(fileTypes))} would return, only cheaper to compute.
   *
   * @throws IllegalArgumentException if no {@link FileType} is provided
   */
  public List<InputFile> getInputFiles(FileSystem fileSystem, DurationStatistics durationStatistics, FileType... fileTypes) {
    if (fileTypes.length == 0) {
      throw new IllegalArgumentException("At least one FileType must be provided to collect files");
    }
    var candidateFiles = classifyCandidateFiles(fileSystem, durationStatistics);
    var typedFiles = yamlFileTypeCache.getFiles(fileTypes);
    return candidateFiles.stream()
      .filter(typedFiles::contains)
      .toList();
  }

  /**
   * Resolves and caches the {@link FileType} of the given file system's MAIN candidate files, returning them in
   * file-system iteration order. Candidates are the files in any of the {@link #candidateLanguages()} - the YAML/JSON
   * languages plus every specialized IaC language a .yaml/.json file may be reassigned to via
   * {@code sonar.<lang>.file.suffixes}; their actual type is then decided by the ordered predicates, exactly as
   * {@link #getFilePredicate} would. As the {@link YamlFileTypeCache} is shared by all sensors of the analysis, a file's
   * content based predicates are evaluated only by the first sensor that reaches it; later sensors get cache hits.
   */
  private List<InputFile> classifyCandidateFiles(FileSystem fileSystem, DurationStatistics durationStatistics) {
    dispatchTimers(durationStatistics);
    var predicates = fileSystem.predicates();
    // Candidates are MAIN files in any candidate language, plus Helm .tpl templates: those carry no YAML language (they
    // are not valid YAML) yet are a valid HELM file type, so a language-only filter would drop them - as it would here
    // the HELM members that getFilePredicate(HELM) used to pick up over the whole file system (SONARIAC-3025).
    var candidates = predicates.and(
      predicates.hasType(InputFile.Type.MAIN),
      predicates.or(
        predicates.hasLanguages(candidateLanguages().toArray(new String[0])),
        predicates.matchesPathPattern(HelmFilePredicate.TPL_TEMPLATE_PATH_PATTERN)));
    var candidateFiles = new ArrayList<InputFile>();
    durationStatistics.time("Scanner file retrieval", () -> {
      for (InputFile inputFile : fileSystem.inputFiles(candidates)) {
        computeFileTypeWithCache(inputFile);
        candidateFiles.add(inputFile);
      }
    });
    return candidateFiles;
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
      yamlFileTypeCache.put(file, type);
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
