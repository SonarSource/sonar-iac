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
package org.sonar.iac.common.checks;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecretClassifierTest {

  // One representative value per skip pattern / exact value, grouped by category. Drives both the
  // "classified as non-secret" check and the coverage check below, so adding a pattern without a
  // sample here fails coverageShouldExerciseEveryPatternAndExactValue.
  static final List<String> KNOWN_NON_SECRETS = List.of(
    // FAKE_VALUE
    "abc", // minimum length
    "samplepassword", "EXAMPLE_SECRET", "deadbeef", "qwerty", // fake words (substring)
    "password1234", "passwd", // password-like
    "undefined", "true", "null", // boolean / null / scalar literals
    "yourpassword", // starts with "your"
    "abbbbc", // same character 4 times
    "111111", // same character start to end
    "1fj28...askn3i", // masked
    "TODO: replace me", "FIXME", // reminder/placeholder prefix
    // SECRET (exact match, case-insensitive)
    "hunter2", "letmein", "secret", "abc123",
    "admin", "changeme", "changeit", "unknown", "optional", "enabled", "disabled", "string", "random", "token", "pass",
    // PLACEHOLDER
    "${secret}", "value-${pwd}", "#{{secret}}", "((db-password))",
    "$(echo $PASSWORD)", "`echo $PASSWORD`", "$foo_bar",
    "{secret}", "%{secret}", "{{secret}}",
    "System.getenv(\"secret\")", "process.env.MY_SECRET", "%GITHUB_TOKEN%", "config['secret']", "Read-Host",
    "<password>", "(password)", "[password]", "%(password)s", "@variables('name')",
    // ENCRYPTED
    "encrypted:YWJjZGVm", "{cipher}1e3faa2cdab2deae117dca102e52922a", "enc[QUJDRA==]", "ENC{abcdef}", "%enc{QUJDRA==}", "ENC(abcdef)",
    // REFERENCE
    "arn:aws:secretsmanager:us-east-1:123456789012:secret:db-pass", "op://vault/item/password", "VAULT[path/to/secret access_token]",
    // STRUCTURED_FORMAT
    "/var/keys/gsa-key.json", "v1.2.3", ">=1.0.0", "~1.4.5-alpha", "4.0.9(@types/node@22.13.4)");

  static Stream<String> knownNonSecrets() {
    return KNOWN_NON_SECRETS.stream();
  }

  @ParameterizedTest
  @MethodSource("knownNonSecrets")
  void shouldClassifyKnownNonSecrets(String value) {
    assertThat(SecretClassifier.isKnownNonSecret(value)).isTrue();
  }

  @Test
  void coverageShouldExerciseEveryPatternAndExactValue() {
    for (Pattern pattern : SecretClassifier.allPatterns()) {
      assertThat(KNOWN_NON_SECRETS)
        .as("no sample exercises pattern: %s", pattern.pattern())
        .anyMatch(sample -> pattern.matcher(sample).find());
    }
    for (String exact : SecretClassifier.exactMatchValues()) {
      assertThat(KNOWN_NON_SECRETS)
        .as("no sample exercises exact value: %s", exact)
        .anyMatch(sample -> sample.equalsIgnoreCase(exact));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Xk9Lm2Qp7Rs4Tv1Wz0",
    "9f8e7d6c5b4a392817",
    "Tr0ub4dor&3xpl0!t"
  })
  void shouldNotClassifyRealisticTokensAsNonSecrets(String value) {
    assertThat(SecretClassifier.isKnownNonSecret(value)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // Credential words are matched only as whole values, so a value that merely contains one stays a candidate.
    "admin1",
    "sonarPass",
    "hardcoded-pass",
    "another-secret",
    "mytoken123"
  })
  void shouldNotExcludeValuesMerelyContainingCredentialWords(String value) {
    assertThat(SecretClassifier.isKnownNonSecret(value)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "VAULT[path/to/secret access_token]",
    "ENC{abcdef}",
    "4.0.9(@types/node@22.13.4)",
    "/var/keys/gsa-key.json",
    "TODO: replace with a real token"
  })
  void shouldClassifyNewlyCoveredFormats(String value) {
    assertThat(SecretClassifier.isKnownNonSecret(value)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "${secret}",
    "Xk9Lm2Qp7Rs4Tv1Wz0"
  })
  void contextOverloadShouldMatchBareValueOverload(String value) {
    assertThat(SecretClassifier.isKnownNonSecret(value, SecretClassifier.Context.empty()))
      .isEqualTo(SecretClassifier.isKnownNonSecret(value));
  }

  @Test
  void shouldNotClassifyNullAsNonSecret() {
    assertThat(SecretClassifier.isKnownNonSecret(null)).isFalse();
    assertThat(SecretClassifier.isKnownNonSecret(null, SecretClassifier.Context.empty())).isFalse();
  }

  @Test
  void emptyContextShouldBeSingleton() {
    assertThat(SecretClassifier.Context.empty()).isSameAs(SecretClassifier.Context.empty());
  }
}
