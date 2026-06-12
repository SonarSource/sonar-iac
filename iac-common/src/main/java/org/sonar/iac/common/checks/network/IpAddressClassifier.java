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

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.OptionalLong;
import java.util.regex.Pattern;

/**
 * Classifies an IP-address literal (plain or CIDR, IPv4 or IPv6) as reserved for private/special-purpose use, internet-routable, or unparseable.
 *
 * <p>CIDR (Classless Inter-Domain Routing) semantics: a literal is reserved only if its <em>entire</em> range fits inside one reserved block.
 * For example, {@code 10.0.0.0/1} is NOT reserved.
 *
 * <p>Reserved ranges follow the IANA IPv4/IPv6 Special-Purpose Address registries' "not globally reachable" entries.
 *
 * <h2>Usage examples</h2>
 *
 * <p>S6329 ("public network access") — flag any firewall-rule literal not provably reserved (unparseable literals stay flagged on the safe side):
 * <pre>{@code
 * if (!IpAddressClassifier.isReserved(literal)) {
 *   ctx.reportIssue(node, "Public IP allowed in firewall rule.");
 * }
 * }</pre>
 *
 * <p>S6321 ("admin services exposed to the Internet") — flag CIDR rules that cover the entire address space:
 * <pre>{@code
 * if (IpAddressClassifier.isUnrestrictedCidr(literal)) {
 *   ctx.reportIssue(node, "Restrict source addresses.");
 * }
 * }</pre>
 *
 * <p>S1313 ("hardcoded IP") — exempt only documentation, loopback, any-address, and broadcast sentinels; still flag private RFC 1918 leakage:
 * <pre>{@code
 * boolean exempt = IpAddressClassifier.isLoopback(literal)
 *               || IpAddressClassifier.isAnyAddress(literal)
 *               || IpAddressClassifier.isBroadcast(literal)
 *               || IpAddressClassifier.isDocumentation(literal);
 * if (!exempt) {
 *   ctx.reportIssue(node, "Avoid hardcoded IP addresses.");
 * }
 * }</pre>
 */
public final class IpAddressClassifier {

  /** Result of classifying an IP literal. Package-private — boolean predicates expose what callers need. */
  enum Classification {
    /** Literal parses; its entire range fits inside a reserved special-purpose block. */
    RESERVED,
    /** Literal parses; its range falls outside every reserved block. */
    PUBLIC,
    /** Literal does not parse as an IP address or CIDR. */
    UNPARSEABLE
  }

  private static final String RFC1918_LABEL = "RFC 1918 Private";

  // IPv4 special-purpose blocks per IANA, restricted to entries that are NOT
  // globally reachable.
  @SuppressWarnings("java:S1313")
  private static final List<Block4> RESERVED_IPV4_BLOCKS = List.of(
    block4("0.0.0.0/8", "This network", BlockKind.THIS_NETWORK),
    block4("10.0.0.0/8", RFC1918_LABEL, BlockKind.RFC1918_PRIVATE),
    block4("172.16.0.0/12", RFC1918_LABEL, BlockKind.RFC1918_PRIVATE),
    block4("192.168.0.0/16", RFC1918_LABEL, BlockKind.RFC1918_PRIVATE),
    block4("100.64.0.0/10", "CGNAT", BlockKind.CGNAT),
    block4("127.0.0.0/8", "Loopback", BlockKind.LOOPBACK),
    block4("169.254.0.0/16", "Link-local", BlockKind.LINK_LOCAL),
    block4("192.0.0.0/24", "IETF Protocol Assignments", BlockKind.IETF_PROTOCOL),
    block4("192.0.2.0/24", "Documentation TEST-NET-1", BlockKind.DOCUMENTATION),
    block4("198.51.100.0/24", "Documentation TEST-NET-2", BlockKind.DOCUMENTATION),
    block4("203.0.113.0/24", "Documentation TEST-NET-3", BlockKind.DOCUMENTATION),
    block4("198.18.0.0/15", "Benchmarking", BlockKind.BENCHMARKING),
    block4("240.0.0.0/4", "Reserved for future use / Class E", BlockKind.RESERVED_FUTURE));

  private static final List<Block6> RESERVED_IPV6_BLOCKS = List.of(
    block6("::1/128", "Loopback", BlockKind.LOOPBACK),
    block6("2001:db8::/32", "Documentation", BlockKind.DOCUMENTATION),
    block6("3fff::/20", "Documentation", BlockKind.DOCUMENTATION),
    block6("fc00::/7", "Unique Local", BlockKind.UNIQUE_LOCAL),
    block6("fe80::/10", "Link-local", BlockKind.LINK_LOCAL));

  // Lexical pre-filter for IPv6; prevents {@link InetAddress#getByName} from
  // attempting a DNS lookup on strings outside the IPv6 alphabet. Dots are
  // intentionally excluded: IPv4-mapped IPv6 literals (e.g. ::ffff:1.2.3.4)
  // are not supported.
  private static final Pattern IPV6_LIKE = Pattern.compile("[\\da-fA-F:]+(/\\d+)?");

  private IpAddressClassifier() {
  }

  /**
   * Classifies the literal into {@link Classification#RESERVED}, {@link Classification#PUBLIC}, or {@link Classification#UNPARSEABLE}.
   * The literal must be non-null; pre-filter {@code Optional} results with {@code orElse} first.
   * Package-private — boolean predicates ({@link #isReserved}, {@link #isInternetRoutable}, the per-kind variants) are the public surface.
   */
  static Classification classify(String literal) {
    if (looksLikeIpv4(literal)) {
      Range4 parsed = parseIpv4Cidr(literal);
      if (parsed == null) {
        return Classification.UNPARSEABLE;
      }
      return RESERVED_IPV4_BLOCKS.stream().anyMatch(b -> b.range().contains(parsed))
        ? Classification.RESERVED
        : Classification.PUBLIC;
    }
    if (literal.indexOf(':') >= 0 && IPV6_LIKE.matcher(literal).matches()) {
      Range6 parsed = parseIpv6Cidr(literal);
      if (parsed == null) {
        return Classification.UNPARSEABLE;
      }
      return RESERVED_IPV6_BLOCKS.stream().anyMatch(b -> b.range().contains(parsed))
        ? Classification.RESERVED
        : Classification.PUBLIC;
    }
    return Classification.UNPARSEABLE;
  }

  /**
   * True if the literal parses and its entire range fits inside a reserved special-purpose block; false otherwise (including unparseable).
   * Examples: {@code "10.0.0.1"} → true; {@code "10.0.0.0/1"} → false (straddles a reserved boundary).
   */
  public static boolean isReserved(String literal) {
    return classify(literal) == Classification.RESERVED;
  }

  /**
   * True if the literal parses and its entire range falls outside every reserved block; false otherwise (including unparseable).
   * Examples: {@code "8.8.8.8"} → true; {@code "10.0.0.1"} → false; {@code "bogus"} → false.
   * Package-private — exposed only for callers that need PUBLIC distinct from UNPARSEABLE (which {@code !isReserved} cannot tell apart).
   */
  static boolean isInternetRoutable(String literal) {
    return classify(literal) == Classification.PUBLIC;
  }

  /**
   * True if the literal fits entirely within IPv4 {@code 127.0.0.0/8} or IPv6 {@code ::1/128}.
   * Examples: {@code "127.0.0.1"} → true; {@code "::1"} → true; {@code "10.0.0.1"} → false.
   */
  public static boolean isLoopback(String literal) {
    return matchesKind(literal, BlockKind.LOOPBACK);
  }

  /**
   * True if the literal fits entirely within IPv4 {@code 169.254.0.0/16} (RFC 3927) or IPv6 {@code fe80::/10} (RFC 4291).
   * Examples: {@code "169.254.169.254"} → true; {@code "fe80::1"} → true; {@code "10.0.0.1"} → false.
   */
  public static boolean isLinkLocal(String literal) {
    return matchesKind(literal, BlockKind.LINK_LOCAL);
  }

  /**
   * True if the literal fits entirely within one of:
   * <ul>
   *   <li>IPv4 RFC 1918 — {@code 10.0.0.0/8}, {@code 172.16.0.0/12}, {@code 192.168.0.0/16}</li>
   *   <li>IPv6 Unique Local Addresses — {@code fc00::/7} (RFC 4193)</li>
   * </ul>
   * Does not cover CGNAT, link-local, loopback, or documentation; query those via their own predicates.
   */
  public static boolean isPrivate(String literal) {
    return matchesKind(literal, BlockKind.RFC1918_PRIVATE)
      || matchesKind(literal, BlockKind.UNIQUE_LOCAL);
  }

  /**
   * True if the literal fits entirely within one of:
   * <ul>
   *   <li>IPv4 TEST-NET-1/2/3 — {@code 192.0.2.0/24}, {@code 198.51.100.0/24}, {@code 203.0.113.0/24} (RFC 5737)</li>
   *   <li>IPv6 {@code 2001:db8::/32} (RFC 3849)</li>
   *   <li>IPv6 {@code 3fff::/20} (RFC 9637)</li>
   * </ul>
   * Examples: {@code "192.0.2.10"} → true; {@code "3fff::1"} → true; {@code "10.0.0.1"} → false.
   */
  public static boolean isDocumentation(String literal) {
    return matchesKind(literal, BlockKind.DOCUMENTATION);
  }

  /**
   * True if the literal is the "any address" singleton: IPv4 {@code 0.0.0.0} (or {@code /32}) or IPv6 {@code ::} (or {@code /128}).
   * This is the sentinel host-binding APIs use to mean "listen on every interface".
   * Returns false for the {@code /0} CIDR {@code 0.0.0.0/0} — use {@link #isUnrestrictedCidr(String)} for that.
   */
  public static boolean isAnyAddress(String literal) {
    if (looksLikeIpv4(literal)) {
      Range4 parsed = parseIpv4Cidr(literal);
      return parsed != null && parsed.startInclusive() == 0L && parsed.endInclusive() == 0L;
    }
    if (literal.indexOf(':') >= 0 && IPV6_LIKE.matcher(literal).matches()) {
      Range6 parsed = parseIpv6Cidr(literal);
      return parsed != null
        && BigInteger.ZERO.equals(parsed.startInclusive())
        && BigInteger.ZERO.equals(parsed.endInclusive());
    }
    return false;
  }

  /**
   * True if the literal is the IPv4 limited-broadcast address {@code 255.255.255.255} (or {@code /32}).
   * IPv6 has no broadcast equivalent, so every IPv6 literal returns false.
   */
  public static boolean isBroadcast(String literal) {
    if (!looksLikeIpv4(literal)) {
      return false;
    }
    Range4 parsed = parseIpv4Cidr(literal);
    return parsed != null
      && parsed.startInclusive() == 0xFFFFFFFFL
      && parsed.endInclusive() == 0xFFFFFFFFL;
  }

  /**
   * True if the literal is a {@code /0}-mask CIDR covering the entire IPv4 or IPv6 address space (e.g. {@code 0.0.0.0/0}, {@code ::/0}).
   * Any non-zero address combined with {@code /0} also matches ({@code 10.0.0.0/0} normalizes to the same range).
   * Plain singletons like {@code 0.0.0.0} return false — use {@link #isAnyAddress(String)} for the bind-anywhere sentinel.
   */
  public static boolean isUnrestrictedCidr(String literal) {
    if (looksLikeIpv4(literal)) {
      Range4 parsed = parseIpv4Cidr(literal);
      return parsed != null
        && parsed.startInclusive() == 0L
        && parsed.endInclusive() == 0xFFFFFFFFL;
    }
    if (literal.indexOf(':') >= 0 && IPV6_LIKE.matcher(literal).matches()) {
      Range6 parsed = parseIpv6Cidr(literal);
      if (parsed == null) {
        return false;
      }
      BigInteger maxIpv6 = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
      return BigInteger.ZERO.equals(parsed.startInclusive())
        && maxIpv6.equals(parsed.endInclusive());
    }
    return false;
  }

  /**
   * Parses a plain IPv4 address (no CIDR suffix) into a 32-bit value held in a long.
   * Returns empty for CIDR literals, malformed input, IPv6 literals, or null.
   * Example: {@code "10.0.0.1"} → {@code OptionalLong.of(0x0A000001L)}; {@code "10.0.0.0/8"} → empty.
   */
  public static OptionalLong parseIpv4SingleAddress(String literal) {
    if (literal.indexOf('/') >= 0 || !looksLikeIpv4(literal)) {
      return OptionalLong.empty();
    }
    long addr = parseIpv4Address(literal);
    return addr < 0 ? OptionalLong.empty() : OptionalLong.of(addr);
  }

  /**
   * True if the inclusive IPv4 range {@code [start, end]} is fully contained in one of the reserved blocks.
   * Inputs are 32-bit IPv4 addresses held in a long (see {@link #parseIpv4SingleAddress}).
   * Throws {@link IllegalArgumentException} when {@code start > end}; callers must pre-check the ordering or catch.
   */
  public static boolean isAddressRangeReserved(long startInclusive, long endInclusive) {
    if (startInclusive > endInclusive) {
      throw new IllegalArgumentException("Inverted IPv4 range: start=" + startInclusive + " > end=" + endInclusive);
    }
    Range4 range = new Range4(startInclusive, endInclusive);
    return RESERVED_IPV4_BLOCKS.stream().anyMatch(b -> b.range().contains(range));
  }

  private static boolean matchesKind(String literal, BlockKind kind) {
    if (looksLikeIpv4(literal)) {
      Range4 parsed = parseIpv4Cidr(literal);
      return parsed != null && RESERVED_IPV4_BLOCKS.stream()
        .filter(b -> b.kind() == kind)
        .anyMatch(b -> b.range().contains(parsed));
    }
    if (literal.indexOf(':') >= 0 && IPV6_LIKE.matcher(literal).matches()) {
      Range6 parsed = parseIpv6Cidr(literal);
      return parsed != null && RESERVED_IPV6_BLOCKS.stream()
        .filter(b -> b.kind() == kind)
        .anyMatch(b -> b.range().contains(parsed));
    }
    return false;
  }

  // ---------- IPv4 parsing ----------

  private static boolean looksLikeIpv4(String literal) {
    int dots = 0;
    for (int i = 0; i < literal.length(); i++) {
      if (literal.charAt(i) == '.') {
        dots++;
      }
    }
    return dots == 3;
  }

  private static Range4 parseIpv4Cidr(String literal) {
    int slash = literal.indexOf('/');
    String addressPart = slash < 0 ? literal : literal.substring(0, slash);
    int prefix = slash < 0 ? 32 : parsePrefix(literal.substring(slash + 1), 32);
    if (prefix < 0) {
      return null;
    }
    long address = parseIpv4Address(addressPart);
    if (address < 0) {
      return null;
    }
    long hostBits = 32L - prefix;
    long mask = (0xFFFFFFFFL << hostBits) & 0xFFFFFFFFL;
    long start = address & mask;
    long end = start | (0xFFFFFFFFL >>> prefix);
    return new Range4(start, end);
  }

  private static long parseIpv4Address(String dotted) {
    String[] parts = dotted.split("\\.", -1);
    if (parts.length != 4) {
      return -1L;
    }
    long result = 0L;
    for (String part : parts) {
      if (part.isEmpty() || part.length() > 3) {
        return -1L;
      }
      int octet;
      try {
        octet = Integer.parseInt(part);
      } catch (NumberFormatException e) {
        return -1L;
      }
      if (octet < 0 || octet > 255) {
        return -1L;
      }
      result = (result << 8) | octet;
    }
    return result;
  }

  // ---------- IPv6 parsing ----------

  private static Range6 parseIpv6Cidr(String literal) {
    int slash = literal.indexOf('/');
    String addressPart = slash < 0 ? literal : literal.substring(0, slash);
    int prefix = slash < 0 ? 128 : parsePrefix(literal.substring(slash + 1), 128);
    if (prefix < 0) {
      return null;
    }
    BigInteger address = parseIpv6Address(addressPart);
    if (address == null) {
      return null;
    }
    BigInteger hostBits = BigInteger.ONE.shiftLeft(128 - prefix).subtract(BigInteger.ONE);
    BigInteger start = address.andNot(hostBits);
    BigInteger end = start.or(hostBits);
    return new Range6(start, end);
  }

  private static BigInteger parseIpv6Address(String literal) {
    try {
      InetAddress addr = InetAddress.getByName(literal);
      if (!(addr instanceof Inet6Address)) {
        return null;
      }
      return new BigInteger(1, addr.getAddress());
    } catch (UnknownHostException e) {
      return null;
    }
  }

  // ---------- common ----------

  private static int parsePrefix(String text, int maxBits) {
    int prefix;
    try {
      prefix = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return -1;
    }
    return (prefix < 0 || prefix > maxBits) ? -1 : prefix;
  }

  private static Block4 block4(String cidr, String label, BlockKind kind) {
    Range4 range = parseIpv4Cidr(cidr);
    if (range == null) {
      throw new IllegalStateException("Invalid hardcoded IPv4 CIDR: " + cidr);
    }
    return new Block4(cidr, label, kind, range);
  }

  private static Block6 block6(String cidr, String label, BlockKind kind) {
    Range6 range = parseIpv6Cidr(cidr);
    if (range == null) {
      throw new IllegalStateException("Invalid hardcoded IPv6 CIDR: " + cidr);
    }
    return new Block6(cidr, label, kind, range);
  }

  private enum BlockKind {
    THIS_NETWORK,
    RFC1918_PRIVATE,
    CGNAT,
    LOOPBACK,
    LINK_LOCAL,
    IETF_PROTOCOL,
    DOCUMENTATION,
    BENCHMARKING,
    RESERVED_FUTURE,
    UNIQUE_LOCAL
  }

  private record Block4(String cidr, String label, BlockKind kind, Range4 range) {
  }

  private record Block6(String cidr, String label, BlockKind kind, Range6 range) {
  }

  private record Range4(long startInclusive, long endInclusive) {
    boolean contains(Range4 other) {
      return startInclusive <= other.startInclusive && other.endInclusive <= endInclusive;
    }
  }

  private record Range6(BigInteger startInclusive, BigInteger endInclusive) {
    boolean contains(Range6 other) {
      return startInclusive.compareTo(other.startInclusive) <= 0
        && other.endInclusive.compareTo(endInclusive) <= 0;
    }
  }
}
