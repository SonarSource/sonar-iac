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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.springconfig.tree.api.Profile;
import org.sonar.iac.springconfig.tree.api.Tuple;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck implements IacCheck {
  private static final String ISSUE_MESSAGE = "Change this code to use a stronger protocol.";
  private static final String SECONDARY_LOCATION_MESSAGE = "Other weak protocol.";
  private static final Set<String> SENSITIVE_KEYS = Set.of(
    "server.ssl.enabled-protocols",
    "spring.ssl.bundle.jks.server.options.enabled-protocols");
  private static final Set<String> SENSITIVE_VALUES = Set.of(
    "TLSv1.0", "TLSv1.1");
  private static final Pattern SENSITIVE_KEYS_PATTERN = Pattern.compile("(?<key>" + String.join("|", SENSITIVE_KEYS) + ")\\[\\d+]");

  @Override
  public void initialize(InitContext init) {
    init.register(Profile.class, WeakSSLProtocolCheck::checkProfile);
  }

  private static void checkProfile(CheckContext ctx, Profile profile) {
    Map<String, List<Tuple>> sensitiveTuples = new HashMap<>();

    for (Tuple tuple : profile.properties()) {
      var matcher = SENSITIVE_KEYS_PATTERN.matcher(tuple.key().value().value());
      if (matcher.matches() && hasSensitiveValue(tuple)) {
        String key = matcher.group("key");
        sensitiveTuples.computeIfAbsent(key, k -> new ArrayList<>()).add(tuple);
      }
    }

    sensitiveTuples.forEach((key, value) -> reportSensitiveTuples(ctx, value));
  }

  private static boolean hasSensitiveValue(Tuple tuple) {
    var scalarValue = tuple.value();
    return scalarValue != null && SENSITIVE_VALUES.contains(scalarValue.value().value());
  }

  private static void reportSensitiveTuples(CheckContext ctx, List<Tuple> sensitiveTuples) {
    if (sensitiveTuples.isEmpty()) {
      return;
    }
    List<SecondaryLocation> secondaryLocations = sensitiveTuples.stream()
      .skip(1)
      .map(tuple -> new SecondaryLocation(tuple, SECONDARY_LOCATION_MESSAGE))
      .toList();

    ctx.reportIssue(sensitiveTuples.get(0), ISSUE_MESSAGE, secondaryLocations);
  }
}
