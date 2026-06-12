/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks.network;

import java.util.OptionalLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.checks.network.IpAddressClassifier.Classification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IpAddressClassifierTest {

  @ParameterizedTest
  @ValueSource(strings = {
    // "This network" (0.0.0.0/8)
    "0.0.0.0", // singleton sentinel
    "0.99.42.7", // middle of the /8
    // RFC 1918 Private (10/8, 172.16/12, 192.168/16) — middle of each block, then boundaries
    "10.99.42.7",
    "10.0.0.1",
    "10.255.255.255",
    "172.24.50.25",
    "172.16.0.0",
    "172.31.255.255",
    "192.168.99.42",
    "192.168.0.0",
    "192.168.255.255",
    // CGNAT (100.64.0.0/10)
    "100.96.50.25", // middle (100.64 to 100.127)
    "100.64.0.0",
    "100.100.100.200", // Alibaba ECS IMDS
    // Loopback (127.0.0.0/8)
    "127.99.42.7", // middle
    // Link-local (169.254.0.0/16)
    "169.254.169.254", // middle — also Azure IMDS
    // IETF Protocol Assignments (192.0.0.0/24)
    "192.0.0.128", // middle of /24
    // Documentation: TEST-NET-1/2/3 — middle of each /24
    "192.0.2.128",
    "198.51.100.128",
    "203.0.113.128",
    // Benchmarking (198.18.0.0/15) — middle (second /16 of the /15)
    "198.19.50.25",
    // Reserved for future use / Class E (240.0.0.0/4) — middle and the limited-broadcast singleton
    "247.99.42.7",
    "255.255.255.255"
  })
  void shouldClassifyReservedIpv4SingleAsReserved(String literal) {
    assertThat(IpAddressClassifier.classify(literal)).isEqualTo(Classification.RESERVED);
    assertThat(IpAddressClassifier.isReserved(literal)).isTrue();
    assertThat(IpAddressClassifier.isInternetRoutable(literal)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // Ordinary public
    "1.2.3.4",
    "8.8.8.8",
    "199.0.0.0",
    // Just outside an RFC 1918 boundary
    "172.32.0.0",
    "192.1.0.0",
    // IANA single-use specials marked globally reachable (not in our safe list)
    "192.31.196.0", // AS112-v4 (RFC 7535)
    "192.52.193.0", // AMT (RFC 7450)
    "192.175.48.0" // AS112 Direct Delegation (RFC 7534)
  })
  void shouldClassifyPublicIpv4SingleAsPublic(String literal) {
    assertThat(IpAddressClassifier.classify(literal)).isEqualTo(Classification.PUBLIC);
    assertThat(IpAddressClassifier.isReserved(literal)).isFalse();
    assertThat(IpAddressClassifier.isInternetRoutable(literal)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // "This network" (0.0.0.0/8)
    "0.0.0.0/8",
    // RFC 1918 Private (10/8, 172.16/12, 192.168/16)
    "10.0.0.0/8",
    "10.5.0.0/16",
    "172.16.0.0/12",
    "172.20.0.0/16",
    "192.168.0.0/16",
    "192.168.27.0/24",
    // Loopback (127.0.0.0/8)
    "127.0.0.0/8",
    // Reserved for future use / Class E (240.0.0.0/4)
    "240.0.0.0/4"
  })
  void shouldClassifyReservedIpv4CidrAsReserved(String literal) {
    assertThat(IpAddressClassifier.classify(literal)).isEqualTo(Classification.RESERVED);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // Default route, straddles everything
    "0.0.0.0/0",
    // Straddles RFC 1918 10/8 boundary
    "10.0.0.0/1",
    "10.0.0.0/7",
    // Straddles RFC 1918 172.16/12 boundary
    "172.0.0.0/8",
    // Straddles 192.0.0.0/24 boundary
    "192.0.0.0/16"
  })
  void shouldClassifyStraddlingCidrAsPublic(String literal) {
    assertThat(IpAddressClassifier.classify(literal)).isEqualTo(Classification.PUBLIC);
  }

  @ParameterizedTest
  @CsvSource({
    // Loopback (::1/128) — the block is a singleton
    "::1,                     RESERVED",
    "::1/128,                 RESERVED",
    // Unique Local Addresses (fc00::/7) — middle of each half, plus block
    "fc99:1234::42,           RESERVED",
    "fd12:3456::1,            RESERVED",
    "fd00:ec2::254,           RESERVED", // AWS IMDSv6
    "fd20:ce::254,            RESERVED", // GCP IMDSv6
    "fc00::/7,                RESERVED",
    // Link-local (fe80::/10) — middle and boundary
    "fea0::42,                RESERVED",
    "fe80::1,                 RESERVED",
    // Documentation 2001:db8::/32 — middle and block
    "2001:db8:1234:5678::42,  RESERVED",
    "2001:db8::1,             RESERVED",
    "2001:db8::/32,           RESERVED",
    // Documentation 3fff::/20 — middle, upper boundary, and block
    "3fff:0800::42,           RESERVED",
    "3fff::1,                 RESERVED",
    "3fff:0fff::1,            RESERVED",
    "3fff::/20,               RESERVED",
    // Default route, straddles everything
    "::/0,                    PUBLIC",
    // Ordinary public
    "2001::1,                 PUBLIC",
    "2606:4700::1,            PUBLIC",
    "4000::1,                 PUBLIC"
  })
  void shouldClassifyIpv6Correctly(String literal, Classification expected) {
    assertThat(IpAddressClassifier.classify(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // Not IP-shaped
    "",
    "unknown",
    "not.an.ip",
    // Bad IPv4 octet count
    "1.2.3",
    "1.2.3.4.5",
    // Bad IPv4 octet value
    "256.0.0.0",
    // Bad IPv4 CIDR mask
    "10.0.0.1/33",
    "10.0.0.1/-1",
    "10.0.0.1/abc",
    // Bad IPv6 CIDR mask
    "2001:db8::/129",
    // Bad IPv6 hex
    "gggg::1"
  })
  void shouldClassifyMalformedAsUnparseable(String literal) {
    assertThat(IpAddressClassifier.classify(literal)).isEqualTo(Classification.UNPARSEABLE);
    assertThat(IpAddressClassifier.isReserved(literal)).isFalse();
    assertThat(IpAddressClassifier.isInternetRoutable(literal)).isFalse();
  }

  @Test
  void parseIpv4SingleAddressShouldReturnLongForValidAddress() {
    assertThat(IpAddressClassifier.parseIpv4SingleAddress("0.0.0.0")).hasValue(0L);
    assertThat(IpAddressClassifier.parseIpv4SingleAddress("10.0.0.1")).hasValue(0x0A000001L);
    assertThat(IpAddressClassifier.parseIpv4SingleAddress("255.255.255.255")).hasValue(0xFFFFFFFFL);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    // CIDR (not a single address)
    "10.0.0.0/8",
    // Bad IPv4
    "10.0.0",
    "256.0.0.0",
    // Not IP-shaped
    "not.an.ip",
    "",
    // Wrong family
    "::1"
  })
  void parseIpv4SingleAddressShouldReturnEmptyForInvalidInput(String literal) {
    assertThat(IpAddressClassifier.parseIpv4SingleAddress(literal)).isEqualTo(OptionalLong.empty());
  }

  @Test
  void isAddressRangeReservedShouldRecognizeRfc1918Ranges() {
    long start = IpAddressClassifier.parseIpv4SingleAddress("10.0.0.0").orElseThrow();
    long end = IpAddressClassifier.parseIpv4SingleAddress("10.255.255.255").orElseThrow();
    assertThat(IpAddressClassifier.isAddressRangeReserved(start, end)).isTrue();
  }

  @Test
  void isAddressRangeReservedShouldRejectStraddlingRange() {
    long start = IpAddressClassifier.parseIpv4SingleAddress("10.0.0.0").orElseThrow();
    long end = IpAddressClassifier.parseIpv4SingleAddress("11.0.0.0").orElseThrow();
    assertThat(IpAddressClassifier.isAddressRangeReserved(start, end)).isFalse();
  }

  @Test
  void isAddressRangeReservedShouldCoverFullClassEReservedBlock() {
    long start = IpAddressClassifier.parseIpv4SingleAddress("240.0.0.0").orElseThrow();
    long end = IpAddressClassifier.parseIpv4SingleAddress("255.255.255.255").orElseThrow();
    assertThat(IpAddressClassifier.isAddressRangeReserved(start, end)).isTrue();
  }

  @Test
  void isAddressRangeReservedShouldThrowOnInvertedRange() {
    long start = IpAddressClassifier.parseIpv4SingleAddress("10.0.0.100").orElseThrow();
    long end = IpAddressClassifier.parseIpv4SingleAddress("10.0.0.0").orElseThrow();
    assertThatThrownBy(() -> IpAddressClassifier.isAddressRangeReserved(start, end))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Inverted IPv4 range");
  }

  @ParameterizedTest
  @CsvSource({
    // IPv4 loopback (127.0.0.0/8) — middle, edges, and CIDR forms
    "127.99.42.7,      true",
    "127.0.0.1,        true",
    "127.255.255.255,  true",
    "127.0.0.0/8,      true",
    "127.0.0.0/16,     true",
    // IPv6 loopback (::1/128) — the block is a singleton
    "::1,              true",
    "::1/128,          true",
    // Abbreviated inet_aton forms — not supported (classifier requires 4 octets)
    "127.1,            false", // FN: expands to 127.0.0.1
    "127.0.1,          false", // FN: expands to 127.0.0.1
    // Not loopback
    "10.0.0.1,         false",
    "8.8.8.8,          false",
    "fe80::1,          false",
    "192.0.2.10,       false",
    "bogus,            false"
  })
  void shouldDetectLoopback(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isLoopback(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    // IPv4 link-local (169.254.0.0/16) — middle, edges, CIDR
    "169.254.99.42,    true",
    "169.254.169.254,  true",
    "169.254.0.0,      true",
    "169.254.255.255,  true",
    "169.254.0.0/16,   true",
    "169.254.10.0/24,  true",
    // IPv6 link-local (fe80::/10) — middle, edges
    "fea0::42,         true",
    "fe80::1,          true",
    "febf::1,          true",
    "fe80::/10,        true",
    // Not link-local
    "10.0.0.1,         false",
    "127.0.0.1,        false",
    "169.255.0.0,      false",
    "fec0::1,          false",
    "bogus,            false"
  })
  void shouldDetectLinkLocal(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isLinkLocal(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    // RFC 1918 IPv4 — middle of each block
    "10.99.42.7,       true",
    "172.24.50.25,     true",
    "192.168.99.42,    true",
    // RFC 1918 IPv4 — edges (sanity)
    "10.0.0.1,         true",
    "10.255.255.255,   true",
    "172.16.0.0,       true",
    "172.31.255.255,   true",
    // RFC 1918 CIDR forms
    "10.0.0.0/8,       true",
    "172.16.0.0/12,    true",
    "192.168.0.0/16,   true",
    // IPv6 Unique Local (fc00::/7) — middle and edges
    "fd12:3456::1,     true",
    "fc99:1234::42,    true",
    "fc00::1,          true",
    "fc00::/7,         true",
    // Not private — adjacent or other reserved
    "127.0.0.1,        false",
    "169.254.169.254,  false",
    "192.0.2.10,       false",
    "100.64.0.0,       false",
    "8.8.8.8,          false",
    "::1,              false",
    "fe80::1,          false",
    "bogus,            false"
  })
  void shouldDetectPrivate(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isPrivate(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    // TEST-NET-1/2/3 — middle of each /24
    "192.0.2.128,             true",
    "198.51.100.128,          true",
    "203.0.113.128,           true",
    "192.0.2.0/24,            true",
    // IPv6 2001:db8::/32 — middle and the block itself
    "2001:db8:1234:5678::42,  true",
    "2001:db8::1,             true",
    "2001:db8::/32,           true",
    // IPv6 3fff::/20 — middle, upper-boundary, CIDR
    "3fff:0800::42,           true",
    "3fff::1,                 true",
    "3fff:0fff::1,            true",
    "3fff::/20,               true",
    // Not documentation
    "10.0.0.1,                false",
    "127.0.0.1,                false",
    "192.0.0.0,                false",
    "192.0.3.0,                false",
    "4000::1,                  false",
    "bogus,                    false"
  })
  void shouldDetectDocumentation(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isDocumentation(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    // Singletons
    "0.0.0.0,          true",
    "0.0.0.0/32,       true",
    "::,               true",
    "::/128,           true",
    // Not any-address — these belong to other categories
    "0.0.0.0/0,        false",
    "::/0,             false",
    "0.0.0.1,          false",
    "0.0.0.0/8,        false",
    "0.0.0.0/31,       false",
    "::1,              false",
    "10.0.0.1,         false",
    "bogus,            false"
  })
  void shouldDetectAnyAddress(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isAnyAddress(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "255.255.255.255,     true",
    "255.255.255.255/32,  true",
    // Not broadcast
    "255.255.255.254,     false",
    "255.255.255.255/31,  false",
    "240.0.0.0/4,         false",
    "0.0.0.0,             false",
    "::,                  false",
    "::ffff:ffff:ffff,    false",
    "bogus,               false"
  })
  void shouldDetectBroadcast(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isBroadcast(literal)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    // /0-mask CIDR (any starting address normalizes to the same range)
    "0.0.0.0/0,        true",
    "::/0,             true",
    "0::0/0,           true",
    "10.0.0.0/0,       true",
    // Not unrestricted
    "0.0.0.0,          false",
    "::,               false",
    "0.0.0.0/32,       false",
    "::/128,           false",
    "0.0.0.0/8,        false",
    "10.0.0.0/8,       false",
    "bogus,            false"
  })
  void shouldDetectUnrestrictedCidr(String literal, boolean expected) {
    assertThat(IpAddressClassifier.isUnrestrictedCidr(literal)).isEqualTo(expected);
  }
}
