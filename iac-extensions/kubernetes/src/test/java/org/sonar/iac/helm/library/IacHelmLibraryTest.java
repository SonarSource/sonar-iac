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
package org.sonar.iac.helm.library;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.common.testing.IacTestUtils;
import org.sonar.iac.helm.jna.Loader;
import org.sonar.iac.helm.jna.library.IacHelmLibrary;
import org.sonar.iac.helm.jna.mapping.GoString;
import org.sonarsource.iac.helm.TemplateEvaluationResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class IacHelmLibraryTest {
  @Test
  void shouldFailWhenLoadingNonExistingLibrary() {
    var os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      os = "windows";
    } else if (os.contains("mac")) {
      os = "darwin";
    } else {
      os = "linux";
    }
    var arch = System.getProperty("os.arch").toLowerCase();
    var loader = new Loader();
    Assertions.assertThatThrownBy(() -> loader.load("/non-existing-library", IacHelmLibrary.class))
      .isInstanceOf(UnsatisfiedLinkError.class)
      .hasMessageStartingWith("Unable to load library '/non-existing-library-" + os + "-" + arch + "'");
  }

  @Test
  void shouldFailWhenLoadingForUnknownPlatform() {
    var loader = Mockito.mock(Loader.class);
    Mockito.when(loader.getNormalizedOsName(anyString())).thenReturn("freebsd");
    Mockito.when(loader.getNormalizedArchName(anyString())).thenReturn("amd64");
    Mockito.when(loader.load(any(), any())).thenCallRealMethod();
    var arch = System.getProperty("os.arch").toLowerCase();

    Assertions.assertThatThrownBy(() -> loader.load("/sonar-helm-for-iac", IacHelmLibrary.class))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unsupported platform: freebsd-" + arch);
  }

  @Test
  void shouldEvaluateTemplate() throws URISyntaxException, IOException {
    var iacHelmLibrary = (new Loader()).load("/sonar-helm-for-iac", IacHelmLibrary.class);

    ClassLoader classLoader = getClass().getClassLoader();
    var template = new String(Files.readAllBytes(Path.of(classLoader.getResource("helm/templates/pod.yaml").toURI())));
    var rawEvaluationResult = iacHelmLibrary.evaluateTemplate(
      new GoString.ByValue("/helm/templates/pod.yaml"), new GoString.ByValue(template), new GoString.ByValue("container:\n  port: 8080"));
    var evaluationResult = TemplateEvaluationResult.parseFrom(rawEvaluationResult.getByteArray());

    Assertions.assertThat(evaluationResult.getError()).isEmpty();
    Assertions.assertThat(evaluationResult.getTemplate()).isEqualTo(IacTestUtils.code(
      "apiVersion: v1",
      "kind: Pod",
      "metadata:",
      "  name: example",
      "spec:",
      "  containers:",
      "    - name: web",
      "      image: nginx",
      "      ports:",
      "        - name: web",
      "          containerPort: 8080",
      "          protocol: TCP",
      ""));
  }

  @Test
  void testGoString() {
    // Empty constructor is required by JNA, but isn't covered by tests
    var goString = new GoString();
    Assertions.assertThat(goString.p).isNull();
  }
}
