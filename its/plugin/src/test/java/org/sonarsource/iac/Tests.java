/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonarsource.iac;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MeasuresTest.class,
  NoSonarTest.class,
  PropertiesTest.class
})
public class Tests {

  static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE[8.9]";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR;

  public static final FileLocation IAC_PLUGIN_LOCATION = FileLocation.byWildcardMavenFilename(new File("../../sonar-iac-plugin/target"), "sonar-iac-plugin-*.jar");

  static {
    ORCHESTRATOR = Orchestrator.builderEnv()
      .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
      .addPlugin(IAC_PLUGIN_LOCATION)
      .restoreProfileAtStartup(FileLocation.of("src/test/resources/nosonar-terraform.xml"))
      .build();
  }
}
