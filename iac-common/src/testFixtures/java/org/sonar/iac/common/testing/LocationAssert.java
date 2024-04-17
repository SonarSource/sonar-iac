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
package org.sonar.iac.common.testing;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.sonar.iac.common.api.tree.Location;

/**
 * Common usage:
 * <pre>
 *   {@code
 *       assertThat(new LocationImpl(0,5))
 *       .on("abcdefgh")
 *       .isEqualTo("abcde")
 *       .hasLocation(0, 5);
 *   }
 * </pre>
 */
public class LocationAssert extends AbstractAssert<LocationAssert, Location> {

  protected LocationAssert(@Nullable Location location) {
    super(location, LocationAssert.class);
  }

  public static LocationAssert assertThat(@Nullable Location actual) {
    return new LocationAssert(actual);
  }

  public LocationAssert hasLocation(int expectedPosition, int expectedLength) {
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.position()).isEqualTo(expectedPosition);
      softly.assertThat(actual.length()).isEqualTo(expectedLength);
    });
    return this;
  }

  public LocationAndCodeAssert on(String sourceCode) {
    return new LocationAndCodeAssert(actual, sourceCode);
  }

  public static class LocationAndCodeAssert extends AbstractAssert<LocationAndCodeAssert, Location> {

    private final String sourceCode;

    protected LocationAndCodeAssert(Location location, String sourceCode) {
      super(location, LocationAndCodeAssert.class);
      this.sourceCode = sourceCode;
    }

    @Override
    public LocationAndCodeAssert isEqualTo(Object expected) {
      var text = sourceCode.substring(actual.position(), actual.position() + actual.length());
      org.assertj.core.api.Assertions.assertThat(text).isEqualTo(expected);
      return this;
    }

    public LocationAndCodeAssert hasLocation(int expectedPosition, int expectedLength) {
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(actual.position()).isEqualTo(expectedPosition);
        softly.assertThat(actual.length()).isEqualTo(expectedLength);
      });
      return this;
    }
  }
}
