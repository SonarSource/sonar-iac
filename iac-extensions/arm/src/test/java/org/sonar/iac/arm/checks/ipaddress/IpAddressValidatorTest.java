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
package org.sonar.iac.arm.checks.ipaddress;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.impl.json.ObjectExpressionImpl;
import org.sonar.iac.arm.tree.impl.json.StringLiteralImpl;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.yaml.tree.YamlTreeMetadata;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class IpAddressValidatorTest {

  private static final String MESSAGE = "message";
  private static final String SECONDARY_MESSAGE = "secondary message";
  private YamlTreeMetadata startMetadata = new YamlTreeMetadata("start", range(1, 2, 1, 7), List.of());
  private YamlTreeMetadata endMetadata = new YamlTreeMetadata("end", range(2, 2, 2, 5), List.of());

  static Stream<Arguments> shouldReportIssue() {
    return Stream.of(
      Arguments.of("0.0.0.0", "255.255.255.255"),
      Arguments.of("0.0.0.", "255.255.255.255"),
      Arguments.of("0123.12345", "255.255.255.255"),
      Arguments.of("0.1.2.300", "255.255.255.255"),
      Arguments.of("0.1.2.abc", "255.255.255.255"),
      Arguments.of("0.1.2.-5", "255.255.255.255"),
      Arguments.of("0.0.0.1", "255.255.255.255.1"),
      Arguments.of("10.0.0.0", "100.64.0.0"),
      Arguments.of("10.0.0.0", "169.254.255.255"),
      Arguments.of("11.0.0.0", "169.254.255.255"));
  }

  @MethodSource
  @ParameterizedTest(name = "[{index}]should report issue for ip from {0} to {1}")
  void shouldReportIssue(String start, String end) {
    ArmTree startIpAddress = new StringLiteralImpl(start, startMetadata);
    ArmTree endIpAddress = new StringLiteralImpl(end, endMetadata);
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verify(ctx).reportIssue(
      same(startIpAddress),
      same(MESSAGE),
      eq(List.of(new SecondaryLocation(range(2, 2, 2, 5), SECONDARY_MESSAGE))));
  }

  @Test
  void shouldReportIssueForNullStart() {
    ArmTree endIpAddress = new StringLiteralImpl("255.255.255.255", endMetadata);
    IpAddressValidator validator = new IpAddressValidator(null, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verify(ctx).reportIssue(
      same(endIpAddress),
      same(MESSAGE));
  }

  @Test
  void shouldReportIssueForNullEnd() {
    ArmTree startIpAddress = new StringLiteralImpl("0.0.0.0", startMetadata);
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, null);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verify(ctx).reportIssue(
      same(startIpAddress),
      same(MESSAGE));
  }

  @Test
  void shouldReportIssueWhenStartIsNotStringLiteral() {
    ArmTree startIpAddress = new ObjectExpressionImpl(List.of(), range(1, 1, 1, 1));
    ArmTree endIpAddress = new StringLiteralImpl("255.255.255.255", endMetadata);
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verify(ctx).reportIssue(
      same(startIpAddress),
      same(MESSAGE),
      eq(List.of(new SecondaryLocation(range(2, 2, 2, 5), SECONDARY_MESSAGE))));
  }

  @Test
  void shouldReportIssueWhenEndIsNotStringLiteral() {
    ArmTree startIpAddress = new StringLiteralImpl("0.0.0.0", startMetadata);
    ArmTree endIpAddress = new ObjectExpressionImpl(List.of(), range(1, 1, 1, 1));
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verify(ctx).reportIssue(
      same(startIpAddress),
      same(MESSAGE),
      eq(List.of(new SecondaryLocation(range(1, 1, 1, 1), SECONDARY_MESSAGE))));
  }

  @Test
  void shouldNotReportIssueForPrivateNetworks() {
    ArmTree startIpAddress = new StringLiteralImpl("10.0.0.1", startMetadata);
    ArmTree endIpAddress = new StringLiteralImpl("10.255.255.255", endMetadata);
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verifyNoInteractions(ctx);
  }

  @Test
  void shouldNotReportIssueForNulls() {
    IpAddressValidator validator = new IpAddressValidator(null, null);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verifyNoInteractions(ctx);
  }

  @Test
  void shouldNotReportIssueWhenStartAndEndIsNotStringLiteral() {
    ArmTree startIpAddress = new ObjectExpressionImpl(List.of(), range(1, 1, 1, 1));
    ArmTree endIpAddress = new ObjectExpressionImpl(List.of(), range(2, 2, 2, 2));
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verifyNoInteractions(ctx);
  }

  @Test
  void shouldNotReportIssueForStartAndEndIsUnknown() {
    ArmTree startIpAddress = new StringLiteralImpl("unknown", startMetadata);
    ArmTree endIpAddress = new StringLiteralImpl("unknown", endMetadata);
    IpAddressValidator validator = new IpAddressValidator(startIpAddress, endIpAddress);
    CheckContext ctx = mock(CheckContext.class);

    validator.reportIssueIfPublicIPAddress(ctx, MESSAGE, SECONDARY_MESSAGE);

    verifyNoInteractions(ctx);
  }
}
