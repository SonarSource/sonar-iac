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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Classifies string values against a shared set of "skip" patterns: values that look like fake credentials,
 * placeholders, variable references, or encrypted markers and should therefore not be reported as hardcoded secrets.
 *
 * <p>The patterns are defined in this class, grouped by {@link Category}, and derived from sonar-text's
 * {@code patternNot.yaml}. This is the initial, IaC-local home of that shared logic; it is intended to be extracted
 * to sonar-analyzer-commons later so other analyzers can reuse it.
 *
 * <p>Classification takes the candidate value plus a {@link Context}. The context is an extensible carrier for
 * surrounding information (for example the key a value was found under); it is empty today and exists so the
 * classification entry point can gain context-aware behavior later without changing its signature. A convenience
 * overload classifies a bare value with an empty context.
 *
 * <p>Example:
 * <pre>{@code
 * SecretClassifier.isKnownNonSecret("${db_password}"); // true  -> variable interpolation
 * SecretClassifier.isKnownNonSecret("example-secret"); // true  -> fake word
 * SecretClassifier.isKnownNonSecret("Xk9Lm2Qp7Rs4Tv"); // false -> looks like a real token
 * }</pre>
 */
public final class SecretClassifier {

  /**
   * Coarse group a skip pattern belongs to. Categories make the configuration enumerable and give a future export and
   * per-category telemetry a stable handle. Kept package-private until a consumer needs it.
   */
  enum Category {
    /** Trivially fake or weak literals: fake words, password-like values, repeated, too-short, or masked strings. */
    FAKE_VALUE,
    /** Well-known literal placeholder secrets matched in full, e.g. {@code hunter2}, {@code letmein}. */
    SECRET,
    /** Templating, interpolation, variable references and env/config lookups where the value comes from elsewhere. */
    PLACEHOLDER,
    /** Encrypted markers wrapping a ciphertext, e.g. {@code {cipher}...}, {@code enc[...]}. */
    ENCRYPTED,
    /** Pointers into an external secret store rather than a literal, e.g. an AWS Secrets Manager ARN, {@code op://}. */
    REFERENCE,
    /** Recognizable structured values that are not credentials, e.g. filesystem paths. */
    STRUCTURED_FORMAT
  }

  /**
   * The skip patterns of a single {@link Category}, grouped to keep the configuration readable. Each pattern is
   * compiled case-insensitively and keeps its own source via {@link Pattern#pattern()} and flags via
   * {@link Pattern#flags()}, so the whole set can be serialized for non-JVM consumers later.
   */
  record PatternGroup(Category category, List<Pattern> patterns) {

    static PatternGroup of(Category category, String... regexes) {
      return new PatternGroup(category, Arrays.stream(regexes).map(SecretClassifier::compile).toList());
    }
  }

  /** Values of a single {@link Category} matched in full, case-insensitively, via a set rather than a regex. */
  record ExactMatchGroup(Category category, Set<String> values) {
  }

  private static final List<PatternGroup> PATTERN_GROUPS = List.of(

    // Trivially fake or weak literal values.
    PatternGroup.of(Category.FAKE_VALUE,
      // Expect minimum length of 6 characters
      "^.{1,5}$",
      // Words usually found in fake secrets, e.g. "samplepassword", "EXAMPLE_SECRET"
      "sample|example|placeholder|replace|change|foo|bar|test|fake|abcd",
      "redacted|cafebabe|deadbeef|whatever|123456|default|dummy|qwerty|setting|obfuscated",
      // Password-like words, e.g. "password", "passwd", "pass", "password1234"
      "^(my)?pass(word|wd)?\\d{0,5}+$",
      // Boolean / null / scalar literals, e.g. "password = undefined", "enabled: true"
      "^(?:none|undefined|null|true|false|yes|no|1|0)$",
      // Starts with "your", e.g. "yourpassword", "your_super_secret"
      "^your",
      // Same character 4 times in a row, e.g. "abbbbc"
      "(?<char>[\\w\\*\\.])\\k<char>{3}",
      // Same character repeated from start to end, e.g. "aa", "111111"
      "^(?<repeated>.)\\k<repeated>*+$",
      // A secret being masked or shortened, e.g. "1fj28...askn3i"
      "\\.\\.\\.",
      // Code-reminder placeholder markers left as a value (see the regex)
      "^(?:todo|fixme)\\b"),

    // Templating, interpolation and env/config lookups where the value comes from elsewhere.
    PatternGroup.of(Category.PLACEHOLDER,
      // Variable interpolation, e.g. "${secret}", "$${camel}", starting with the interpolation
      "^(?:\\\\)?\\${1,2}\\{[^}]++\\}",
      // Variable interpolation ending with the interpolation
      "(?:\\\\)?\\${1,2}\\{[^}]++\\}$",
      // Variable interpolation, e.g. "#{{secret}}", "##(password)"
      "^\\#{1,2}[{(]",
      // Concourse ((vars))
      "^\\(\\(.*\\)\\)$",
      // Shell command substitution, e.g. "$(echo $PASSWORD)"
      "^\\$\\(",
      // Shell command substitution, e.g. "`echo $PASSWORD`"
      "^`[^`]++`$",
      // Variable references, e.g. "$a", "$foo_bar", "$$R", "$password$"
      "^(?:\\\\)?\\${1,2}\\w+\\${0,2}$",
      // Variable interpolation in templates, e.g. "{secret}", "%{secret}"
      "^%?\\{[^}]++\\}$",
      // Variable interpolation in templates, e.g. "{{secret}}", "{{{password}}}"
      "^\\{{2,}[^}]++\\}{2,}",
      // Environment variable access, e.g. "System.getenv(\"secret\")", "ENV['SECRET']"
      "\\b(get)?env(iron)?\\b",
      // Node.js environment variable access, e.g. "process.env.MY_SECRET"
      "process\\.env\\.",
      // Environment variables with %...% syntax, e.g. "%GITHUB_TOKEN%"
      "^%[^%]++%$",
      // Configuration access, e.g. "config['secret']", "config('password')"
      "config[\\(\\[]",
      // PowerShell cmdlet to read console input
      "Read-Host",
      // Angle-bracketed placeholders, e.g. "<password>"
      "^<[\\w\\.\\t -]{1,10}>",
      "^<[^>]++>$",
      // Normal (potentially escaped) bracketed placeholders, e.g. "(password)", jq "(.password)"
      "^\\\\?\\([^)]++\\)$",
      // Square-bracketed placeholders, e.g. "[password]"
      "^\\[[^\\]]++\\]$",
      // Python format string placeholders, e.g. "%(password)s"
      "^%\\([^)]++\\)s$",
      // Azure Logic Apps runtime expressions, e.g. "@variables('name')", "@body('action')"
      "^@\\w++\\([^)]*+\\)$"),

    // Encrypted markers wrapping a ciphertext.
    PatternGroup.of(Category.ENCRYPTED,
      // Encrypted secrets, encrypted:<base64>
      "^encrypted:[a-zA-Z0-9+\\/]++={0,2}$",
      // Encrypted spring cloud secrets, e.g. "{cipher}1e3faa2cdab2deae117dca102e52922a"
      "^\\{cipher\\}",
      // Encrypted string literals, e.g. "enc[...]", "enc{...}", "%enc{...}", "ENC(...)"
      "^enc\\[",
      "^%?enc\\{",
      "^enc\\([^)]*+\\)$"),

    // Pointers into an external secret store rather than a literal value.
    PatternGroup.of(Category.REFERENCE,
      // ARN to an AWS Secrets Manager secret
      "^arn:aws:secretsmanager:",
      // 1Password URLs, e.g. "op://vault/secret"
      "^op:\\/[\\S\\ ]++$",
      // HashiCorp/Cirrus Vault references, e.g. "VAULT[path/to/secret access_token]"
      "^vault\\["),

    // Recognizable structured values that are not credentials.
    PatternGroup.of(Category.STRUCTURED_FORMAT,
      // Filesystem paths with at least 3 segments, e.g. "/path/to/file.ext"
      "^(?:/[a-z0-9_.-]++){3,}+$",
      // Semantic version strings, optionally prefixed, e.g. "v1.2.3", ">=1.0.0", "~1.4.5-alpha" (semver.org regex)
      "^(?:>=?|<=?|[~^])?v?(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)"
        + "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
        + "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$",
      // Resolved / peer-annotated version strings from package lockfiles, e.g. "4.0.9(@types/node@22.13.4)".
      // Stopgap: such values may instead be excluded by ignoring lockfiles by path.
      "^v?\\d++(?:\\.\\d++)++(?:\\([^()]*+\\))++$"));

  // Flattened once: isKnownNonSecret is on every check's hot path, so avoid re-flattening PATTERN_GROUPS per call.
  private static final List<Pattern> ALL_PATTERNS = PATTERN_GROUPS.stream()
    .flatMap(group -> group.patterns().stream())
    .toList();

  // Well-known placeholder secrets plus config/credential vocabulary, matched in full (case-insensitive) rather than
  // as substrings, so they cannot hide a real secret that merely contains one of these words.
  private static final ExactMatchGroup SECRET_VALUES = new ExactMatchGroup(Category.SECRET, Set.of(
    "hunter2", "letmein", "secret", "abc123",
    "admin", "changeme", "changeit", "unknown", "optional", "enabled", "disabled",
    "string", "random", "token", "pass"));

  // Context is an empty extension point today, so the analyzer sees instantiating it as pointless; the single shared
  // empty instance is intentional and lets empty() return a non-null context.
  @SuppressWarnings("java:S2440")
  private static final Context EMPTY_CONTEXT = new Context() {
  };

  private SecretClassifier() {
  }

  private static Pattern compile(String regex) {
    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Returns {@code true} when the value matches a known skip pattern, such as a fake value, a variable reference, or
   * an encrypted placeholder.
   *
   * @param candidate the string to classify, or {@code null}
   * @param context surrounding information; pass {@link Context#empty()} when none is available
   * @return {@code true} if the value is recognized as a non-secret; {@code false} for {@code null}
   */
  // context is unused today; it is the extension point that lets classification become context-aware later.
  @SuppressWarnings("java:S1172")
  public static boolean isKnownNonSecret(@Nullable String candidate, Context context) {
    if (candidate == null) {
      return false;
    }
    if (SECRET_VALUES.values().contains(candidate.toLowerCase(Locale.ROOT))) {
      return true;
    }
    for (Pattern pattern : ALL_PATTERNS) {
      if (pattern.matcher(candidate).find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Convenience overload that classifies a value with an empty {@link Context}.
   *
   * @param candidate the string to classify, or {@code null}
   * @return {@code true} if the value is recognized as a non-secret; {@code false} for {@code null}
   */
  public static boolean isKnownNonSecret(@Nullable String candidate) {
    return isKnownNonSecret(candidate, Context.empty());
  }

  /** Visible for testing: every configured skip pattern, so a coverage test can assert each one is exercised. */
  static List<Pattern> allPatterns() {
    return ALL_PATTERNS;
  }

  /** Visible for testing: the exact-match values. */
  static Set<String> exactMatchValues() {
    return SECRET_VALUES.values();
  }

  /**
   * Surrounding information passed alongside the candidate value to {@link #isKnownNonSecret(String, Context)}.
   *
   * <p>The interface is intentionally empty for now. It is a stable extension point: future accessors (for example the
   * key a value was found under, or the analyzed language) can be added without changing the classification signature.
   */
  public interface Context {

    /**
     * Returns a context that carries no additional information.
     *
     * @return the shared empty context
     */
    static Context empty() {
      return EMPTY_CONTEXT;
    }
  }
}
