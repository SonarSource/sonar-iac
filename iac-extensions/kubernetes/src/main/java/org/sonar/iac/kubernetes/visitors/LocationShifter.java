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
package org.sonar.iac.kubernetes.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;

/**
 * This class is used to store all lines that has to be shifted.
 * The data are stored into this class through methods {@link #addLineSize(int)} and {@link #addShiftedLine(int, int)}.
 * Then we can use those data through the method {@link #computeShiftedLocation(TextRange)}, which for a given {@link TextRange} will provide
 * a shifted {@link TextRange}.
 * This is especially used in helm context, when the issue we are detecting on the transformed code should be raised on the original code.
 */
public class LocationShifter {

  private final Map<Integer, Integer> shiftedLines = new HashMap<>();
  private final List<Integer> linesSize = new ArrayList<>();

  public void addShiftedLine(int originalLine, int targetLine) {
    shiftedLines.put(originalLine, targetLine);
  }

  public void clear() {
    shiftedLines.clear();
    linesSize.clear();
  }

  public void addLineSize(int size) {
    linesSize.add(size);
  }

  public TextRange computeShiftedLocation(TextRange textRange) {
    int lineStart = textRange.start().line();
    int lineEnd = textRange.end().line();

    if (!shiftedLines.containsKey(lineStart) && !shiftedLines.containsKey(lineEnd)) {
      return textRange;
    }

    TextPointer start;
    TextPointer end;

    if (shiftedLines.containsKey(lineStart)) {
      start = new TextPointer(shiftedLines.get(lineStart), 0);
    } else {
      start = textRange.start();
    }

    if (shiftedLines.containsKey(lineEnd)) {
      int shiftedLine = shiftedLines.get(lineEnd);
      end = new TextPointer(shiftedLine, linesSize.get(shiftedLine - 1));
    } else {
      end = textRange.end();
    }

    return new TextRange(start, end);
  }
}
