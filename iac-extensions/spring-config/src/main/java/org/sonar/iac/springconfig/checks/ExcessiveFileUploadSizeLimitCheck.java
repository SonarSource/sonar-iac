/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.springconfig.checks;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.springconfig.tree.api.Scalar;
import org.sonar.iac.springconfig.tree.api.SyntaxToken;
import org.sonar.iac.springconfig.tree.api.Tuple;

@Rule(key = "S5693")
public class ExcessiveFileUploadSizeLimitCheck implements IacCheck {
  private static final String MESSAGE_FORMAT = "The content length limit of %s bytes is greater than the defined limit of %d; make sure it is safe here.";
  private static final List<String> SENSITIVE_KEYS = List.of(
    "spring.servlet.multipart.max-file-size",
    "spring.servlet.multipart.max-request-size");
  private static final long MULTIPLIER = 1024;
  private static final int SUFFIX_LENGTH = 2;

  // size in bytes, taken from `ExcessiveContentRequestCheck` in sonar-java
  private static final long DEFAULT_LIMIT = 8_388_608;
  @RuleProperty(key = "fileUploadSizeLimit")
  public long fileUploadSizeLimit = DEFAULT_LIMIT;

  @Override
  public void initialize(InitContext init) {
    init.register(Tuple.class, this::checkFileUploadSizeLimit);
  }

  private void checkFileUploadSizeLimit(CheckContext checkContext, Tuple tuple) {
    var key = tuple.key().value().value();
    if (SENSITIVE_KEYS.contains(key)) {
      var valueString = Optional.ofNullable(tuple.value())
        .map(Scalar::value)
        .map(SyntaxToken::value)
        .orElse(null);

      if (valueString == null) {
        return;
      }

      var value = sizeBytes(valueString);
      if (value > fileUploadSizeLimit) {
        checkContext.reportIssue(tuple, MESSAGE_FORMAT.formatted(value, fileUploadSizeLimit));
      }
    }
  }

  static long sizeBytes(String input) {
    var normalized = input.toLowerCase(Locale.ROOT);
    var suffix = "";
    var value = normalized;
    if (normalized.length() > SUFFIX_LENGTH) {
      suffix = normalized.substring(normalized.length() - SUFFIX_LENGTH);
      value = normalized.substring(0, normalized.length() - SUFFIX_LENGTH);
    }
    return switch (suffix) {
      case "kb", "kilobytes", "ofkilobytes" -> Long.parseLong(value) * MULTIPLIER;
      case "mb", "megabytes", "ofmegabytes" -> Long.parseLong(value) * MULTIPLIER * MULTIPLIER;
      case "gb", "gigabytes", "ofgigabytes" -> Long.parseLong(value) * MULTIPLIER * MULTIPLIER * MULTIPLIER;
      case "tb", "terabytes", "ofterabytes" -> Long.parseLong(value) * MULTIPLIER * MULTIPLIER * MULTIPLIER * MULTIPLIER;
      default -> Long.parseLong(normalized);
    };
  }
}
