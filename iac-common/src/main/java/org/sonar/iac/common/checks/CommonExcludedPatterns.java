/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.checks;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class define the PatternNot coming from
 * <a href="https://github.com/SonarSource/sonar-text-enterprise/blob/master/private/sonar-text-developer-plugin/src/main/resources/com/sonar/plugins/secrets/common/patternNot.yaml">
 * sonar-text</a>.
 * It is a set of pattern used to identify values that should not be considered as hardcoded credentials or secrets, in order to reduce FPs.
 */
public final class CommonExcludedPatterns {
  // Recognizable words we typically find
  public static final String RECOGNIZABLE_WORDS_PATTERN = "sample|example|foo|bar|test|fake|abcd|redacted|cafebabe|deadbeef|123456|admin|pass|changeme|changeit|secret|unknown|" +
    "optional|enabled|disabled|string|test|random|token";
  // Path-like strings with at least 3 slashes, e.g. "/path/to/file"
  public static final String PATH_PATTERN = "(?:/[a-z0-9_-]++){3,}+";

  // Set of patterns, move entries in above map if you want to reference them in another class.
  public static final Set<String> PATTERNS = Set.of(
    RECOGNIZABLE_WORDS_PATTERN,
    PATH_PATTERN,
    "^pass(?:word|wd)?$",
    "^(?:none|undefined|null|true|false|yes|no|1|0)$",
    "^your",
    // A character repeated 5 times or more
    "(?<char>[\\w\\*\\.])\\k<char>{4}+",
    // Same characters repeated from start to end
    "^(?<repeated>.)\\k<repeated>*+$",
    // Variable interpolation ${my_var}, #{{my_var}}, ((my_var)), $my_var, {my_var}, {{my_var}}
    "\\$\\{[^}]++\\}",
    "^\\#{1,2}+[{(]",
    "^\\(\\([^)]++\\)\\)",
    "^\\${1,2}+\\w+$",
    "%?\\{[^}]++\\}",
    "%?+\\{\\{[^}]++\\}\\}",
    "\\$_[0-9]++",
    // Shell command substitution $(...), `...`
    "^\\$\\(",
    "^`[^`]++`$",
    // Placeholders in strings: "%s", "%v"
    "^%[sv]$",
    // Environment variable access in various languages, e.g. "System.getenv("secret")", "os.Getenv("SECRET")", "ENV['SECRET']"
    "\\b(?:get)?env(?:iron)?\\b",
    // Node.js environment variables access, e.g. "process.env.MY_SECRET"
    "process\\.env\\.",
    // Configuration access in PHP and various languages, e.g. "config['secret']", "config('password')"
    "config[\\(\\[]",
    // Angle-bracketed placeholders, e.g. "<password>"
    "^<[\\w\\.\\t -]{1,10}+>",
    "^<[^>]++>$",
    // Versions look-alike strings, e.g. "v1.0", "2.3.4", "v1.2.3-alpha"
    "^v?+[0-9]{1,3}+(?:\\.[0-9]{1,3}+)++",
    // Encrypted string
    "^ENC\\([^)]*+\\)$",
    "^%ENC\\{[^}]*+\\}$",
    "^ENC\\[[^\\]]*+\\]$",
    // Looking like a file with extension
    "^[a-z0-9_]++\\.[a-z]{2,3}+$",
    // Combinations of words tied together with pipes, like crypt3|md5|sha512, can have surrounding quotes
    "^(?<optquote>'?)(?:[a-z0-9]++\\|)++[a-z0-9]++\\k<optquote>$",
    // Bicep json expressions
    "^\\[.*+\\]$",
    // Look like sentences (at least 3 words).
    "^(?:[a-z'-]++[ ,;:]*+){3,}+[.!?]?$");

  private static final Pattern PATTERNS_COMPILED = Pattern.compile(
    String.join("|", PATTERNS), Pattern.CASE_INSENSITIVE);

  private CommonExcludedPatterns() {
  }

  public static boolean isCommonExcludedPattern(String value) {
    return PATTERNS_COMPILED.matcher(value).find();
  }
}
