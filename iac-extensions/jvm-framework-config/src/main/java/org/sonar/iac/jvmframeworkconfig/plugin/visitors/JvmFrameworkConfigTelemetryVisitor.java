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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.jvmframeworkconfig.tree.api.Tuple;

public class JvmFrameworkConfigTelemetryVisitor extends TreeVisitor<InputFileContext> {
  private static final String TELEMETRY_PREFIX = "java.";
  protected boolean springRecorded = false;
  protected boolean quarkusRecorded = false;
  protected boolean micronautRecorded = false;

  public JvmFrameworkConfigTelemetryVisitor() {
    register(Tuple.class, this::findFramework);
  }

  boolean allFrameworksRecorded() {
    return quarkusRecorded && springRecorded && micronautRecorded;
  }

  public void storeTelemetry(SensorTelemetry sensorTelemetry) {
    sensorTelemetry.addTelemetry(TELEMETRY_PREFIX + "spring", springRecorded ? "1" : "0");
    sensorTelemetry.addTelemetry(TELEMETRY_PREFIX + "quarkus", quarkusRecorded ? "1" : "0");
    sensorTelemetry.addTelemetry(TELEMETRY_PREFIX + "micronaut", micronautRecorded ? "1" : "0");
  }

  private void findFramework(InputFileContext ctx, Tuple tuple) {
    if (allFrameworksRecorded()) {
      return;
    }
    recordFramework(tuple.key().value().value());
  }

  void recordFramework(String key) {
    if (!springRecorded && key.startsWith("spring.")) {
      springRecorded = true;
    } else if (!micronautRecorded && key.startsWith("micronaut.")) {
      micronautRecorded = true;
    } else if (!quarkusRecorded && key.contains("quarkus.")) {
      quarkusRecorded = true;
    }
  }
}
