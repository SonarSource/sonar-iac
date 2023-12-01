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

import java.util.HashMap;
import java.util.Map;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.extension.visitors.InputFileContext;

/**
 * This class is used to store all lines that has to be shifted.
 * The data are stored into this class through methods {@link #addLineSize(InputFileContext, int, int)} and {@link #addShiftedLine(InputFileContext, int, int)}.
 * Then we can use those data through the method {@link #computeShiftedLocation(InputFileContext, TextRange)}, which for a given {@link TextRange} will provide
 * a shifted {@link TextRange}.
 * Every store or access methods is required to provide the concerned {@link InputFileContext}, as the data are stored contextually to this object.
 * This is especially used in helm context, when the issue we are detecting on the transformed code should be raised on the original code.
 */
public class LocationShifter {

  private final Map<InputFileContext, LinesShifting> linesShiftingPerContext = new HashMap<>();

  public void addShiftedLine(InputFileContext ctx, int originalLine, int targetLine) {
    getOrCreateLinesShifting(ctx)
      .getOrCreateLinesData(originalLine).targetLine = targetLine;
  }

  public void addLineSize(InputFileContext ctx, int originalLine, int size) {
    getOrCreateLinesShifting(ctx)
      .getOrCreateLinesData(originalLine).originalLineSize = size;
  }

  public TextRange computeShiftedLocation(InputFileContext ctx, TextRange textRange) {
    int lineStart = textRange.start().line();
    int lineEnd = textRange.end().line();
    var linesData = getOrCreateLinesShifting(ctx).linesData;
    var lineStartData = linesData.get(lineStart);
    var lineEndData = linesData.get(lineEnd);

    if ((lineStartData == null || lineStartData.targetLine == null)
      && (lineEndData == null || lineEndData.targetLine == null)) {
      return textRange;
    }

    TextPointer start;
    TextPointer end;

    if (lineStartData != null && lineStartData.targetLine != null) {
      start = new TextPointer(lineStartData.targetLine, 0);
    } else {
      start = textRange.start();
    }

    if (lineEndData != null && lineEndData.targetLine != null) {
      end = new TextPointer(lineEndData.targetLine, linesData.get(lineEndData.targetLine).originalLineSize);
    } else {
      end = textRange.end();
    }

    return new TextRange(start, end);
  }

  private LinesShifting getOrCreateLinesShifting(InputFileContext ctx) {
    return linesShiftingPerContext.computeIfAbsent(ctx, context -> new LinesShifting());
  }

  /**
   * Store information related to an original line number.
   * The {@link #linesData} Map contain the original line number as the key.
   * The original line length and target line number are stored in the value, as a {@link LineData} object.
   */
  static class LinesShifting {
    private final Map<Integer, LineData> linesData = new HashMap<>();

    private LineData getOrCreateLinesData(Integer originalLine) {
      return linesData.computeIfAbsent(originalLine, line -> new LineData());
    }
  }

  static class LineData {
    private Integer targetLine;
    private int originalLineSize;
  }
}
