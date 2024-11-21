/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.jvmframeworkconfig.checks.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.jvmframeworkconfig.tree.api.Profile;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

public abstract class AbstractWeakSSLProtocolCheck implements IacCheck {
  protected static final String ISSUE_MESSAGE = "Change this code to use a stronger protocol.";
  protected static final String SECONDARY_LOCATION_MESSAGE = "Other weak protocol.";
  protected static final Set<String> SENSITIVE_VALUES = Set.of("TLSv1.0", "TLSv1.1");

  private Predicate<String> sensitiveKeysPattern;
  private Pattern sensitiveArrayKeysPattern;

  protected abstract Set<String> sensitivePatternKeys();

  protected abstract Set<String> sensitivePatternArrayKeys();

  @Override
  public void initialize(InitContext init) {
    sensitiveKeysPattern = Pattern.compile(String.join("|", sensitivePatternKeys())).asMatchPredicate();
    sensitiveArrayKeysPattern = Pattern.compile("(?<key>" + String.join("|", sensitivePatternArrayKeys()) + ")\\[\\d+]");
    init.register(Tuple.class, this::checkTuple);
    init.register(Profile.class, this::checkProfile);
  }

  private void checkTuple(CheckContext ctx, Tuple tuple) {
    var key = tuple.key().value().value();
    if (sensitiveKeysPattern.test(key) && hasSensitiveValue(tuple)) {
      ctx.reportIssue(tuple.value(), ISSUE_MESSAGE);
    }
  }

  private void checkProfile(CheckContext ctx, Profile profile) {
    Map<String, List<Tuple>> sensitiveTuples = new HashMap<>();

    for (Tuple tuple : profile.properties()) {
      var matcher = sensitiveArrayKeysPattern.matcher(tuple.key().value().value());
      if (matcher.matches() && hasSensitiveValue(tuple)) {
        String key = matcher.group("key");
        sensitiveTuples.computeIfAbsent(key, k -> new ArrayList<>()).add(tuple);
      }
    }

    sensitiveTuples.forEach((key, value) -> reportSensitiveTuples(ctx, value));
  }

  private static boolean hasSensitiveValue(Tuple tuple) {
    var scalarValue = tuple.value();
    if (scalarValue != null) {
      var values = scalarValue.value().value().split(",");
      for (String value : values) {
        var trimmed = value.trim();
        if (SENSITIVE_VALUES.contains(trimmed)) {
          return true;
        }
      }
    }
    return false;
  }

  private static void reportSensitiveTuples(CheckContext ctx, List<Tuple> sensitiveTuples) {
    List<SecondaryLocation> secondaryLocations = sensitiveTuples.stream()
      .skip(1)
      .map(tuple -> new SecondaryLocation(tuple.value(), SECONDARY_LOCATION_MESSAGE))
      .toList();

    ctx.reportIssue(sensitiveTuples.get(0).value(), ISSUE_MESSAGE, secondaryLocations);
  }
}
