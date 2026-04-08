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
package org.sonar.iac.jvmframeworkconfig.checks.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.checks.CommonExcludedPatterns;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

import static org.sonar.iac.jvmframeworkconfig.tree.utils.JvmFrameworkConfigUtils.getStringValue;

public abstract class AbstractHardcodedSecrets extends AbstractSensitiveKeyCheck {
  protected static final String MESSAGE = "Revoke and change this password, as it is compromised.";
  // Matches one or more dot-terminated named config segments (e.g. "mydb." or "tenant.region.").
  // Use the possessive variant NAMED_SEGMENT_PATTERN when the suffix is a plain word (e.g. "password", "secret") — no backtracking needed.
  protected static final String NAMED_SEGMENT_PATTERN = "([\\w-]++\\.)++";
  // Use the non-possessive NAMED_SEGMENT_PATTERN_NP variant when the suffix contains hyphens or dots (e.g. "proxy-password", "api.key"),
  // so the engine can backtrack if it over-consumes segments.
  protected static final String NAMED_SEGMENT_PATTERN_NP = "([\\w-]++\\.)+";
  protected static final Pattern PATTERN_PASSWORD_IN_CONNECTION_STRING = Pattern.compile("(?:;|^)AccountKey=(?<password>[a-zA-Z0-9+/=]{60,})(?:;|$)");
  // Handles simple schemes (e.g. redis://, mongodb://) and compound schemes (e.g. jdbc:postgresql://, r2dbc:driver://).
  protected static final Pattern PATTERN_PASSWORD_IN_URL = Pattern.compile(
    "[a-zA-Z][a-zA-Z0-9+.-]*+(?::[a-zA-Z][a-zA-Z0-9+.-]*+)*+://(?<username>[^:@]++):(?<password>.+)@.++");
  // Handles JDBC URLs with password as a query parameter (e.g. ?password=secret or &password=secret).
  protected static final Pattern PATTERN_PASSWORD_IN_JDBC_URL = Pattern.compile("[?&]password=(?<password>[^&\\s]++)");
  protected static final String PASSWORD_GROUP = "password";

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    if (isHardcoded(value)) {
      ctx.reportIssue(tuple.value(), MESSAGE);
    }
  }

  private static boolean isHardcoded(String value) {
    return !(value.isEmpty() || CommonExcludedPatterns.isCommonExcludedPattern(value));
  }

  protected static boolean checkValueWithPattern(CheckContext ctx, Pattern pattern, Tuple tuple) {
    String valueString = getStringValue(tuple);
    if (valueString == null) {
      return false;
    }

    var matcher = pattern.matcher(valueString);
    if (!matcher.find()) {
      return false;
    }

    String password = matcher.group(PASSWORD_GROUP);
    if (password == null || CommonExcludedPatterns.isCommonExcludedPattern(password)) {
      return false;
    }

    var textRange = computePasswordTextRange(matcher, tuple.value());
    if (textRange == null) {
      return false;
    }

    ctx.reportIssue(textRange, MESSAGE);
    return true;
  }

  @Nullable
  protected static TextRange computePasswordTextRange(Matcher matcher, @Nullable HasTextRange hasTextRange) {
    if (hasTextRange == null) {
      return null;
    }
    // If the value is split on multiple lines, we cannot recover the exact password location, so we just highlight the whole string
    if (hasTextRange.textRange().start().line() != hasTextRange.textRange().end().line()) {
      return hasTextRange.textRange();
    }
    int startPassword = matcher.start(PASSWORD_GROUP);
    int endPassword = matcher.end(PASSWORD_GROUP);
    int startLine = hasTextRange.textRange().start().line();
    int startLineOffset = hasTextRange.textRange().start().lineOffset() + startPassword;
    int endLine = hasTextRange.textRange().start().line();
    int endLineOffset = hasTextRange.textRange().start().lineOffset() + endPassword;
    return TextRanges.range(startLine, startLineOffset, endLine, endLineOffset);
  }
}
