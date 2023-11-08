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
package org.sonar.iac.helm.jna;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import java.util.Arrays;
import org.sonar.iac.helm.jna.mapping.GoSlice;
import org.sonar.iac.helm.jna.mapping.GoString;

public class Example {
  // Mapping for functions from calc.h
  // which is generated from main.go during `mvn exec:exec`
  public interface Calc extends Library {
    long Add(long a, long b);

    double Cosine(double val);

    void Sort(GoSlice.ByValue vals);

    long Log(GoString.ByValue str);
  }

  public static void main(String[] args) {
    var extension = System.getProperty("os.name").toLowerCase().startsWith("win") ? ".dll" : ".so";
    Calc calc = Native.loadLibrary(
      Example.class.getResource("/calc" + extension).getPath(),
      Calc.class);

    System.out.println("12+99 via JNA Go bridge: " + calc.Add(12, 99));
    System.out.println("cos(1.0)=" + calc.Cosine(1.0));

    long[] nums = new long[] {53, 11, 5, 2, 88};
    Memory arr = new Memory(nums.length * Native.getNativeSize(Long.TYPE));
    arr.write(0, nums, 0, nums.length);
    GoSlice.ByValue slice = new GoSlice.ByValue();
    slice.data = arr;
    slice.len = nums.length;
    slice.cap = nums.length;
    calc.Sort(slice);
    long[] sorted = arr.getLongArray(0, nums.length);
    System.out.println("Sorted array:" + Arrays.toString(sorted));

    GoString.ByValue str = new GoString.ByValue();
    str.p = "Hello Java!";
    str.n = str.p.length();
    System.out.printf("Return code of Log: " + calc.Log(str));
  }
}
