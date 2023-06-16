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
package org.sonar.iac;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.sonar.api.batch.sensor.Sensor;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = {"org.sonar.iac", "org.sonar.plugins.iac"}, importOptions = ImportOption.Predefined.DoNotIncludeTests.class)
public class ArchUnitTest {
  @ArchTest
  static final ArchRule shouldNotUseInternalPackage = noClasses().should().dependOnClassesThat().resideInAPackage("org.sonar.api.internal..");

  @ArchTest
  static final ArchRule sensorsShouldHavePublicConstructor = classes().that().implement(Sensor.class)
    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
    .should(new ArchCondition<>("have a public constructor") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        boolean satisfied = javaClass.getConstructors().stream()
          .allMatch(constructor -> constructor.getModifiers().contains(JavaModifier.PUBLIC));
        String message = javaClass.getDescription() + (satisfied ? " has" : " does not have")
          + " all public constructors";
        events.add(new SimpleConditionEvent(javaClass, satisfied, message));
      }
    });
}
