/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.parser.grammar;

import org.junit.jupiter.api.Test;
import org.sonar.sslr.internal.vm.Machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseInsensitiveStringExpressionTest {

  @Test
  void shouldMatchExactString() {
    var expression = new CaseInsensitiveStringExpression("hello");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(5);
    when(machine.charAt(0)).thenReturn('h');
    when(machine.charAt(1)).thenReturn('e');
    when(machine.charAt(2)).thenReturn('l');
    when(machine.charAt(3)).thenReturn('l');
    when(machine.charAt(4)).thenReturn('o');

    expression.execute(machine);

    verify(machine).createLeafNode(expression, 5);
    verify(machine).jump(1);
    verify(machine, never()).backtrack();
  }

  @Test
  void shouldMatchStringIgnoringCase() {
    var expression = new CaseInsensitiveStringExpression("hello");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(5);
    when(machine.charAt(0)).thenReturn('H');
    when(machine.charAt(1)).thenReturn('E');
    when(machine.charAt(2)).thenReturn('L');
    when(machine.charAt(3)).thenReturn('L');
    when(machine.charAt(4)).thenReturn('O');

    expression.execute(machine);

    verify(machine).createLeafNode(expression, 5);
    verify(machine).jump(1);
    verify(machine, never()).backtrack();
  }

  @Test
  void shouldMatchMixedCaseString() {
    var expression = new CaseInsensitiveStringExpression("HeLLo");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(5);
    when(machine.charAt(0)).thenReturn('h');
    when(machine.charAt(1)).thenReturn('E');
    when(machine.charAt(2)).thenReturn('l');
    when(machine.charAt(3)).thenReturn('L');
    when(machine.charAt(4)).thenReturn('o');

    expression.execute(machine);

    verify(machine).createLeafNode(expression, 5);
    verify(machine).jump(1);
    verify(machine, never()).backtrack();
  }

  @Test
  void shouldBacktrackWhenInputTooShort() {
    var expression = new CaseInsensitiveStringExpression("hello");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(3);

    expression.execute(machine);

    verify(machine).backtrack();
    verify(machine, never()).createLeafNode(any(), anyInt());
    verify(machine, never()).jump(anyInt());
  }

  @Test
  void shouldBacktrackOnCharacterMismatch() {
    var expression = new CaseInsensitiveStringExpression("hello");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(5);
    when(machine.charAt(0)).thenReturn('h');
    when(machine.charAt(1)).thenReturn('e');
    when(machine.charAt(2)).thenReturn('x'); // mismatch
    when(machine.charAt(3)).thenReturn('l');
    when(machine.charAt(4)).thenReturn('o');

    expression.execute(machine);

    verify(machine).backtrack();
    verify(machine, never()).createLeafNode(any(), anyInt());
    verify(machine, never()).jump(anyInt());
  }

  @Test
  void shouldHandleEmptyString() {
    var expression = new CaseInsensitiveStringExpression("");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(0);

    expression.execute(machine);

    verify(machine).createLeafNode(expression, 0);
    verify(machine).jump(1);
    verify(machine, never()).backtrack();
  }

  @Test
  void shouldHandleSingleCharacter() {
    var expression = new CaseInsensitiveStringExpression("A");
    var machine = mock(Machine.class);
    when(machine.length()).thenReturn(1);
    when(machine.charAt(0)).thenReturn('a');

    expression.execute(machine);

    verify(machine).createLeafNode(expression, 1);
    verify(machine).jump(1);
    verify(machine, never()).backtrack();
  }

  @Test
  void shouldReturnCorrectToString() {
    var expression = new CaseInsensitiveStringExpression("Hello");

    assertEquals("String hello", expression.toString());
  }

  @Test
  void shouldReturnCorrectToStringForEmptyString() {
    var expression = new CaseInsensitiveStringExpression("");

    assertEquals("String ", expression.toString());
  }
}
