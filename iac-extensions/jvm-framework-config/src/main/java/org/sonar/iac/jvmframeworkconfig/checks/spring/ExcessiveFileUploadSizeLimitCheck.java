/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.jvmframeworkconfig.checks.spring;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.jvmframeworkconfig.checks.common.AbstractSensitiveKeyCheck;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

@Rule(key = "S5693")
public class ExcessiveFileUploadSizeLimitCheck extends AbstractSensitiveKeyCheck {
  private static final String MESSAGE_FORMAT = "The content length limit of %s bytes is greater than the defined limit of %d; make sure " +
    "it is safe here.";
  private static final Set<String> SENSITIVE_KEYS = Set.of(
    "spring.servlet.multipart.max-file-size",
    "spring.servlet.multipart.max-request-size");
  private static final long MULTIPLIER = 1024;
  private static final Pattern DATA_SIZE_PATTERN = Pattern.compile("^(?<value>[+\\-]?\\d+)(?<suffix>[a-zA-Z]{0,2})$");
  private static final long BYTES_IN_KB = MULTIPLIER;
  private static final long BYTES_IN_MB = BYTES_IN_KB * MULTIPLIER;
  private static final long BYTES_IN_GB = BYTES_IN_MB * MULTIPLIER;
  private static final long BYTES_IN_TB = BYTES_IN_GB * MULTIPLIER;

  // size in bytes, taken from `ExcessiveContentRequestCheck` in sonar-java
  private static final long DEFAULT_LIMIT = 8_388_608;
  @RuleProperty(key = "fileUploadSizeLimit",
    defaultValue = "" + DEFAULT_LIMIT)
  public long fileUploadSizeLimit = DEFAULT_LIMIT;

  @Override
  protected Set<String> sensitiveKeys() {
    return SENSITIVE_KEYS;
  }

  @Override
  protected void checkValue(CheckContext ctx, Tuple tuple, String value) {
    var valueBytes = sizeBytes(value);
    if (valueBytes != null && valueBytes > fileUploadSizeLimit) {
      ctx.reportIssue(tuple, MESSAGE_FORMAT.formatted(valueBytes, fileUploadSizeLimit));
    }
  }

  /**
   * Parses a string representing data size similarly to
   * <a href=https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/util/unit/DataSize.java>Spring's DataSize</a>.
   */
  @CheckForNull
  static Long sizeBytes(String input) {
    var normalized = input.toLowerCase(Locale.ROOT);

    var matcher = DATA_SIZE_PATTERN.matcher(normalized);

    if (matcher.matches()) {
      var suffix = matcher.group("suffix");
      var value = matcher.group("value");
      return switch (suffix) {
        case "b", "" -> Long.parseLong(value);
        case "kb" -> Long.parseLong(value) * BYTES_IN_KB;
        case "mb" -> Long.parseLong(value) * BYTES_IN_MB;
        case "gb" -> Long.parseLong(value) * BYTES_IN_GB;
        case "tb" -> Long.parseLong(value) * BYTES_IN_TB;
        default -> null;
      };
    }
    return null;
  }
}
