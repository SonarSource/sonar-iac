/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.plugin;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TerraformSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private static final CheckFactory checkFactory = mock(CheckFactory.class);
  private static final Checks<Object> checks = mock(Checks.class);
  private static final FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
  private static final FileLinesContext fileLinesContext = mock(FileLinesContext.class);
  private static final NoSonarFilter noSonarFilter = mock(NoSonarFilter.class);

  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());

  static {
    when(checks.addAnnotatedChecks(any(Iterable.class))).thenReturn(checks);
    when(checkFactory.create(anyString())).thenReturn(checks);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  void should_return_terraform_descriptor() {
    TerraformSensor sensor = createTerraformSensor();
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Terraform Sensor");
    assertThat(descriptor.languages()).containsOnly("terraform");
  }

  @Test
  void should_log_info() {
    TerraformSensor sensor = createTerraformSensor();
    sensor.execute(context);
    assertThat(logTester.logs(LoggerLevel.INFO)).contains("Execute TerraformSensor");
  }

  private static TerraformSensor createTerraformSensor() {
    return new TerraformSensor(fileLinesContextFactory, checkFactory, noSonarFilter);
  }
}
