/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.checks;

import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChmodTest {

  @ParameterizedTest
  @MethodSource
  void shouldContainsPermissions(String expectedPermission, List<String> permissions) {
    SoftAssertions.assertSoftly(softly -> {
      for (String permission : permissions) {
        var chmod = Chmod.fromString(permission);
        softly.assertThat(chmod.hasPermission(expectedPermission))
          .overridingErrorMessage("Expected '%s' permission but not found for mode '%s'", expectedPermission, permission)
          .isTrue();
      }
    });
  }

  static Stream<Arguments> shouldContainsPermissions() {
    return Stream.of(
      Arguments.arguments("o+x", List.of(
        "1", "01", "001", "0001", "1111", "7771",
        "3", "03", "003", "0003", "1113", "7773",
        "5", "05", "005", "0005", "1115", "7775",
        "7", "07", "007", "0007", "1117", "7777",
        "o+x", "o=x", "+x", "o+r,o+x", "o+x,o+r", "u+w,o+x", "o+rx", "o+rwx", "o+xX", "a+x")),
      Arguments.arguments("o+w", List.of(
        "2", "02", "002", "0002", "1112", "7772",
        "3", "03", "003", "0003", "1113", "7773",
        "6", "06", "006", "0006", "1116", "7776",
        "7", "07", "007", "0007", "1117", "7777",
        "o+w", "o=w", "+w", "o+r,o+w", "o+w,o+r", "u+r,o+w", "o+rw", "o+rwx", "o+wX", "a+w")),
      Arguments.arguments("o+r", List.of(
        "4", "04", "004", "0004", "4444", "7774",
        "5", "05", "005", "0005", "1115", "7775",
        "6", "06", "006", "0006", "1116", "7776",
        "7", "07", "007", "0007", "1117", "7777",
        "o+r", "o=r", "+r", "o+w,o+r", "o+r,o+w", "u+x,o+r", "o+rw", "o+rwx", "o+rX", "a+r")),
      Arguments.arguments("g+x", List.of(
        "10", "010", "0010", "1111", "7717",
        "30", "030", "0030", "1131", "7737",
        "50", "050", "0050", "1151", "7757",
        "70", "070", "0070", "1171", "7777",
        "g+x", "g=x", "g+r,g+x", "g+x,g+r", "u+w,g+x", "g+rx", "g+rwx", "g+xX", "a+x")),
      Arguments.arguments("g+w", List.of(
        "20", "020", "0020", "1121", "7727",
        "30", "030", "0030", "1131", "7737",
        "60", "060", "0060", "1161", "7767",
        "70", "070", "0070", "1171", "7777",
        "g+w", "g=w", "g+r,g+w", "g+w,g+r", "u+w,g+w", "g+rw", "g+rwx", "g+wX", "a+w")),
      Arguments.arguments("g+r", List.of(
        "40", "040", "0040", "1141", "7747",
        "50", "050", "0050", "1151", "7757",
        "60", "060", "0060", "1161", "7767",
        "70", "070", "0070", "1171", "7777",
        "g+r", "g=r", "g+r,g+w", "g+w,g+r", "u+w,g+r", "g+rw", "g+rwx", "g+rX", "a+r")),
      Arguments.arguments("u+x", List.of(
        "100", "0100", "1111", "7177",
        "300", "0300", "1311", "7377",
        "500", "0500", "1511", "7577",
        "700", "0700", "1711", "7777",
        "u+x", "u=x", "u+r,u+x", "u+x,u+r", "g+w,u+x", "u+rx", "u+rwx", "u+xX", "a+x")),
      Arguments.arguments("u+w", List.of(
        "200", "0200", "1211", "7277",
        "300", "0300", "1311", "7377",
        "600", "0600", "1611", "7677",
        "700", "0700", "1711", "7777",
        "u+w", "u=w", "u+r,u+w", "u+w,u+r", "g+w,u+w", "u+rw", "u+rwx", "u+wX", "a+w")),
      Arguments.arguments("u+r", List.of(
        "400", "0400", "1411", "7477",
        "500", "0500", "1511", "7577",
        "600", "0600", "1611", "7677",
        "700", "0700", "1711", "7777",
        "u+r", "u=r", "u+r,u+w", "u+w,u+r", "g+w,u+r", "u+rw", "u+rwx", "u+rX", "a+r")),
      // setting sticky bit using `+t` is not supported yet, and it is not used in checks yet
      // "+t", "=t", "o+r,+t", "+t,o+r", "u+w,+t", "u+s,+t", "+t,g+s"
      Arguments.arguments("+t", List.of(
        "1000", "1111", "1777",
        "3000", "3111", "3777",
        "5000", "5111", "5777",
        "7000", "7111", "7777")),
      Arguments.arguments("g+s", List.of(
        "2000", "2111", "2777",
        "3000", "3111", "3777",
        "6000", "6111", "6777",
        "7000", "7111", "7777",
        "g+s", "g=s", "g+r,g+s", "g+s,g+r", "u+w,g+s", "g+rs", "g+rwxs", "g+sX")),
      Arguments.arguments("u+s", List.of(
        "4000", "4111", "4777",
        "5000", "5111", "5777",
        "6000", "6111", "6777",
        "7000", "7111", "7777",
        "u+s", "u=s", "u+r,u+s", "u+s,u+r", "o+w,u+s", "u+rs", "u+rwxs", "u+sX")));
  }

  @ParameterizedTest
  @MethodSource
  void shouldNotContainsPermissions(String expectedPermission, List<String> permissions) {
    SoftAssertions.assertSoftly(softly -> {
      for (String permission : permissions) {
        var chmod = Chmod.fromString(permission);
        softly.assertThat(chmod.hasPermission(expectedPermission))
          .overridingErrorMessage("Do NOT expected '%s' permission but not found for mode '%s'", expectedPermission, permission)
          .isFalse();
      }
    });
  }

  static Stream<Arguments> shouldNotContainsPermissions() {
    return Stream.of(
      Arguments.arguments("o+x", List.of(
        "2", "02", "002", "0002", "2222", "7772",
        "4", "04", "004", "0004", "1114", "7774",
        "6", "06", "006", "0006", "1116", "7776",
        "o-x", "o=r", "o=w", "+r", "o+r,o+w", "o+w,o+r", "u+w,o+r", "o+rw", "o+rX", "", "a+w")),
      Arguments.arguments("o+w", List.of(
        "1", "01", "001", "0001", "1111", "7771",
        "4", "04", "004", "0004", "1114", "7774",
        "5", "05", "005", "0005", "1115", "7775",
        "o-w", "o=r", "o=x", "+r", "o+r,o+x", "o+x,o+r", "u+x,o+r", "o+rx", "o+rX", "", "a+r")),
      Arguments.arguments("o+r", List.of(
        "1", "01", "001", "0001", "1111", "7771",
        "2", "02", "002", "0002", "1112", "7772",
        "3", "03", "003", "0003", "1113", "7773",
        "o-r", "o=x", "o=w", "+x", "o+w,o+x", "o+x,o+w", "u+r,o+w", "o+wx", "o+wX", "", "a+x")),
      Arguments.arguments("g+x", List.of(
        "20", "020", "0020", "1121", "7727",
        "40", "040", "0040", "1141", "7747",
        "60", "060", "0060", "1161", "7767",
        "g-x", "g=r", "g=w", "g+r,g+w", "g+w,g+r", "u+w,g+r", "g+rw", "g+rX", "", "a+r")),
      Arguments.arguments("g+w", List.of(
        "10", "010", "0010", "1111", "7717",
        "40", "040", "0040", "1141", "7747",
        "50", "050", "0050", "1151", "7757",
        "g-w", "g=r", "g=x", "g+r,g+x", "g+x,g+r", "u+w,g+r", "g+rx", "g+rX", "", "a+r")),
      Arguments.arguments("g+r", List.of(
        "10", "010", "0010", "1111", "7717",
        "20", "020", "0020", "1121", "7727",
        "30", "030", "0030", "1131", "7737",
        "g-r", "g=w", "g=x", "g+w,g+x", "g+x,g+w", "u+r,g+w", "g+wx", "g+wX", "", "a+w")),
      Arguments.arguments("u+x", List.of(
        "200", "0200", "1211", "7277",
        "400", "0400", "1411", "7477",
        "600", "0600", "1611", "7677",
        "u-x", "u=r", "u=w", "u+r,u+w", "u+w,u+r", "g+x,u+r", "u+rw", "u+rX", "", "a+r")),
      Arguments.arguments("u+w", List.of(
        "100", "0100", "1111", "7177",
        "400", "0400", "1411", "7477",
        "500", "0500", "1511", "7577",
        "u-w", "u=r", "u=x", "u+r,u+x", "u+x,u+r", "g+w,u+r", "u+rx", "u+xX", "", "a+r")),
      Arguments.arguments("u+r", List.of(
        "100", "0100", "1111", "7177",
        "200", "0200", "1211", "7277",
        "300", "0300", "1311", "7377",
        "u-r", "u=w", "u=x", "u+w,u+x", "u+x,u+w", "g+w,u+w", "u+wx", "u+xX", "", "a+w")),
      Arguments.arguments("+t", List.of(
        // setting sticky bit using `+t` is not supported yet, and it is not used in checks yet
        "+t", "=t", "o+r,+t", "+t,o+r", "u+w,+t", "u+s,+t", "+t,g+s",
        "2000", "2111", "2777",
        "4000", "4111", "4777",
        "6000", "6111", "6777", "")),
      Arguments.arguments("g+s", List.of(
        "1000", "1111", "1777",
        "4000", "4111", "4777",
        "5000", "5111", "5777",
        "g-s", "g=w", "g=x", "g=r", "g+r,g+w", "g+w,g+r", "u+s,g+w", "g+rw", "g+rwx", "g+rX", "", "a+rwx")),
      Arguments.arguments("u+s", List.of(
        "1000", "1111", "1777",
        "2000", "2111", "2777",
        "3000", "3111", "3777",
        "u-s", "u=w", "u=r", "u=x", "u+r,u+w", "u+w,u+r", "u+w,g+s", "u+rw", "u+rwx", "u+rX", "", "a+rwx")));
  }
}
