/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.cloudformation.checks;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.yaml.tree.ScalarTree;

import static org.sonar.iac.cloudformation.checks.utils.TagUtils.getTagKeyStream;

@Rule(key = "S7452")
public class AwsTagNameCheck extends AbstractResourceCheck {
  protected static final String MESSAGE = "Rename tag key \"%s\" to comply with required format.";
  private static final Predicate<String> HAS_VALID_FORMAT = Pattern.compile("^[\\p{IsAlphabetic}\\d\\p{IsWhitespace}_.:/=+@\\-\"]++$").asMatchPredicate();
  private static final int TAG_MAX_LENGTH = 128;
  private static final String TAG_RESERVED_PREFIX = "aws:";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    var properties = resource.properties();
    if (properties != null) {
      getTagKeyStream(properties)
        .filter(tagKey -> !isValidKey(tagKey))
        .forEach(key -> ctx.reportIssue(key, MESSAGE.formatted(key.value())));
    }
  }

  private static boolean isValidKey(ScalarTree tagKey) {
    var literal = tagKey.value();
    if (literal.length() > TAG_MAX_LENGTH || literal.startsWith(TAG_RESERVED_PREFIX)) {
      return false;
    }
    return HAS_VALID_FORMAT.test(literal);
  }
}
