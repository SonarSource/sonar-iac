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
package org.sonar.iac.common.checks.policy;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpRestrictedAdminAccessCheckUtils {

  public static final String MESSAGE = "Restrict IP addresses authorized to access administration services.";
  public static final String ALL_IPV4 = "0.0.0.0/0";
  public static final String ALL_IPV6 = "::/0";
  public static final int SSH_PORT = 22;
  public static final int RDP_PORT = 3389;
  public static final Set<String> SENSITIVE_PORTS = Set.of("*", String.valueOf(SSH_PORT), String.valueOf(RDP_PORT));
  private static final Pattern PORT_RANGE_PATTERN = Pattern.compile("^(?<from>\\d{1,5})-(?<to>\\d{1,5})$");

  private IpRestrictedAdminAccessCheckUtils() {
    // utils class
  }

  public static boolean rangeContainsSshOrRdpPort(String range) {
    if (range.contains("-")) {
      Matcher m = PORT_RANGE_PATTERN.matcher(range);
      if (m.find()) {
        return rangeContainsSshOrRdpPort(portFromMatch(m, "from"), portFromMatch(m, "to"));
      }
      return false;
    } else {
      return SENSITIVE_PORTS.contains(range);
    }
  }

  /**
   * Extract port as integer from range pattern matcher
   */
  private static int portFromMatch(Matcher m, String group) {
    return Integer.parseInt(m.group(group));
  }

  public static boolean rangeContainsSshOrRdpPort(int from, int to) {
    return (SSH_PORT >= from && SSH_PORT <= to) || (RDP_PORT >= from && RDP_PORT <= to);
  }
}
