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
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

public class IpAddressValidator {

  private static final List<Range> SAFE_RANGES = List.of(
    new Range(new Ip("10.0.0.0").toLong(), new Ip("10.255.255.255").toLong()),
    new Range(new Ip("100.64.0.0").toLong(), new Ip("100.127.255.255").toLong()),
    new Range(new Ip("169.254.0.0").toLong(), new Ip("169.254.255.255").toLong()),
    new Range(new Ip("172.16.0.0").toLong(), new Ip("172.31.255.255").toLong()),
    new Range(new Ip("192.0.0.0").toLong(), new Ip("192.0.0.255").toLong()),
    new Range(new Ip("192.0.2.0").toLong(), new Ip("192.0.2.255").toLong()),
    new Range(new Ip("192.168.0.0").toLong(), new Ip("192.168.255.255").toLong()),
    new Range(new Ip("198.18.0.0").toLong(), new Ip("198.19.255.255").toLong()),
    new Range(new Ip("198.51.100.0").toLong(), new Ip("198.51.100.255").toLong()),
    new Range(new Ip("203.0.113.0").toLong(), new Ip("203.0.113.255").toLong()),
    new Range(new Ip("240.0.0.0").toLong(), new Ip("240.255.255.254").toLong())
  );

  @Nullable
  private final ArmTree startIpAddress;

  @Nullable
  private final ArmTree endIpAddress;

  private IpAddressValidator(@Nullable ArmTree startIpAddress, @Nullable ArmTree endIpAddress) {
    this.startIpAddress = startIpAddress;
    this.endIpAddress = endIpAddress;

  }

  public static IpAddressValidator fromStartToEnd(@Nullable ArmTree startIpAddress, @Nullable ArmTree endIpAddress) {
    return new IpAddressValidator(startIpAddress, endIpAddress);
  }


  public void reportIssueIfPublicIPAddress(CheckContext ctx, String message, String secondaryLocationMessage) {
    if(startIpAddress == null && endIpAddress == null) {
      return;
    }
    String start = null;
    String end = null;
    if (startIpAddress != null && startIpAddress.is(ArmTree.Kind.STRING_LITERAL)) {
      start = TextUtils.getValue(startIpAddress).orElse("0.0.0.0");
    }
    if(endIpAddress != null && endIpAddress.is(ArmTree.Kind.STRING_LITERAL)) {
      end = TextUtils.getValue(endIpAddress).orElse("255.255.255.255");
    }
    if(start == null && end == null) {
      return;
    }
    if(start == null) {
      start = "0.0.0.0";
    }
    if (end == null) {
      end = "255.255.255.255";
    }
    if (Ip.isAddressValid(start) && Ip.isAddressValid(end)) {
      long startInt = new Ip(start).toLong();
      long endInt = new Ip(end).toLong();

      for (Range safeRange : SAFE_RANGES) {
        if (safeRange.contains(startInt) && safeRange.contains(endInt)) {
          return;
        }
      }

      Tree tree = (startIpAddress != null) ? startIpAddress : endIpAddress;
      Tree secondary = (startIpAddress != null && endIpAddress != null) ? endIpAddress : null;

      if(secondary != null) {
        SecondaryLocation secondaryLocation = new SecondaryLocation(secondary, secondaryLocationMessage);
        ctx.reportIssue(tree, message, List.of(secondaryLocation));
      } else {
        ctx.reportIssue(tree, message);
      }
    }
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
}
