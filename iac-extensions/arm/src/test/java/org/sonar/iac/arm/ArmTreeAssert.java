/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.common.testing.IacCommonAssertions;

public class ArmTreeAssert extends AbstractAssert<ArmTreeAssert, ArmTree> {

  private ArmTreeAssert(@Nullable ArmTree actual) {
    super(actual, ArmTreeAssert.class);
  }

  public static ArmTreeAssert assertThat(@Nullable ArmTree actual) {
    return new ArmTreeAssert(actual);
  }

  public ArmTreeAssert is(ArmTree.Kind kind) {
    isNotNull();
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.is(kind))
        .overridingErrorMessage("Expected node to be of kind %s but got %s", kind, actual.getKind())
        .isTrue();
    });
    return this;
  }

  public ArmTreeAssert has(String methodName, String value) {
    isNotNull();
    SoftAssertions.assertSoftly(softly -> {
      Method method = null;
      try {
        method = actual.getClass().getMethod(methodName);
        String elementValue = (String) method.invoke(actual);
        org.assertj.core.api.Assertions.assertThat(elementValue).isEqualTo(value);
      } catch (NoSuchMethodException e) {
        Assertions.fail("NoSuchMethodException raised: couldn't find method '" + methodName + "()' for object '" + actual.getClass().getSimpleName() + "'");
      } catch (InvocationTargetException e) {
        Assertions.fail("InvocationTargetException raised: couldn't call method '" + methodName + "()' for object '" + actual.getClass().getSimpleName() + "'");
      } catch (IllegalAccessException e) {
        Assertions.fail("IllegalAccessException raised: couldn't call method '" + methodName + "()' for object '" + actual.getClass().getSimpleName() + "'");
      }
    });
    return this;
  }

  public ArmTreeAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    IacCommonAssertions.assertThat(actual.textRange()).hasRange(startLine, startLineOffset, endLine, endLineOffset);
    return this;
  }
}
