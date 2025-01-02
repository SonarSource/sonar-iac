/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.TreeParser;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.testing.AbstractMetricsTest;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigParser;
import org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension;

import static org.assertj.core.api.Assertions.assertThat;

class JvmFrameworkConfigMetricsVisitorTest extends AbstractMetricsTest {
  @Override
  protected String languageKey() {
    return JvmFrameworkConfigExtension.LANGUAGE_KEY;
  }

  @Override
  protected TreeParser<? extends Tree> treeParser() {
    return new JvmFrameworkConfigParser();
  }

  @Override
  protected MetricsVisitor metricsVisitor(FileLinesContextFactory fileLinesContextFactory) {
    return new JvmFrameworkConfigMetricsVisitor(fileLinesContextFactory, noSonarFilter, sensorTelemetryMetrics);
  }

  @Test
  void shouldCalculateMetricsForProperties() {
    language = JvmFrameworkConfigExtension.LANGUAGE_KEY;

    scan(
      // language=properties
      """
        # Comment 1
        sonar.host.url=http://localhost:9000


        sonar.projectKey=sonar-iac
        sonar.projectName=sonar-iac
        # Comment 2
        #---
        spring.profiles.active=profile2
        ! comment in another format
        """,
      "application.properties");

    assertThat(visitor.linesOfCode()).containsExactly(2, 5, 6, 9);
    assertThat(visitor.commentLines()).containsExactly(1, 7, 8, 10);
    assertThat(visitor.noSonarLines()).isEmpty();

    verifyLinesOfCodeMetricsAndTelemetry(2, 5, 6, 9);
  }

  @Test
  void shouldCalculateMetricsForYaml() {
    language = YamlLanguage.KEY;

    scan(
      // language=yaml
      """
        # Comment 1
        spring:
        # datasource
          datasource:
            url: jdbc:mysql://127.0.0.1:3306/example-database
            driver-class-name: com.mysql.jdbc.Driver
            username: root

            password:
        # cache
          cache:
            type: ehcache
        """,
      "application.yml");

    assertThat(visitor.linesOfCode()).containsExactly(2, 4, 5, 6, 7, 9, 11, 12);
    assertThat(visitor.commentLines()).containsExactly(1, 3, 10);
    assertThat(visitor.noSonarLines()).isEmpty();

    verifyLinesOfCodeMetricsAndTelemetry(2, 4, 5, 6, 7, 9, 11, 12);
  }
}
