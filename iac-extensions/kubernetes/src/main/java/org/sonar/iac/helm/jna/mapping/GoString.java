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
package org.sonar.iac.helm.jna.mapping;

import com.sun.jna.Structure;

/**
 * See 'sonar-helm-for-iac-*.h' for corresponding C signature.
 */
@Structure.FieldOrder({"p", "n"})
public class GoString extends Structure {
  /**
   * This class is an indicator for JNA to pass instances by value rather than by reference (i.e. address).
   */
  public static class ByValue extends GoString implements Structure.ByValue {
    public ByValue(String p) {
      super(p);
    }
  }

  // no-arg constructor is required by JNA
  public GoString() {
  }

  public GoString(String p) {
    this.p = p;
    this.n = p.length();
  }

  public String p;
  public long n;
}
