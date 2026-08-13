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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.SharedFileHeadReader;
import org.sonar.iac.common.languages.IacLanguage;
import org.sonarsource.api.sonarlint.SonarLintSide;

import static org.sonar.iac.common.extension.SonarRuntimeUtils.isHiddenFilesAnalysisSupported;
import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.JSON_LANGUAGE_KEY;
import static org.sonar.iac.common.yaml.AbstractYamlLanguageSensor.YAML_LANGUAGE_KEY;

/**
 * Single entry point to determine and share the {@link FileType} of YAML (or YAML-like) files between the different YAML
 * based sensors: the scanner's {@link FileSystem#predicates()} selects candidate files cheaply (type/language/path),
 * then {@link #classify} assigns each one a type by trying {@code filePredicatesOrder} in order. Results are cached in
 * {@link YamlFileTypeCache}, shared per-analysis so a file's type is computed only once.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.SINGLE_ANALYSIS)
public class YamlFileTypeResolver {
  public static final String EXTENDED_LOGGING_PROPERTY_NAME = "sonar.internal.iac.extendedLogging";

  // GitHub Actions is included here (not only in the enterprise resolver) so a .github/workflows file is classified
  // GITHUB_ACTIONS rather than Kubernetes/CloudFormation (see KubernetesSensorTest#shouldSkipKubernetesFileInGithubActionsWorkflowFolder).
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
  protected final SharedFileHeadReader sharedFileHeadReader;

  // Lazily built on first use via filePredicatesOrder(): a subclass contributes its own predicates through
  // additionalFilePredicates(), which may depend on subclass fields only initialized after super(...) returns.
  @Nullable
  private List<YamlFileTypePredicate> filePredicatesOrder;

  public YamlFileTypeResolver(FileSystem fileSystem, Configuration config, YamlFileTypeCache yamlFileTypeCache) {
    FilePredicates predicates = fileSystem.predicates();
    var extendedLoggingEnabled = isExtendedLoggingEnabled(config);
    this.yamlFileTypeCache = yamlFileTypeCache;
    this.sharedFileHeadReader = new SharedFileHeadReader();
    this.kustomizationFilePredicate = new KustomizationFilePredicate(predicates, extendedLoggingEnabled);
    this.kubernetesFilePredicate = new KubernetesFilePredicate(predicates, extendedLoggingEnabled, sharedFileHeadReader);
    this.helmFilePredicate = new HelmFilePredicate(fileSystem, extendedLoggingEnabled);
    this.jvmConfigFilePredicate = new JvmConfigFilePredicate(predicates, config, extendedLoggingEnabled);
    this.cloudFormationFilePredicate = new CloudFormationFilePredicate(predicates, config, extendedLoggingEnabled, sharedFileHeadReader);
    this.githubActionsFilePredicate = new GithubActionsFilePredicate(predicates, extendedLoggingEnabled, sharedFileHeadReader);
    this.armJsonFilePredicate = new ArmJsonFilePredicate(predicates, config, extendedLoggingEnabled, sharedFileHeadReader);
  }

  /**
   * Predicates contributed by a subclass, appended after the community predicates with the lowest precedence. Called
   * lazily (not from the constructor) so a subclass' own fields are initialized first.
   */
  protected List<YamlFileTypePredicate> additionalFilePredicates() {
    return List.of();
  }

  /**
   * The languages whose MAIN files are classification candidates. A user can reassign .yaml/.json files to a
   * specialized IaC language via {@code sonar.<lang>.file.suffixes}, so this must cover all of them; subclasses extend
   * it by overriding and adding their languages.
   */
  public Set<String> candidateLanguages() {
    return new LinkedHashSet<>(BASE_CANDIDATE_LANGUAGES);
  }

  /**
   * The order in which predicates are tried by {@link #classify} (first match wins). Cheaper/more specific predicates
   * come first; the JVM config predicate is the exception - though filepath-only, it must defer to GitHub Actions,
   * Kubernetes and CloudFormation, so a config file that also looks like one of those keeps that classification.
   */
  private List<YamlFileTypePredicate> filePredicatesOrder() {
    if (filePredicatesOrder == null) {
      filePredicatesOrder = computeFilePredicatesOrder();
    }
    return filePredicatesOrder;
  }

  private List<YamlFileTypePredicate> computeFilePredicatesOrder() {
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
    order.addAll(additionalFilePredicates());
    return List.copyOf(order);
  }

  protected static boolean isExtendedLoggingEnabled(Configuration config) {
    return config.getBoolean(EXTENDED_LOGGING_PROPERTY_NAME).orElse(false);
  }

  /**
   * Re-binds the shared predicates to the calling sensor's {@link DurationStatistics}, so classification time is
   * recorded against whichever sensor actually triggers the (at most once) scan.
   */
  protected void dispatchTimers(DurationStatistics durationStatistics) {
    filePredicatesOrder().forEach(predicate -> predicate.applyTimers(durationStatistics));
  }

  public List<InputFile> getInputFiles(FileType fileType) {
    return getInputFiles(Set.of(fileType));
  }

  /**
   * Reads the already classified files of the given {@link FileType}s. Callers must have classified the analysis'
   * files first - in practice, by the PRE-phase {@link YamlFileTypeClassificationSensor}.
   */
  public List<InputFile> getInputFiles(Set<FileType> fileTypes) {
    return yamlFileTypeCache.getInputFiles(fileTypes);
  }

  /**
   * Classifies the analysis' YAML/JSON candidate files into the shared {@link YamlFileTypeCache}; a no-op if this
   * {@link FileSystem} was already classified. The scanner selects candidates ({@link #createCandidatePredicate}),
   * then each is classified by {@link #classify}. Hidden files are queried separately, ungated by
   * {@link #candidateLanguages()} - see {@link #classify}.
   */
  public void classifyInputFiles(SensorContext sensorContext, DurationStatistics durationStatistics) {
    var fileSystem = sensorContext.fileSystem();
    var sonarRuntime = sensorContext.runtime();
    if (yamlFileTypeCache.hasCacheDataFor(fileSystem)) {
      return;
    }
    yamlFileTypeCache.clearAndStartClassifyingFor(fileSystem);

    dispatchTimers(durationStatistics);
    var predicates = fileSystem.predicates();
    boolean hiddenFilesAnalysisSupported = isHiddenFilesAnalysisSupported(sonarRuntime);

    var candidatePredicate = createCandidatePredicate(predicates);
    if (hiddenFilesAnalysisSupported) {
      // Hidden files skip only the candidateLanguages() gate above, not a predicate's own language gate: a hidden
      // .github/workflows file must still satisfy GithubActionsFilePredicate's own hasLanguages(...) check, since it's
      // the only predicate tried against hidden files (see #classify).
      var hiddenFileCandidates = predicates.and(
        IndexedFile::isHidden,
        predicates.hasType(InputFile.Type.MAIN),
        predicates.hasLanguages(IacLanguage.YAML.getKey(), IacLanguage.GITHUB_ACTIONS.getKey()));
      candidatePredicate = predicates.or(
        predicates.and(inputFile -> !inputFile.isHidden(), candidatePredicate),
        hiddenFileCandidates);
    }

    var finalCandidatePredicate = candidatePredicate;
    durationStatistics.time("Scanner file retrieval",
      () -> fileSystem.inputFiles(finalCandidatePredicate).forEach(inputFile -> classify(inputFile, hiddenFilesAnalysisSupported)));

    yamlFileTypeCache.logClassifiedCount();
  }

  private FilePredicate createCandidatePredicate(FilePredicates predicates) {
    return predicates.and(
      predicates.hasType(InputFile.Type.MAIN),
      predicates.or(
        predicates.hasLanguages(candidateLanguages().toArray(new String[0])),
        predicates.matchesPathPattern(HelmFilePredicate.TPL_TEMPLATE_PATH_PATTERN),
        jvmConfigFilePredicate.getPatternBasedCandidates()));
  }

  /**
   * Assigns the candidate its {@link FileType}: first matching predicate wins, early return. A hidden file is matched
   * only against {@link #githubActionsFilePredicate} - the only sensor currently matching on hidden files.
   */
  private void classify(InputFile inputFile, boolean hiddenFilesAnalysisSupported) {
    var order = hiddenFilesAnalysisSupported && inputFile.isHidden() ? List.of(githubActionsFilePredicate) : filePredicatesOrder();
    for (YamlFileTypePredicate predicate : order) {
      if (predicate.apply(inputFile)) {
        yamlFileTypeCache.putIfUncached(inputFile, predicate.fileType());
        return;
      }
    }
  }
}
