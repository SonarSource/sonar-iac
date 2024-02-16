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
package org.sonar.iac.helm.tree.impl;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.sonar.iac.helm.tree.api.Location;

public class LocationAssert extends AbstractAssert<LocationAssert, Location> {

  protected LocationAssert(Location location) {
    super(location, LocationAssert.class);
  }

  public static LocationAssert assertThat(Location actual) {
    return new LocationAssert(actual);
  }

  public LocationAssert hasLocation(int expectedPosition, int expectedLength) {
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.position()).isEqualTo(expectedPosition);
      softly.assertThat(actual.length()).isEqualTo(expectedLength);
    });
    return this;
  }

  public LocationAssertAndText on(String sourceCode) {
    return new LocationAssertAndText(actual, sourceCode);
  }

  public class LocationAssertAndText extends AbstractAssert<LocationAssertAndText, Location> {

    private final String sourceCode;

    protected LocationAssertAndText(Location location, String sourceCode) {
      super(location, LocationAssertAndText.class);
      this.sourceCode = sourceCode;
    }

    @Override
    public LocationAssertAndText isEqualTo(Object expected) {
      var text = sourceCode.substring(actual.position(), actual.position() + actual.length());
      org.assertj.core.api.Assertions.assertThat(text).isEqualTo(expected);
      return this;
    }

    public LocationAssertAndText hasLocation(int expectedPosition, int expectedLength) {
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(actual.position()).isEqualTo(expectedPosition);
        softly.assertThat(actual.length()).isEqualTo(expectedLength);
      });
      return this;
    }
  }
}
