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
package org.sonar.iac.kubernetes.jna.library;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.helm.jna.Loader;
import org.sonar.iac.helm.jna.library.IacHelmLibrary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;

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
    Assertions.assertThatThrownBy(() -> (new Loader()).load("/non-existing-library", IacHelmLibrary.class))
      .isInstanceOf(UnsatisfiedLinkError.class)
      .hasMessageStartingWith("Unable to load library '/non-existing-library-" + os + "-" + arch + "'");
  }

  @Test
  void shouldFailWhenLoadingForUnknownPlatform() {
    var loader = Mockito.mock(Loader.class);
    Mockito.when(loader.getNormalizedOsName()).thenReturn("freebsd");
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
    var evaluationResult = iacHelmLibrary.evaluateTemplate("/helm/templates/pod.yaml", template, "container:\n  port: 8080");

    Assertions.assertThat(evaluationResult.getError()).isEmpty();
    Assertions.assertThat(evaluationResult.getTemplate()).isEqualTo("apiVersion: v1\n" +
      "kind: Pod\n" +
      "metadata:\n" +
      "  name: example\n" +
      "spec:\n" +
      "  containers:\n" +
      "    - name: web\n" +
      "      image: nginx\n" +
      "      ports:\n" +
      "        - name: web\n" +
      "          containerPort: 8080\n" +
      "          protocol: TCP\n");
  }
}
