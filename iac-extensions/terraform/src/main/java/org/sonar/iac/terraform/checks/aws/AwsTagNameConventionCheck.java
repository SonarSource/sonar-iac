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
package org.sonar.iac.terraform.checks.aws;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

import static java.util.function.Predicate.not;
import static org.sonar.iac.terraform.checks.aws.utils.AwsUtils.getTagKeyStream;

@Rule(key = "S6273")
public class AwsTagNameConventionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Rename tag key \"%s\" to match the regular expression \"%s\".";
  public static final String DEFAULT = "^(([^:]++:)*+([A-Z][A-Za-z]*+))$";
  protected Predicate<String> isMismatchingKey;

  @RuleProperty(key = "format", description = "Regular expression used to check the tag keys against.", defaultValue = DEFAULT)
  public String format = DEFAULT;

  @Override
  public void initialize(InitContext init) {
    isMismatchingKey = not(Pattern.compile(format).asMatchPredicate());
    super.initialize(init);
  }

  @Override
  protected void registerResourceChecks() {
    register((ctx, resource) -> getTagKeyStream(resource)
      .filter(tagKey -> isMismatchingKey.test(tagKey.value()))
      .forEach(tagKey -> ctx.reportIssue(tagKey, MESSAGE.formatted(tagKey.value(), format))));
  }
}
