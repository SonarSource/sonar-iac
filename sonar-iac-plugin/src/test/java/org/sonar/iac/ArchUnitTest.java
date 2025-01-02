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
package org.sonar.iac;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.iac.helm.tree.api.Node;

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

  /**
   * The Path.toRealPath() on Mac resolve temp directories so unit test failing, e.g.:
   * /var/folders/.../test will be resolved to /private/var/folders/.../test
   * The production code should not operate on resolving real path as virtual file systems should be used.
   */
  @ArchTest
  static final ArchRule shouldNotCallPathToRealPath = noClasses().should().callMethod(Path.class, "toRealPath", LinkOption[].class);

  @ArchTest
  static final ArchRule shouldOverrideChildrenMethodIfContainsNodes = classes().that().implement(Node.class)
    .and().doNotHaveModifier(JavaModifier.ABSTRACT)
    .and().containAnyFieldsThat(new DescribedPredicate<>("are assignable to %s", Node.class.getSimpleName()) {
      @Override
      public boolean test(JavaField javaField) {
        return javaField.getRawType().isAssignableTo(Node.class) ||
          hasCollectionOfNodes(javaField);
      }

      private boolean hasCollectionOfNodes(JavaField javaField) {
        return javaField.getType() instanceof JavaParameterizedType &&
          ((JavaParameterizedType) javaField.getType()).getActualTypeArguments().get(0).toErasure().isAssignableTo(Node.class);
      }
    })
    .should(new ArchCondition<>("override the method children()") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        try {
          var method = item.getMethod("children");
          if (!method.getOwner().equals(item)) {
            var message = String.format("The %s doesn't override the method children() and contains Node as fields. " +
              "The AbstractNode.children() implementation will be not sufficient", item.getName());
            events.add(SimpleConditionEvent.violated(item, message));
          }
        } catch (IllegalArgumentException e) {
          var message = String.format("The %s doesn't override the method children() and contains Node as fields. " +
            "The AbstractNode.children() implementation will be not sufficient", item.getName());
          events.add(SimpleConditionEvent.violated(item, message));
        }
      }
    });
}
