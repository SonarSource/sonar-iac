/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.checks.ipaddress;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

public class IpAddressValidator {

  // Make sure using this hardcoded IP address is safe here.
  @SuppressWarnings("java:S1313")
  private static final List<Range> SAFE_RANGES = List.of(
    ipRange("10.0.0.0", "10.255.255.255"),
    ipRange("100.64.0.0", "100.127.255.255"),
    ipRange("169.254.0.0", "169.254.255.255"),
    ipRange("172.16.0.0", "172.31.255.255"),
    ipRange("192.0.0.0", "192.0.0.255"),
    ipRange("192.0.2.0", "192.0.2.255"),
    ipRange("192.168.0.0", "192.168.255.255"),
    ipRange("198.18.0.0", "198.19.255.255"),
    ipRange("198.51.100.0", "198.51.100.255"),
    ipRange("203.0.113.0", "203.0.113.255"),
    ipRange("240.0.0.0", "240.255.255.254"));

  private static final String DEFAULT_START_IP = "0.0.0.0";
  private static final String DEFAULT_END_IP = "255.255.255.255";

  @Nullable
  private final ArmTree startIpAddress;

  @Nullable
  private final ArmTree endIpAddress;

  public IpAddressValidator(@Nullable ArmTree startIpAddress, @Nullable ArmTree endIpAddress) {
    this.startIpAddress = startIpAddress;
    this.endIpAddress = endIpAddress;
  }

  public void reportIssueIfPublicIPAddress(CheckContext ctx, String message, String secondaryLocationMessage) {
    ValidationResult validation = validateTrees();

    if (validation.isValid()) {
      long startLong = validation.startIp().toLong();
      long endInt = validation.endIp().toLong();
      for (Range safeRange : SAFE_RANGES) {
        if (safeRange.contains(startLong) && safeRange.contains(endInt)) {
          return;
        }
      }
      reportIssue(ctx, message, secondaryLocationMessage);
    }
  }

  private ValidationResult validateTrees() {
    if (startIpAddress == null && endIpAddress == null) {
      return new ValidationResult(false, null, null);
    }
    String start = valueOrElseDefaultIfStringLiteralOrNull(startIpAddress, DEFAULT_START_IP);
    String end = valueOrElseDefaultIfStringLiteralOrNull(endIpAddress, DEFAULT_END_IP);
    if (start == null && end == null) {
      return new ValidationResult(false, null, null);
    }
    if (start == null) {
      start = DEFAULT_START_IP;
    }
    if (end == null) {
      end = DEFAULT_END_IP;
    }

    boolean isStartValid = Ip.isAddressValid(start);
    boolean isEndValid = Ip.isAddressValid(end);
    if (!isStartValid && !isEndValid) {
      return new ValidationResult(false, null, null);
    }
    return new ValidationResult(
      true,
      isStartValid ? new Ip(start) : new Ip(DEFAULT_START_IP),
      isEndValid ? new Ip(end) : new Ip(DEFAULT_END_IP));
  }

  private void reportIssue(CheckContext ctx, String message, String secondaryLocationMessage) {
    Tree tree = (startIpAddress != null) ? startIpAddress : endIpAddress;
    Tree secondary = (startIpAddress != null && endIpAddress != null) ? endIpAddress : null;

    if (secondary != null) {
      SecondaryLocation secondaryLocation = new SecondaryLocation(secondary, secondaryLocationMessage);
      ctx.reportIssue(tree, message, List.of(secondaryLocation));
    } else {
      ctx.reportIssue(tree, message);
    }
  }

  @CheckForNull
  private static String valueOrElseDefaultIfStringLiteralOrNull(@Nullable ArmTree tree, String defaultIpAddress) {
    if (tree != null && tree.is(ArmTree.Kind.STRING_LITERAL)) {
      return TextUtils.getValue(tree).orElse(defaultIpAddress);
    }
    return null;
  }

  private static Range ipRange(String startIp, String endIp) {
    return new Range(new Ip(startIp).toLong(), new Ip(endIp).toLong());
  }

  static class Ip {

    private static final int BASE3 = (int) Math.pow(256, 3);
    private static final int BASE2 = (int) Math.pow(256, 2);
    private static final int BASE1 = 256;
    private final String address;

    Ip(String address) {
      this.address = address;
    }

    static boolean isAddressValid(String text) {
      // 0.0.0.0 - 7 chars, 255.255.255.255 - 15 chars
      if (text.length() < 7 || text.length() > 15) {
        return false;
      }
      String[] split = text.split("\\.");
      if (split.length != 4) {
        return false;
      }
      for (String s : split) {
        try {
          int x = Integer.parseInt(s);
          if (x < 0 || x > 255) {
            return false;
          }
        } catch (NumberFormatException e) {
          return false;
        }
      }
      return true;
    }

    // The method assume that address is valid
    // The IP Address can be represented as 32 bit int, but to not have negative integers, the long in necessary here.
    long toLong() {
      String[] split = address.split("\\.");
      long x1 = Long.parseLong(split[0]);
      long x2 = Long.parseLong(split[1]);
      long x3 = Long.parseLong(split[2]);
      long x4 = Long.parseLong(split[3]);

      return x1 * BASE3 + x2 * BASE2 + x3 * BASE1 + x4;
    }
  }

  static class Range {
    private final long startInclusive;
    private final long endInclusive;

    public Range(long startInclusive, long endInclusive) {
      this.startInclusive = startInclusive;
      this.endInclusive = endInclusive;
    }

    public boolean contains(long x) {
      return x >= startInclusive && x <= endInclusive;
    }
  }

  static class ValidationResult {
    private final boolean valid;
    private final Ip startIp;
    private final Ip endIp;

    ValidationResult(boolean valid, @Nullable Ip startIp, @Nullable Ip endIp) {
      this.valid = valid;
      this.startIp = startIp;
      this.endIp = endIp;
    }

    public boolean isValid() {
      return valid;
    }

    public Ip startIp() {
      return startIp;
    }

    public Ip endIp() {
      return endIp;
    }
  }
}
