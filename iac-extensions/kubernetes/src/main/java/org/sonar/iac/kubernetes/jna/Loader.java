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
package org.sonar.iac.kubernetes.jna;

import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

public class Loader {
  private Loader() {
  }

  /**
   * Load a native library. This method takes into account that we use OS and architecture as suffixes of the library name.
   * It also takes care of mapping Go function names to Java method names not to break camelCase convention on Java side.
   */
  public static <T extends Library> T load(String name, Class<T> libraryClass) {
    var os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    if (os.startsWith("mac")) {
      os = "darwin";
    } else if (os.startsWith("win")) {
      os = "windows";
    } else if (os.startsWith("linux")) {
      os = "linux";
    } else {
      throw new IllegalStateException("Unsupported OS: " + os);
    }
    var arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

    return Native.load(name + "-" + os + "-" + arch, libraryClass, Map.of(
      Library.OPTION_FUNCTION_MAPPER, (FunctionMapper) (NativeLibrary library, Method method) -> StringUtils.capitalize(method.getName())));
  }
}
