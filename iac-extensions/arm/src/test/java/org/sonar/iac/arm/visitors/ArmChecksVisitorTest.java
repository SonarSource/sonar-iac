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
package org.sonar.iac.arm.visitors;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;
import org.sonar.iac.common.AbstractTestTree;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.languages.IacLanguage;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArmChecksVisitorTest {

  private static final String BICEP_FILE_LANGUAGE = "azureresourcemanager";
  private static final String JSON_FILE_LANGUAGE = "json";
  private static final Pattern HOW_TO_FIX = Pattern.compile("<h2>How to fix it in (?:(?:an|a|the)\\s)?(?<displayName>.*)</h2>");

  private final SensorContext sensorContext = mock(SensorContext.class);
  private final DurationStatistics statistics = new DurationStatistics(new MapSettings().asConfig());

  @ParameterizedTest
  @CsvSource({
    "S6378, true, bicep",
    "S6378, false, json_templates",
    "S8684, false, json_templates",
    "S6329, true, bicep",
    "S6329, false, arm_templates",
    "S4507, false, arm_templates",
    "S6413, false, arm_templates",
  })
  void shouldResolveContextKeyForRulesWithBicepContext(String rule, boolean isBicep, String expectedKey) {
    assertThat(ArmChecksVisitor.contextKeyFor(rule, isBicep)).isEqualTo(expectedKey);
  }

  @ParameterizedTest
  @ValueSource(strings = {"S6382", "S5332", "S4423", "S6388", "S9999"})
  void shouldNotResolveContextKeyForRulesWithoutBicepContext(String rule) {
    assertThat(ArmChecksVisitor.contextKeyFor(rule, true)).isNull();
    assertThat(ArmChecksVisitor.contextKeyFor(rule, false)).isNull();
  }

  @Test
  void shouldInjectBicepContextKeyOnBicepFile() {
    RuleKey ruleKey = RuleKey.of("azureresourcemanager", "S6378");
    InputFileContext ifc = scanReportingCheck(ruleKey, BICEP_FILE_LANGUAGE);

    verify(ifc).reportIssue(eq(ruleKey), any(), eq("message"), anyList(), eq("bicep"));
  }

  @Test
  void shouldInjectJsonTemplatesContextKeyOnJsonFile() {
    RuleKey ruleKey = RuleKey.of("azureresourcemanager", "S6378");
    InputFileContext ifc = scanReportingCheck(ruleKey, JSON_FILE_LANGUAGE);

    verify(ifc).reportIssue(eq(ruleKey), any(), eq("message"), anyList(), eq("json_templates"));
  }

  @Test
  void shouldInjectArmTemplatesContextKeyOnJsonFile() {
    RuleKey ruleKey = RuleKey.of("azureresourcemanager", "S6329");
    InputFileContext ifc = scanReportingCheck(ruleKey, JSON_FILE_LANGUAGE);

    verify(ifc).reportIssue(eq(ruleKey), any(), eq("message"), anyList(), eq("arm_templates"));
  }

  @Test
  void shouldNotInjectContextKeyForRuleWithoutBicepContext() {
    RuleKey ruleKey = RuleKey.of("azureresourcemanager", "S6382");
    InputFileContext ifc = scanReportingCheck(ruleKey, JSON_FILE_LANGUAGE);

    verify(ifc).reportIssue(eq(ruleKey), any(), eq("message"), anyList(), isNull());
  }

  @Test
  void shouldNotOverrideExplicitlyProvidedContextKey() {
    RuleKey ruleKey = RuleKey.of("azureresourcemanager", "S6378");
    // S6378 is in the set, but the check provides its own key: it must be preserved as-is.
    IacCheck check = init -> init.register(TestTree.class, (ctx, tree) -> ctx.reportIssue(tree, "message", List.of(), "explicit_key"));
    InputFileContext ifc = scan(check, ruleKey, BICEP_FILE_LANGUAGE);

    verify(ifc).reportIssue(eq(ruleKey), any(), eq("message"), anyList(), eq("explicit_key"));
  }

  /**
   * Guard against drift between the rule descriptions shipped with the analyzer and the registries hard-coded in
   * {@link ArmChecksVisitor}. Adding (or removing) an ARM rule with a {@code How to fix it in Bicep} context without updating the
   * registries will fail this test.
   */
  @Test
  void registriesShouldMatchShippedRuleDescriptions() throws Exception {
    Map<String, Set<String>> contextsByRule = howToFixContextKeysByRule();

    Set<String> rulesWithBicepContext = contextsByRule.entrySet().stream()
      .filter(entry -> entry.getValue().contains("bicep"))
      .map(Map.Entry::getKey)
      .collect(toSet());
    Set<String> rulesWithArmTemplatesContext = contextsByRule.entrySet().stream()
      .filter(entry -> entry.getValue().contains("arm_templates"))
      .map(Map.Entry::getKey)
      .collect(toSet());

    assertThat(ArmChecksVisitor.RULES_WITH_BICEP_AND_TEMPLATES_CONTEXTS)
      .as("rules with a 'How to fix it in Bicep' context")
      .isEqualTo(rulesWithBicepContext);
    assertThat(ArmChecksVisitor.RULES_USING_ARM_TEMPLATES_CONTEXT)
      .as("rules whose non-Bicep context is 'ARM Templates'")
      .isEqualTo(rulesWithArmTemplatesContext)
      // Every Bicep-context rule must also have a non-Bicep (JSON/ARM templates) context, otherwise the JSON branch is unreachable.
      .isSubsetOf(ArmChecksVisitor.RULES_WITH_BICEP_AND_TEMPLATES_CONTEXTS);
  }

  private InputFileContext scanReportingCheck(RuleKey ruleKey, String fileLanguage) {
    IacCheck check = init -> init.register(TestTree.class, (ctx, tree) -> ctx.reportIssue(tree.textRange(), "message"));
    return scan(check, ruleKey, fileLanguage);
  }

  private InputFileContext scan(IacCheck check, RuleKey ruleKey, String fileLanguage) {
    Checks<IacCheck> checks = mock(Checks.class);
    when(checks.all()).thenReturn(List.of(check));
    when(checks.ruleKey(check)).thenReturn(ruleKey);

    ArmChecksVisitor visitor = new ArmChecksVisitor(checks, statistics);
    InputFileContext ifc = inputFileContext(fileLanguage);
    visitor.scan(ifc, new TestTree());
    return ifc;
  }

  private InputFileContext inputFileContext(String fileLanguage) {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.language()).thenReturn(fileLanguage);
    InputFileContext ifc = spy(new InputFileContext(sensorContext, inputFile, IacLanguage.ARM));
    // The real reportIssue talks to the (mocked) SensorContext; we only assert the arguments it receives.
    doNothing().when(ifc).reportIssue(any(RuleKey.class), any(), anyString(), anyList(), nullable(String.class));
    return ifc;
  }

  private static Map<String, Set<String>> howToFixContextKeysByRule() throws Exception {
    URL dirUrl = ArmChecksVisitor.class.getResource("/org/sonar/l10n/azureresourcemanager/rules/azureresourcemanager");
    assertThat(dirUrl).as("ARM rule descriptions directory on the classpath").isNotNull();
    Path dir = Path.of(dirUrl.toURI());

    Map<String, Set<String>> contextsByRule = new HashMap<>();
    try (Stream<Path> files = Files.list(dir)) {
      for (Path file : files.filter(p -> p.getFileName().toString().matches("S\\d+\\.html")).toList()) {
        String rule = file.getFileName().toString().replace(".html", "");
        Set<String> keys = new HashSet<>();
        Matcher matcher = HOW_TO_FIX.matcher(Files.readString(file));
        while (matcher.find()) {
          keys.add(matcher.group("displayName").trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "_"));
        }
        if (!keys.isEmpty()) {
          contextsByRule.put(rule, keys);
        }
      }
    }
    return contextsByRule;
  }

  private static class TestTree extends AbstractTestTree {
    private final TextRange textRange = TextRanges.range(1, 0, 1, 4);

    @Override
    public TextRange textRange() {
      return textRange;
    }
  }
}
